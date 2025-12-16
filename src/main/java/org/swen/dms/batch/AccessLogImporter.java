package org.swen.dms.batch;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swen.dms.batch.xml.AccessLogEntry;
import org.swen.dms.batch.xml.AccessLogs;
import org.swen.dms.entity.Document;
import org.swen.dms.repository.jpa.DocumentRepository;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class AccessLogImporter {

    private static final Logger log = LoggerFactory.getLogger(AccessLogImporter.class);

    private final DocumentRepository documentRepository;
    private final String inputFolderPath;

    public AccessLogImporter(DocumentRepository documentRepository,
                             @Value("${dms.batch.input-folder:./input}") String inputFolderPath) {
        this.documentRepository = documentRepository;
        this.inputFolderPath = inputFolderPath;
    }

    /**
     * Runs every day at 01:00 AM.
     * Cron format: Second, Minute, Hour, Day, Month, Weekday
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void processAccessLogs() {
        log.info("Batch process started: Checking for XML files in {}", inputFolderPath);

        try {
            Path folder = Paths.get(inputFolderPath);
            if (!Files.exists(folder)) {
                Files.createDirectories(folder);
            }

            try (Stream<Path> files = Files.list(folder)) {
                files.filter(path -> path.toString().endsWith(".xml"))
                        .forEach(this::processFile);
            }

        } catch (Exception e) {
            log.error("Batch process failed", e);
        }
    }

    private void processFile(Path filePath) {
        log.info("Processing file: {}", filePath.getFileName());
        try {
            JAXBContext context = JAXBContext.newInstance(AccessLogs.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            // 1. Parse XML
            AccessLogs logs = (AccessLogs) unmarshaller.unmarshal(filePath.toFile());

            if (logs.getEntries() != null) {
                // 2. Update Database
                for (AccessLogEntry entry : logs.getEntries()) {
                    updateDocumentAccessCount(entry);
                }
            }

            // 3. Cleanup (Delete file to prevent re-processing)
            Files.delete(filePath);
            log.info("Successfully processed and deleted: {}", filePath.getFileName());

        } catch (Exception e) {
            log.error("Failed to process file: {}", filePath, e);
        }
    }

    private void updateDocumentAccessCount(AccessLogEntry entry) {
        Optional<Document> docOpt = documentRepository.findById(entry.getDocumentId());
        if (docOpt.isPresent()) {
            Document doc = docOpt.get();
            doc.setAccessCount(entry.getAccessCount());
            documentRepository.save(doc);
        } else {
            log.warn("Document ID {} not found, skipping access log update.", entry.getDocumentId());
        }
    }
}