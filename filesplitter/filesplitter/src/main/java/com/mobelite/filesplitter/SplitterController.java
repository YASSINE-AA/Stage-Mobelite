package com.mobelite.filesplitter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mobelite.filesplitter.Splitter.SplitterFactory;
import com.mobelite.filesplitter.Splitter.SplitterImpl;

@RestController
public class SplitterController {

    @Autowired
    private Environment env;
    private final SplitterImpl splitterFactory = SplitterFactory.createInstance();
    private final String uploadsDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;
    
    private final ExecutorService executorService = Executors.newFixedThreadPool(4); 

    @GetMapping("/existingUploads")
    public ResponseEntity<List<Map<String, String>>> existingUploads() throws IOException {
        return ResponseEntity.ok(SplitterImpl.ExistingUploads());
    }
@PostMapping("/split")
public ResponseEntity<String> split(@RequestParam("filename") String filename) throws IOException {
    String filePath = uploadsDir + filename;
    File fileToSplit = new File(filePath);
    if (!fileToSplit.exists()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found");
    }

    HttpHeaders headers = new HttpHeaders(); 
    try {
        Future<File[]> future = executorService.submit(() -> {
            return splitterFactory.split(fileToSplit, env.getProperty("filesplitter.chunkSize", Double.class));
        });

        File[] files = future.get(); 
        headers.add("X-Total-Chunks", String.valueOf(files.length));

        return ResponseEntity.status(HttpStatus.OK)
                             .headers(headers)
                             .body("Splitting completed");
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error splitting file");
    }
}

@DeleteMapping("/deleteFile")
public ResponseEntity<String> deleteFile(@RequestParam("filename") String filename) throws IOException {
    String filePath = uploadsDir + filename;
    File fileToDelete = new File(filePath);
    if (!fileToDelete.exists()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found");
    }

    if (!fileToDelete.delete()) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting file");
    }

    return ResponseEntity.ok("File deleted successfully");
}

    @GetMapping("/downloadFileChunk")
    public ResponseEntity<Resource> downloadFileChunk(
            @RequestParam("filename") String filename,
            @RequestParam("chunkIndex") int chunkIndex) throws IOException {

        String filePath = uploadsDir + filename;
        File chunk = new File(String.format("%s.filesplitter.part_%d", filePath, chunkIndex));

        if (!chunk.exists()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // No content if chunk is missing
        }

        FileSystemResource resource = new FileSystemResource(chunk);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", chunk.getName());
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    @PostMapping("/merge")
    public ResponseEntity<String> merge(@RequestParam("files") List<MultipartFile> files, @RequestParam("filename") String filename) throws IOException {
        File dir = new File(uploadsDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        Future<String> future = executorService.submit(() -> {
            try {
                List<File> fileList = new ArrayList<>();
                for (var file : files) {
                    fileList.add(SplitterImpl.FileFromMultipart(file, uploadsDir));
                }
                splitterFactory.merge(fileList.toArray(File[]::new), uploadsDir + filename);
                return "Files uploaded and merged successfully!";
            } catch (IOException e) {
                System.out.println(e);
                return "Failed to upload and merge files";
            }
        });

        try {
            String result = future.get();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error merging files");
        }
    }
}
