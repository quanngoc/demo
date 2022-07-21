package com.newwave.demo.service;

import com.newwave.demo.models.UserModel;
import com.newwave.demo.payload.response.UserResponse;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface PdfService {

    File generateUserPdf(UserModel userModel);
    File generateAllUserPdf(Map<Integer, List<UserResponse>> userModels);
}
