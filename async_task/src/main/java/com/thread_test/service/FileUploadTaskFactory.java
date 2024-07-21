package com.thread_test.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.concurrent.Callable;

@Service
public class FileUploadTaskFactory {

    @Autowired
    private ApplicationContext applicationContext;

    public Callable<Boolean> create(InputStream inputStream) {
        FileUploadTask fileUploadTask = applicationContext.getBean(FileUploadTask.class);
        fileUploadTask.setInputStream(inputStream);
        return fileUploadTask;
    }
}
