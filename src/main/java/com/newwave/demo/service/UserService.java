package com.newwave.demo.service;

import com.newwave.demo.payload.request.LoginRequest;
import com.newwave.demo.payload.request.SearchUserRequest;
import com.newwave.demo.payload.request.SignupRequest;
import com.newwave.demo.payload.request.UserRequest;
import com.newwave.demo.payload.response.projection.ChartResponse;
import com.newwave.demo.payload.response.JwtResponse;
import com.newwave.demo.payload.response.UserExcelResponse;
import com.newwave.demo.payload.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    JwtResponse login(LoginRequest loginRequest);

    void register(SignupRequest signupRequest);

    UserResponse findById(Long id);

    Page<UserResponse> search(SearchUserRequest request, Pageable pageable);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    UserResponse changePassword(UserRequest userRequest);

    void resetPassword(Long id);

    void updateRole(UserRequest userRequest, Long id);

    byte[] exportPDF(Long id);

    UserExcelResponse exportExcel(SearchUserRequest request);

    UserResponse update(UserRequest userRequest);

    ChartResponse chartAge();
}
