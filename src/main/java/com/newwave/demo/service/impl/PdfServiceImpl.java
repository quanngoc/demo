package com.newwave.demo.service.impl;

import com.lowagie.text.DocumentException;
import com.newwave.demo.models.UserModel;
import com.newwave.demo.service.PdfService;
import com.newwave.demo.utils.PDFUtils;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class PdfServiceImpl implements PdfService {

    private final SpringTemplateEngine templateEngine;
    private static final String EMAIL_PDF_TEMPLATE = "email-template";

    public PdfServiceImpl(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }


    @Override
    public File generateUserPdf(UserModel userModel) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("ID", userModel.getId());
        attributes.put("USERNAME", userModel.getUsername());
        attributes.put("EMAIL", userModel.getEmail());

        try {
            return PDFUtils.buildPDF(EMAIL_PDF_TEMPLATE, attributes, templateEngine);
        } catch (IOException e) {

        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }

        return null;
    }
}
