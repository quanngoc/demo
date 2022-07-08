package com.newwave.demo.service.impl;

import com.newwave.demo.models.ERole;
import com.newwave.demo.models.RoleModel;
import com.newwave.demo.models.UserModel;
import com.newwave.demo.payload.request.LoginRequest;
import com.newwave.demo.payload.request.SearchUserRequest;
import com.newwave.demo.payload.request.SignupRequest;
import com.newwave.demo.payload.request.UserRequest;
import com.newwave.demo.payload.response.JwtResponse;
import com.newwave.demo.payload.response.UserResponse;
import com.newwave.demo.repository.RoleRepository;
import com.newwave.demo.repository.UserRepository;
import com.newwave.demo.repository.specification.UserSpecification;
import com.newwave.demo.security.UserDetailsImpl;
import com.newwave.demo.security.jwt.JwtUtils;
import com.newwave.demo.service.PdfService;
import com.newwave.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PdfService pdfService;

    private static final String DEFAULT_PASSWORD = "123456789";

    @Override
    public JwtResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        JwtResponse jwtResponse = new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles);

        return jwtResponse;
    }

    @Override
    public void register(SignupRequest signUpRequest) {
        // Create new user's account
        UserModel userModel = new UserModel(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRole();
        Set<RoleModel> roleModels = new HashSet<>();

        if (strRoles == null) {
            RoleModel userRoleModel = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roleModels.add(userRoleModel);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        RoleModel adminRoleModel = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roleModels.add(adminRoleModel);

                        break;
                    default:
                        RoleModel userRoleModel = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roleModels.add(userRoleModel);
                }
            });
        }

        userModel.setRoles(roleModels);
        userRepository.save(userModel);
    }

    @Override
    public UserResponse findById(Long id) {
        UserModel userModel = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Error: User is not found."));

        UserResponse userResponse = new UserResponse();
        userResponse.setId(userModel.getId());
        userResponse.setUsername(userModel.getUsername());
        userResponse.setEmail(userModel.getEmail());
        return userResponse;
    }

    @Override
    public Page<UserResponse> search(SearchUserRequest request, Pageable pageable) {
        UserSpecification specificationTwo = new UserSpecification(request);

        Page<UserModel> page = userRepository.findAll(specificationTwo, pageable);
        Function<UserModel, UserResponse> converter = source -> {
            UserResponse target = new UserResponse();
            target.setId(source.getId());
            target.setUsername(source.getUsername());
            target.setEmail(source.getEmail());
            return target;
        };

        return page.map(converter);
    }

    @Override
    public Boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public Boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public UserResponse changePassword(UserRequest userRequest) {
        if (!userRequest.getNewpassword().equals(userRequest.getConNewpassword())) {
            throw new RuntimeException("Error: new password diff confirm new password");
        }
        UserModel userModel = userRepository.findById(userRequest.getId()).orElseThrow(() -> new RuntimeException("Error: User is not found."));
        if (!passwordEncoder.matches(userRequest.getOldPassword(), userModel.getPassword())) {
            throw new RuntimeException("Error: password not correct");
        }
        userModel.setPassword(encoder.encode(userRequest.getNewpassword()));
        userRepository.save(userModel);

        UserResponse userResponse = new UserResponse();
        userResponse.setId(userModel.getId());
        userResponse.setUsername(userModel.getUsername());
        userResponse.setEmail(userModel.getEmail());
        return userResponse;
    }

    @Override
    public void resetPassword(Long id) {
        UserModel userModel = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Error: User is not found."));
        userModel.setPassword(encoder.encode(DEFAULT_PASSWORD));
        userRepository.save(userModel);
    }

    @Override
    public void updateRole(UserRequest userRequest, Long id) {
        UserModel userModel = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Error: User is not found."));
        Set<RoleModel> roleModels = new HashSet<>();
        userRequest.getRole().forEach(role -> {
            switch (role) {
                case "admin":
                    RoleModel adminRoleModel = roleRepository.findByName(ERole.ROLE_ADMIN)
                            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                    roleModels.add(adminRoleModel);

                    break;
                default:
                    RoleModel userRoleModel = roleRepository.findByName(ERole.ROLE_USER)
                            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                    roleModels.add(userRoleModel);
            }
        });
        userModel.setRoles(roleModels);
        userRepository.save(userModel);
    }

    @Override
    public byte[] exportPDF(Long id) {
        UserModel userModel = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Error: User is not found."));
        File file = pdfService.generateUserPdf(userModel);
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            return bytes;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
