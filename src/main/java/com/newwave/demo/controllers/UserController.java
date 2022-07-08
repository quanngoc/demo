package com.newwave.demo.controllers;

import com.newwave.demo.payload.request.SearchUserRequest;
import com.newwave.demo.payload.request.UserRequest;
import com.newwave.demo.payload.response.MessageResponse;
import com.newwave.demo.payload.response.UserResponse;
import com.newwave.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Page<UserResponse>> searchInvoices(SearchUserRequest request,
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

    @PutMapping("/export-pdf/{id}")
    @PreAuthorize("hasRole('USER')  or hasRole('ADMIN')")
    public ResponseEntity<?> exportPDF(@PathVariable Long id) {
        byte[] pdf = userService.exportPDF(id);
        return ResponseEntity.ok(pdf);
    }
}
