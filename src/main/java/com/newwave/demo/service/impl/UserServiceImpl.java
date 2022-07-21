package com.newwave.demo.service.impl;

import com.newwave.demo.models.ERole;
import com.newwave.demo.models.RoleModel;
import com.newwave.demo.models.UserModel;
import com.newwave.demo.payload.request.LoginRequest;
import com.newwave.demo.payload.request.SearchUserRequest;
import com.newwave.demo.payload.request.SignupRequest;
import com.newwave.demo.payload.request.UserRequest;
import com.newwave.demo.payload.response.JwtResponse;
import com.newwave.demo.payload.response.UserExcelResponse;
import com.newwave.demo.payload.response.UserResponse;
import com.newwave.demo.payload.response.projection.ChartResponse;
import com.newwave.demo.repository.RoleRepository;
import com.newwave.demo.repository.UserRepository;
import com.newwave.demo.repository.dao.UserDao;
import com.newwave.demo.repository.specification.UserSpecification;
import com.newwave.demo.security.UserDetailsImpl;
import com.newwave.demo.security.jwt.JwtUtils;
import com.newwave.demo.service.PdfService;
import com.newwave.demo.service.UserService;
import com.newwave.demo.utils.ExcelTemplateFile;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.jxls.common.Context;
import org.jxls.transform.poi.PoiTransformer;
import org.jxls.util.JxlsHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private static final int PAGE_SIZE = 1000;
    private static final int PDF_PAGE_SIZE = 27;
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

    @Autowired
    private UserDao userDao;

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
        validate(signUpRequest);
        // Create new user's account
        UserModel userModel = new UserModel(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()),
                signUpRequest.getName(),
                signUpRequest.getAge());

        Set<RoleModel> roleModels = new HashSet<>();
        RoleModel userRoleModel = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        roleModels.add(userRoleModel);
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
        return populateUserResponse(userModel);
    }

    @Override
    public Page<UserResponse> search(SearchUserRequest request, Pageable pageable) {
        UserSpecification specificationTwo = new UserSpecification(request);

        Page<UserModel> page = userRepository.findAll(specificationTwo, pageable);
        Function<UserModel, UserResponse> converter = source -> {
            return populateUserResponse(source);
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

        if (StringUtils.isEmpty(userRequest.getNewPassword())) {
            throw new RuntimeException("Error: new password cannot be null");
        }

        if (StringUtils.isEmpty(userRequest.getConNewPassword())) {
            throw new RuntimeException("Error: confirm new password cannot be null");
        }

        if (!userRequest.getNewPassword().equals(userRequest.getConNewPassword())) {
            throw new RuntimeException("Error: confirm password don't match!");
        }
        UserModel userModel = userRepository.findById(userRequest.getId()).orElseThrow(() -> new RuntimeException("Error: User is not found."));
        if (!passwordEncoder.matches(userRequest.getOldPassword(), userModel.getPassword())) {
            throw new RuntimeException("Error: password not correct");
        }
        userModel.setPassword(encoder.encode(userRequest.getNewPassword()));
        userRepository.save(userModel);

        return populateUserResponse(userModel);
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
        switch (userRequest.getRole()) {
            case "ROLE_ADMIN":
                RoleModel adminRoleModel = roleRepository.findByName(ERole.ROLE_ADMIN)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                roleModels.add(adminRoleModel);
                break;

            default:
                RoleModel userRoleModel = roleRepository.findByName(ERole.ROLE_USER)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                roleModels.add(userRoleModel);
        }
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

    @Override
    public UserExcelResponse exportExcel(SearchUserRequest request) {
        UserExcelResponse data = new UserExcelResponse();
        data.setDataList(findUser(request));
        data.setContent(exportExcel(data));
        return data;
    }

    @Override
    public UserResponse update(UserRequest userRequest) {
        if (StringUtils.isEmpty(userRequest.getName())) {
            throw new RuntimeException("Error: name cannot be null");
        }

        UserModel userModel = userRepository.findById(userRequest.getId()).orElseThrow(() -> new RuntimeException("Error: User is not found."));

        userModel.setName(userRequest.getName());
        userModel.setAge(userRequest.getAge());
        userRepository.save(userModel);

        UserResponse userResponse = new UserResponse();
        userResponse.setId(userModel.getId());
        userResponse.setUsername(userModel.getUsername());
        userResponse.setEmail(userModel.getEmail());
        return userResponse;
    }

    @Override
    public ChartResponse chartAge() {
        return userRepository.getChartForAge();
    }

    @Override
    public byte[] exportAllUserPDF() {
        Map<Integer, List<UserModel>> userModels = this.findAllUser();
        File file = pdfService.generateAllUserPdf(userModels);
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            return bytes;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<UserResponse> findUser(SearchUserRequest request) {
        int pageIndex = 0;
        Pageable pageable = PageRequest.of(pageIndex, PAGE_SIZE);
        List<UserResponse> data = new ArrayList<>();
        while (true) {
            Page<UserResponse> detailData = this.search(request, pageable);
            if (CollectionUtils.isEmpty(detailData.getContent())) {
                break;
            }
            data.addAll(detailData.getContent());
            pageable = PageRequest.of(++pageIndex, PAGE_SIZE + 5);
        }
        return data;
    }

    private Map<Integer, List<UserModel>> findAllUser() {
        int pageIndex = 0;
        Pageable pageable = PageRequest.of(pageIndex, PDF_PAGE_SIZE);
        Map<Integer, List<UserModel>> dataMap = new HashMap<>();
        while (true) {
            Page<UserModel> detailData = userDao.getUser(pageable);
            if (CollectionUtils.isEmpty(detailData.getContent())) {
                break;
            }
            dataMap.put(detailData.getNumber(), detailData.getContent());
            pageable = PageRequest.of(++pageIndex, PDF_PAGE_SIZE + 5);
        }
        return dataMap;
    }


    protected byte[] exportExcel(UserExcelResponse exportData) {
        ClassPathResource resource = new ClassPathResource(ExcelTemplateFile.USER_EXCEL.filePath());
        try (InputStream is = resource.getInputStream()) {
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                Context context = PoiTransformer.createInitialContext();
                context.putVar("exportDTO", exportData);
                Workbook workbook = WorkbookFactory.create(is);
                PoiTransformer transformer = PoiTransformer.createTransformer(workbook);
                transformer.setOutputStream(os);
                JxlsHelper.getInstance().processTemplate(context, transformer);
                return os.toByteArray();
            }
        } catch (IOException e) {
        }
        return new byte[0];
    }

    private UserResponse populateUserResponse(UserModel userModel) {
        UserResponse userResponse = new UserResponse();
        userResponse.setId(userModel.getId());
        userResponse.setUsername(userModel.getUsername());
        userResponse.setEmail(userModel.getEmail());
        userResponse.setName(userModel.getName());
        userResponse.setAge(userModel.getAge());
        Set<ERole> roles = userModel.getRoles().stream().map(i -> i.getName()).collect(Collectors.toSet());
        userResponse.setRoles(roles);
        return userResponse;
    }

    private void validate(SignupRequest signUpRequest) {
        if (!signUpRequest.getPassword().equals(signUpRequest.getConNewPassword())) {
            throw new RuntimeException("Error: confirm password don't match!");
        }
        if (StringUtils.isEmpty(signUpRequest.getName())) {
            throw new RuntimeException("Error: name cannot be null !");
        }
        if (this.existsByUsername(signUpRequest.getUsername())) {
            throw new RuntimeException("Error: Username is already !");
        }

        if (this.existsByEmail(signUpRequest.getEmail())) {
            throw new RuntimeException("Error: Email is already in use!");
        }
    }
}
