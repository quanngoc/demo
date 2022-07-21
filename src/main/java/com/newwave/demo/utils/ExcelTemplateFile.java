package com.newwave.demo.utils;

public enum ExcelTemplateFile {
    USER_EXCEL("templates/user_template.xlsx"),
    ;
    private String filePath;

    ExcelTemplateFile(String filePath) {
        this.filePath = filePath;
    }

    public String filePath() {
        return this.filePath;
    }
}
