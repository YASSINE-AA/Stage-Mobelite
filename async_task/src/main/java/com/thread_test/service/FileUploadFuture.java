package com.thread_test.service;

import java.io.InputStream;

public class FileUploadFuture {
    private long start;
    private long uploadTime;
    private final InputStream inputStream;
    private final String filename;
    private boolean isUploaded;

    public FileUploadFuture(InputStream inputStream, String filename, boolean isUploaded) {
        this.inputStream = inputStream;
        this.filename = filename;
        this.isUploaded = isUploaded;
    }

    public boolean getIsUploaded() {
        return isUploaded;
    }

    public void setIsUploaded(boolean isUploaded) {
        this.isUploaded = isUploaded;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public String getFilename() {
        return filename;
    }

    public void setStart(long time) {
        this.start = time;
    }

    public long getStartTime() {
        return start;
    }

    public long getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(long uploadTime) {
        this.uploadTime = uploadTime;
    }
}
