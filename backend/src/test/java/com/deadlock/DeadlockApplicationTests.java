package com.deadlock;

import com.deadlock.service.StorageService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.services.s3.S3Client;

@SpringBootTest
@ActiveProfiles("test")
class DeadlockApplicationTests {

    @MockBean
    private S3Client s3Client;

    @MockBean
    private StorageService storageService;

    @Test
    void contextLoads() {
    }
}
