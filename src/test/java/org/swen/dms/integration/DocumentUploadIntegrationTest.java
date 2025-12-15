package org.swen.dms.integration;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // <--- NEW IMPORT
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.swen.dms.entity.Document;
import org.swen.dms.repository.jpa.DocumentRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DocumentUploadIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DocumentRepository documentRepository;

    @MockitoBean
    private MinioClient minioClient;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @Test
    void shouldUploadAndPersistDocument() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "integration-test.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "Hello World Content".getBytes()
        );

        // FIX: Adjusted URL to match Controller (@RequestMapping + @PostMapping path)
        mockMvc.perform(multipart("/api/documents/upload")
                        .file(file)
                        .param("title", "Integration Test Doc")
                        .param("category", "Testing"))
                .andExpect(status().isOk());

        // Verify Database State
        // Note: We check for ".pdf" because your Service logic adds the extension automatically
        Document savedDoc = documentRepository.findAll().stream()
                .filter(d -> d.getTitle().equals("Integration Test Doc.pdf"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Document not found in DB"));

        assertThat(savedDoc.getFileKey()).isNotNull();
        assertThat(savedDoc.getContentType()).isEqualTo("application/pdf");

        // Verify MinIO Interaction
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }
}