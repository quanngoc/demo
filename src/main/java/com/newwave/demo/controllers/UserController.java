package com.newwave.demo.controllers;

import com.newwave.demo.payload.request.SearchUserRequest;
import com.newwave.demo.payload.request.UserRequest;
import com.newwave.demo.payload.response.MessageResponse;
import com.newwave.demo.payload.response.UserExcelResponse;
import com.newwave.demo.payload.response.UserResponse;
import com.newwave.demo.payload.response.projection.ChartResponse;
import com.newwave.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        UserResponse userResponse = userService.findById(id);
        return ResponseEntity.ok(userResponse);
    }


    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponse>> search(SearchUserRequest request,
                                                     @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                                     @RequestParam(value = "page", defaultValue = "0") int page,
                                                     @RequestParam(name = "sort", required = false, defaultValue = "ASC") String sort) {
        Sort sortable = null;
        if (sort.equals("ASC")) {
            sortable = Sort.by("id").ascending();
        }
        if (sort.equals("DESC")) {
            sortable = Sort.by("id").descending();
        }
        Pageable pageable = PageRequest.of(page, pageSize, sortable);

        Page<UserResponse> pageData = userService.search(request, pageable);

        return ResponseEntity.ok(pageData);
    }

    @PutMapping("/change-password/{id}")
    @PreAuthorize("hasRole('USER')  or hasRole('ADMIN')")
    public ResponseEntity<?> changePassword(@RequestBody UserRequest userRequest, @PathVariable Long id) {
        userRequest.setId(id);
        UserResponse userResponse = userService.changePassword(userRequest);
        return ResponseEntity.ok(userResponse);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')  or hasRole('ADMIN')")
    public ResponseEntity<?> update(@RequestBody UserRequest userRequest, @PathVariable Long id) {
        userRequest.setId(id);
        UserResponse userResponse = userService.update(userRequest);
        return ResponseEntity.ok(userResponse);
    }

    @PutMapping("/reset-password/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return ResponseEntity.ok(new MessageResponse("reset password successfully!"));
    }

    @PutMapping("/update-role/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateRole(@RequestBody UserRequest userRequest, @PathVariable Long id) {
        userService.updateRole(userRequest, id);
        return ResponseEntity.ok(new MessageResponse("update role successfully!"));
    }

    @GetMapping("/export-pdf/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> exportPDF(HttpServletResponse response, @PathVariable Long id) {
        byte[] pdf = userService.exportPDF(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/pdf"));
        String name = String.format("user.pdf");
        response.setHeader("Content-Disposition", "attachment; filename=" + name);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    @GetMapping("/export-excel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity exportExcel(HttpServletResponse response,
                                      SearchUserRequest request) {
        UserExcelResponse data = userService.exportExcel(request);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.ms-excel"));
        String name = String.format("user.xlsx");
        response.setHeader("Content-Disposition", "attachment; filename=" + name);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        return new ResponseEntity<>(data.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/chart-age")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ChartResponse> chartAge() {
        ChartResponse chartResponse = userService.chartAge();
        return new ResponseEntity<>(chartResponse, HttpStatus.OK);
    }

    @GetMapping("/export-pdf")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> exportAllUserPDF(HttpServletResponse response) {
        byte[] pdf = userService.exportAllUserPDF();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/pdf"));
        String name = String.format("all-user.pdf");
        response.setHeader("Content-Disposition", "attachment; filename=" + name);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }
}
