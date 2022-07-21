package com.newwave.demo.service;

import com.newwave.demo.models.UserModel;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface PdfService {

    File generateUserPdf(UserModel userModel);

    File generateAllUserPdf(Map<Integer, List<UserModel>> userModels);
}
