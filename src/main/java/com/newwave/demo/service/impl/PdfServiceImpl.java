package com.newwave.demo.service.impl;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import com.lowagie.text.DocumentException;
import com.newwave.demo.models.UserModel;
import com.newwave.demo.service.PdfService;
import com.newwave.demo.utils.PDFUtils;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PdfServiceImpl implements PdfService {

    private final SpringTemplateEngine templateEngine;
    private static final String USER_TEMPLATE = "pdf-template";
    private static final String ALL_USER_TEMPLATE = "all-user-template";

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
            return PDFUtils.buildPDF(USER_TEMPLATE, attributes, templateEngine);
        } catch (IOException e) {

        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public File generateAllUserPdf(Map<Integer, List<UserModel>> userModels) {
        try {
            List<File> files = new ArrayList<>();
            for (Map.Entry<Integer, List<UserModel>> entry : userModels.entrySet()) {
                Map<String, Object> attributes = new LinkedHashMap<>();
                attributes.put("PAGE", entry.getKey());
                attributes.put("ITEMS", entry.getValue());
                File file = PDFUtils.buildPDF(ALL_USER_TEMPLATE, attributes, templateEngine);
                files.add(file);
            }
            return mergeFile(files);
        } catch (IOException e) {

        } catch (DocumentException e) {
            throw new RuntimeException(e);
        } catch (com.itextpdf.text.DocumentException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    private File mergeFile(List<File> files) throws IOException, com.itextpdf.text.DocumentException {
        File tempFile = File.createTempFile("temp", ".pdf");

        Document document = new Document();
        OutputStream outputStream = new FileOutputStream(tempFile);
        PdfCopy copy = new PdfCopy(document, outputStream);

        document.open();
        for (File file : files) {
            PdfReader reader = new PdfReader(file.getPath());
            copy.addDocument(reader);
            copy.freeReader(reader);
            reader.close();
        }
        document.close();

        return tempFile;
    }
}
