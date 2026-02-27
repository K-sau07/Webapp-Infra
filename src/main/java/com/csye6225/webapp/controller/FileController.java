package com.csye6225.webapp.controller;

import com.csye6225.webapp.entity.FileRecord;
import com.csye6225.webapp.repository.FileRepository;
import com.csye6225.webapp.service.S3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/v1/file")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);
    private final FileRepository fileRepository;
    private final S3Service s3Service;

    public FileController(FileRepository fileRepository, S3Service s3Service) {
        this.fileRepository = fileRepository;
        this.s3Service = s3Service;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> uploadFile(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body("{\"error\":\"No file provided\"}");
            }
            String fileId = UUID.randomUUID().toString();
            String fileName = file.getOriginalFilename();
            String fileKey = "uploads/" + fileId + "-" + fileName;
            String fileUrl = s3Service.uploadFile(fileKey, file);

            FileRecord record = new FileRecord();
            record.setId(fileId);
            record.setFileName(fileName);
            record.setUrl(fileUrl);
            record.setUploadDate(LocalDateTime.now());
            record.setUserId("some-user-id");

            FileRecord saved = fileRepository.save(record);
            logger.info("File uploaded successfully, id={}", fileId);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            logger.error("File upload failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"File upload failed\"}");
        }
    }

    @GetMapping
    public ResponseEntity<?> getFile(@RequestParam(required = false) String id) {
        try {
            if (id == null || id.isBlank()) {
                return ResponseEntity.badRequest().body("{\"error\":\"File ID is required\"}");
            }
            Optional<FileRecord> file = fileRepository.findById(id);
            if (file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\":\"File not found\"}");
            }
            return ResponseEntity.ok(file.get());
        } catch (Exception e) {
            logger.error("Error retrieving file id={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Internal server error\"}");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFile(@PathVariable String id) {
        try {
            Optional<FileRecord> fileOpt = fileRepository.findById(id);
            if (fileOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\":\"File not found\"}");
            }
            FileRecord file = fileOpt.get();
            String bucketName = s3Service.getBucketName();
            String awsRegion = s3Service.getAwsRegion();
            String fileKey = file.getUrl()
                    .split(bucketName + ".s3." + awsRegion + ".amazonaws.com/")[1];
            s3Service.deleteFile(fileKey);
            fileRepository.deleteById(id);
            logger.info("File deleted successfully, id={}", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("File deletion failed id={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Internal server error\"}");
        }
    }

    @RequestMapping(method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<Void> methodNotAllowed() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }
}
