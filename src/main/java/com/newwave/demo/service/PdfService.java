package com.newwave.demo.service;

import com.newwave.demo.models.UserModel;

import java.io.File;
import java.util.List;

public interface PdfService {

    File generateUserPdf(UserModel userModel);
    File generateAllUserPdf(List<UserModel> userModels);
}
