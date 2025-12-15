package org.swen.dms;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

// Mocks for external services so we don't need real MinIO/RabbitMQ running for the test
import io.minio.MinioClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class DocumentUploadIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // We mock these because we only want to test OUR logic,
    // not whether MinIO or RabbitMQ are actually running.
    @MockitoBean
    private MinioClient minioClient;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @Test
    public void shouldUploadDocumentSuccessfully() throws Exception {
        // 1. Create a fake PDF file
        MockMultipartFile fakePdf = new MockMultipartFile(
                "file",                 // parameter name in Controller
                "test-document.pdf",    // original filename
                MediaType.APPLICATION_PDF_VALUE,
                "Fake PDF Content".getBytes()
        );

        // 2. Perform the POST request (simulating curl/Postman)
        mockMvc.perform(multipart("/api/documents/upload")
                        .file(fakePdf)
                        .param("title", "Integration Test Doc")
                        .param("category", "Testing"))
                // 3. Verify we get a 200 OK or 201 Created response
                .andExpect(status().isOk()); // Change to isCreated() if your API returns 201
    }
}