package com.thread_test.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
public class HomeController {

    private final String uploadFolder = "tmpChunks"; 
    private final String fileMicroserviceUrl = "http://localhost:9090/";
    private final ExecutorService executorService = Executors.newFixedThreadPool(10); // Adjust the number of threads as needed


    @GetMapping("/getFiles")
    public ResponseEntity<List<Map<String, String>>> getFiles() {
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<List> response = restTemplate.getForEntity(fileMicroserviceUrl + "existingUploads", List.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                List<Map<String, String>> fileList = response.getBody();
                return new ResponseEntity<>(fileList, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @DeleteMapping("/deleteFile")
    public ResponseEntity<String> deleteFile(@RequestParam("filename") String filename) {
        HttpHeaders headers = new HttpHeaders();
                RestTemplate restTemplate = new RestTemplate();

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        String url = fileMicroserviceUrl + "deleteFile?filename=" + filename;

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, String.class);
        
        System.out.println("Response from microservice: " + response.getBody());

        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    @PostMapping("/uploadFileChunk")
    @ResponseBody
    public String uploadFileChunk(@RequestParam("file") MultipartFile file,
                                  @RequestParam("chunkIndex") int chunkIndex,
                                  @RequestParam("totalChunks") int totalChunks,
                                  @RequestParam("fileName") String fileName) throws IOException {

        File uploadDir = new File(uploadFolder);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        File chunkFile = new File(uploadFolder, fileName + ".filesplitter.part_" + chunkIndex);
        
        executorService.submit(() -> {
            try (FileOutputStream outputStream = new FileOutputStream(chunkFile);
                 InputStream inputStream = file.getInputStream()) {

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        if (chunkIndex == totalChunks - 1) {
            executorService.submit(() -> {
                try {
                    sendChunksToMicroservice(fileName, totalChunks);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            return "File upload complete!";
        } else {
            return "Chunk " + chunkIndex + " uploaded successfully.";
        }
    }

    @PostMapping("/splitFile")
    public ResponseEntity<String> splitFile(@RequestParam("filename") String filename) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(fileMicroserviceUrl + "split")
                .queryParam("filename", filename);

        ResponseEntity<Resource> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.POST,
                null,
                Resource.class
        );

        HttpHeaders headers = new HttpHeaders();
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            String totalChunksStr = response.getHeaders().getFirst("X-Total-Chunks");
            if (totalChunksStr != null) {
                headers.add("X-Total-Chunks", totalChunksStr);
            }

            return ResponseEntity.ok()
                    .headers(headers)
                    .body("File split successfully");
        } else {
            return ResponseEntity.status(response.getStatusCode())
                    .body("Error splitting file");
        }
    }

    @GetMapping("/downloadFileChunk")
    public ResponseEntity<byte[]> downloadFileChunk(@RequestParam("filename") String filename,
                                                    @RequestParam("chunkIndex") int chunkIndex) {

        RestTemplate restTemplate = new RestTemplate();
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(fileMicroserviceUrl + "downloadFileChunk")
                .queryParam("filename", filename)
                .queryParam("chunkIndex", chunkIndex);

        ResponseEntity<Resource> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                null,
                Resource.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Future<byte[]> future = executorService.submit(() -> {
                try (InputStream inputStream = response.getBody().getInputStream()) {
                    return inputStream.readAllBytes();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            });

            try {
                byte[] chunkData = future.get();
                if (chunkData != null && chunkData.length > 0) {
                    return ResponseEntity.ok()
                            .contentType(MediaType.APPLICATION_OCTET_STREAM)
                            .body(chunkData);
                } else {
                    return ResponseEntity.noContent().build();
                }
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } else if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(response.getStatusCode()).build();
        }
    }

    private void sendChunksToMicroservice(String fileName, int totalChunks) throws IOException {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();

        File[] chunks = new File[totalChunks];
        for (int i = 0; i < totalChunks; i++) {
            File chunk = new File(uploadFolder, fileName + ".filesplitter.part_" + i);
            chunks[i] = chunk;
        }

        for (File chunk : chunks) {
            FileSystemResource fileResource = new FileSystemResource(chunk);
            parts.add("files", fileResource);
        }
        parts.add("filename", fileName);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(parts, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(fileMicroserviceUrl + "merge", requestEntity, String.class);
        System.out.println("Response from microservice: " + response.getBody());

        // DELETE LEFTOVER CHUNKS
        for (File chunk : chunks) {
            chunk.delete();
        }
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }


}
