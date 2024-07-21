package com.thread_test.service;

public class FileTableRow {
    private String fileName;
    private String time;
    private boolean isUploaded;

    public FileTableRow(String fileName, String time, boolean isUploaded) {
        this.fileName = fileName;
        this.time = time;
        this.isUploaded = isUploaded;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean getIsUploaded() {
        return isUploaded;
    }

    public void setIsUploaded(boolean isUploaded) {
        this.isUploaded = isUploaded;
    }
    
}
