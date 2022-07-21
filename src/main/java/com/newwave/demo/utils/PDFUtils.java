package com.newwave.demo.utils;

import com.lowagie.text.DocumentException;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Map;

public class PDFUtils {
    public static File buildPDF(String template, Map<String, Object> attributes,
                                ITemplateEngine templateEngine) throws IOException, DocumentException {
        File file = File.createTempFile("temp", ".pdf");
        file.deleteOnExit();

        Locale locale = Locale.getDefault();
        Context context = new Context(locale, attributes);
        // generate html from template
        String html = templateEngine.process(template, context);

        // parse html to pdf
        OutputStream outputStream = new FileOutputStream(file);
        ITextRenderer renderer = new ITextRenderer();

        renderer.setDocumentFromString(html);
        renderer.layout();
        renderer.createPDF(outputStream);

        outputStream.close();
        return file;
    }
}
