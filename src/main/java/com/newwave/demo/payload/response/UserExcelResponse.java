package com.newwave.demo.payload.response;

import java.util.List;

public class UserExcelResponse {
    private List<UserResponse> dataList;
    protected byte[] content;

    public List<UserResponse> getDataList() {
        return dataList;
    }

    public void setDataList(List<UserResponse> dataList) {
        this.dataList = dataList;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
