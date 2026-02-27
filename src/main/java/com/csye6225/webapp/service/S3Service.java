package com.csye6225.webapp.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

@Service
public class S3Service {

    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

    private final S3Client s3Client;
    private final MeterRegistry meterRegistry;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.region}")
    private String awsRegion;

    public S3Service(S3Client s3Client, MeterRegistry meterRegistry) {
        this.s3Client = s3Client;
        this.meterRegistry = meterRegistry;
    }

    public String uploadFile(String fileKey, MultipartFile file) throws IOException {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .contentType(file.getContentType())
                    .build();
            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
            logger.info("Uploaded file to S3: {}", fileKey);
            return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, awsRegion, fileKey);
        } finally {
            sample.stop(meterRegistry.timer("s3.call.duration", "operation", "put"));
        }
    }

    public void deleteFile(String fileKey) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();
            s3Client.deleteObject(request);
            logger.info("Deleted file from S3: {}", fileKey);
        } finally {
            sample.stop(meterRegistry.timer("s3.call.duration", "operation", "delete"));
        }
    }

    public String getBucketName() { return bucketName; }
    public String getAwsRegion() { return awsRegion; }
}
