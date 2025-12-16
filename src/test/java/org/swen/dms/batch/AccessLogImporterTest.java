package org.swen.dms.batch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.swen.dms.entity.Document;
import org.swen.dms.repository.jpa.DocumentRepository;
import io.minio.MinioClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.swen.dms.service.SearchService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        properties = {
                "spring.data.elasticsearch.repositories.enabled=false"
        }
)
@ActiveProfiles("test")
class AccessLogImporterTest {

    @Autowired
    private DocumentRepository documentRepository;

    // We need to mock these to prevent the context from failing on missing external services
    @MockitoBean
    private MinioClient minioClient;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    // This injects the real importer, but we will call it manually
    @Autowired
    private AccessLogImporter importer;

    @MockitoBean
    private SearchService searchService;

    @Test
    void shouldImportXmlAndCleanupFile(@TempDir Path tempDir) throws IOException {
        // 1. Setup: Create a dummy document in H2
        Document doc = new Document();
        doc.setTitle("Test Doc");
        doc.setContentType("application/pdf");
        doc.setFileKey("dummy-minio-key");
        doc.setAccessCount(0L);
        doc = documentRepository.save(doc);
        Long docId = doc.getId();

        // 2. Setup: Create a fake XML file in the temporary test directory
        // Note: We use the dynamic tempDir provided by JUnit so we don't clutter your PC
        String xmlContent = """
                <accessLogs>
                    <entry>
                        <documentId>%d</documentId>
                        <accessCount>50</accessCount>
                    </entry>
                </accessLogs>
                """.formatted(docId);

        Path xmlFile = tempDir.resolve("access_log_test.xml");
        Files.writeString(xmlFile, xmlContent);

        // CRITICAL: We need to cheat and tell the importer to look in our @TempDir
        // Since the path is usually hardcoded in properties, we can create a new instance
        // OR easier: just reflectively set the field if you didn't make the path configurable via constructor.
        // BUT: Since your AccessLogImporter has the path in the Constructor, we can just instantiate it manually!

        // Manual instantiation for testing with a specific folder
        AccessLogImporter manualImporter = new AccessLogImporter(documentRepository, tempDir.toString());

        // 3. Act: Run the import logic immediately (no waiting for 1:00 AM)
        manualImporter.processAccessLogs();

        // 4. Assert: Database was updated
        Optional<Document> updatedDoc = documentRepository.findById(docId);
        assertThat(updatedDoc).isPresent();
        assertThat(updatedDoc.get().getAccessCount()).isEqualTo(50L);

        // 5. Assert: File was deleted
        assertThat(Files.exists(xmlFile)).isFalse();
    }
}