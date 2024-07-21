package com.mobelite.filesplitter.Splitter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

import org.springframework.web.multipart.MultipartFile;

public class SplitterImpl implements Splitter {

    @Override
    public File[] split(File file, double chunkSize) throws IOException {

        if (!file.exists()) {
            System.out.println("File does not exist: " + file.getAbsolutePath());
            return null;
        }
        
        // ChunkSize = 1 => 1 MB
        chunkSize = chunkSize * 1024 * 1024;

      

        int numberOfChunks = (int) Math.ceil((double) file.length() / chunkSize);

        int chunkCount = 1;
        byte[] buffer = new byte[(int) chunkSize];
        File[] files = new File[numberOfChunks];
        String fileName = file.getName();
        long startTime = System.currentTimeMillis();

        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1 && chunkCount <= numberOfChunks) {
                String chunkName = String.format("%s.filesplitter.part_%d", fileName, chunkCount);
                File chunkFile = new File(file.getParentFile(), chunkName);
                try (FileOutputStream fos = new FileOutputStream(chunkFile);
                     BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                    bos.write(buffer, 0, bytesRead);
                    files[chunkCount - 1] = chunkFile; 
                }
                chunkCount++;
            }
            System.out.println("Time taken to split the file: " + (System.currentTimeMillis() - startTime) + " milliseconds");
        } 

        return files;
    }

    @Override
    public File merge(File[] files, String filePath) throws IOException {

        if(files == null || files.length == 0){
            System.out.println("No files to merge");
            return null;
        }

        File destinationFile = new File(filePath);
        long startTime = System.currentTimeMillis();
        try (FileOutputStream fos = new FileOutputStream(destinationFile);
             BufferedOutputStream mergingStream = new BufferedOutputStream(fos)) {

            for (File file : files) {
                if (file != null) {
                    Files.copy(file.toPath(), mergingStream);
                    file.delete();
                }
            }
            System.out.println("Time taken to merge the files: " + (System.currentTimeMillis() - startTime) + " milliseconds");
        }
        return destinationFile;
    
    }

    public static File FileFromMultipart(MultipartFile file, String uploadsDir) throws IOException {
        Path filePath = Paths.get(uploadsDir, file.getOriginalFilename());
        System.out.println(filePath);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return filePath.toFile();
    }



    public static String[] ExistingUploads() throws IOException {
        String uploadsDir = "uploads/";
        File dir = new File(uploadsDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String[] files = dir.list();
        files = Arrays.stream(files).filter(file -> !file.contains(".filesplitter.")).toArray(String[]::new);

        return files;
    }

}
