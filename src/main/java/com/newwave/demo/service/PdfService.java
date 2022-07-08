package com.newwave.demo.service;

import com.newwave.demo.models.UserModel;

import java.io.File;

public interface PdfService {

    File generateUserPdf(UserModel userModel);
}
