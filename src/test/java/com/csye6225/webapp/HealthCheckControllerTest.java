package com.csye6225.webapp;

import com.csye6225.webapp.repository.HealthCheckRepository;
import com.csye6225.webapp.service.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.web.servlet.MockMvc;
import software.amazon.awssdk.services.s3.S3Client;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class HealthCheckControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @SpyBean
    private HealthCheckRepository healthCheckRepository;

    // Mock S3Client so Spring context loads without AWS credentials
    @MockBean
    private S3Client s3Client;

    @BeforeEach
    void setUp() {
        healthCheckRepository.deleteAll();
        reset(healthCheckRepository);
    }

    @Test
    void shouldReturn200OnGetHealthz() throws Exception {
        mockMvc.perform(get("/healthz"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-cache, no-store, must-revalidate"))
                .andExpect(header().string("Pragma", "no-cache"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    void shouldReturn400WithQueryParams() throws Exception {
        mockMvc.perform(get("/healthz").param("key", "value"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn405ForNonGetMethods() throws Exception {
        mockMvc.perform(post("/healthz")).andExpect(status().isMethodNotAllowed());
        mockMvc.perform(put("/healthz")).andExpect(status().isMethodNotAllowed());
    }

    @Test
    void shouldReturn503WhenDbFails() throws Exception {
        doThrow(new RuntimeException("DB Error")).when(healthCheckRepository).save(any());
        mockMvc.perform(get("/healthz")).andExpect(status().isServiceUnavailable());
    }
}
