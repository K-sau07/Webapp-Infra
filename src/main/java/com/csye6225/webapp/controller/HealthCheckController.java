package com.csye6225.webapp.controller;

import com.csye6225.webapp.entity.HealthCheck;
import com.csye6225.webapp.repository.HealthCheckRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/healthz")
public class HealthCheckController {

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckController.class);
    private final HealthCheckRepository healthCheckRepository;

    public HealthCheckController(HealthCheckRepository healthCheckRepository) {
        this.healthCheckRepository = healthCheckRepository;
    }

    @GetMapping
    public ResponseEntity<Void> healthCheck(HttpServletRequest request) {
        try {
            logger.info("Health check endpoint hit");

            if (request.getParameterMap() != null && !request.getParameterMap().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            if (request.getContentLengthLong() > 0) {
                return ResponseEntity.badRequest().build();
            }

            healthCheckRepository.save(new HealthCheck());

            HttpHeaders headers = new HttpHeaders();
            headers.set("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.set("Pragma", "no-cache");
            headers.set("X-Content-Type-Options", "nosniff");

            return ResponseEntity.ok().headers(headers).build();

        } catch (Exception e) {
            logger.error("Health check DB operation failed", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT,
            RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.HEAD})
    public ResponseEntity<Void> methodNotAllowed() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }
}
