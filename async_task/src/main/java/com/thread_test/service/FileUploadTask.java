package com.thread_test.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.thread_test.entity.User;

@Service
@Scope("prototype")
public class FileUploadTask implements Callable<Boolean> {

    private InputStream inputStream;

    @Autowired
    private CrudService crudService;

    public FileUploadTask() {}

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public Boolean call() throws Exception {
        System.out.println("Uploading and processing CSV started");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                String[] data = line.split(";");
              //  User user_tmp = new User(Long.valueOf(data[1]), data[0], data[2], data[3]);
             //   crudService.insert(user_tmp);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Task completed.");
        return true;
    }
}
