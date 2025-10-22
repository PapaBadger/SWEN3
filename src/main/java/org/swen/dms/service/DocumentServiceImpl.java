package org.swen.dms.service;

import io.minio.*;
import io.minio.errors.*;
import org.springframework.dao.DataAccessException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.swen.dms.entity.Document;
import org.swen.dms.exception.NotFoundException;
import org.swen.dms.messaging.DocumentEventPublisher;
import org.swen.dms.messaging.DocumentUpdatedEvent;
import org.swen.dms.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swen.dms.messaging.DocumentCreatedEvent;
import org.swen.dms.exception.PersistenceException;
import org.swen.dms.helper.GenerateFileKey;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.time.Instant;

/**
 * Service layer implementation for managing {@link org.swen.dms.entity.Document} entities.
 *
 * Handles persistence operations and integrates with the messaging layer.
 * On successful creation, a {@link DocumentCreatedEvent} is published to RabbitMQ.
 *
 * Catches {@link org.springframework.dao.DataAccessException} and wraps it
 * in a {@link org.swen.dms.exception.PersistenceException} to separate database logic
 * from higher-level business logic.
 */

@Service
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository repo;
    private final DocumentEventPublisher publisher;
    private final MinioClient minioClient;

    GenerateFileKey generateFileKey = new GenerateFileKey();

//    //just testing sth EDIT THIS TESTDOC EVERY TIME YOU DOCKER COMPOSE!!!!
//    Document testDoc = new Document(
//            null,
//            "Testdokument",
//            "docs/test123avdd.pdf",
//            "application/pdf",
//            12345L,
//            LocalDateTime.now()
//    );



    public DocumentServiceImpl(DocumentRepository repo, DocumentEventPublisher publisher, MinioClient minioClient) {

        this.repo = repo;
        this.publisher = publisher;
        this.minioClient = minioClient;
    }

    @Override
    @Transactional
    public ResponseEntity<?> uploadDocument(MultipartFile file, String documentTitle) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Nothing uploaded.");
            }

            if (!"application/pdf".equalsIgnoreCase(file.getContentType())) {
                return ResponseEntity.badRequest().body("Only PDFs allowed!");
            }

            //no collisions in names
            int count = 1;
            while (existsByTitle(documentTitle + ".pdf")) {
                documentTitle = documentTitle + " (" + count++ + ")";
            }

            if(!documentTitle.endsWith(".pdf")) {
                documentTitle = documentTitle + ".pdf";
            }
            String fileKey = generateFileKey.generateFileKey();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket("documents")
                            .object(fileKey)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType("application/pdf")
                            .build()
            );
                Document doc = new Document();
                doc.setTitle(documentTitle != null && !documentTitle.isBlank()
                        ? documentTitle
                        : file.getOriginalFilename());
                doc.setFileKey(fileKey);
                doc.setContentType(file.getContentType());
                doc.setFileSize(file.getSize());
                doc.setUploadedAt(LocalDateTime.now());

                Document saved = repo.save(doc);

                publisher.publishDocumentCreated(
                        new DocumentCreatedEvent(saved.getId(), saved.getTitle(), Instant.now(), "documents", saved.getFileKey())
                );

                return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Error during upload: " + e.getMessage());
        }
    }

    public ResponseEntity<byte[]> downloadDocument(Long id) {
        try {
            Document doc = repo.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Document not found: " + id));

            GetObjectResponse response = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket("documents")
                            .object(doc.getFileKey())
                            .build()
            );

            byte[] fileBytes = response.readAllBytes();

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(doc.getContentType()))
                    .header("Content-Disposition", "attachment; filename=\"" + doc.getTitle() + "\"")
                    .body(fileBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(("Error during download: " + e.getMessage()).getBytes());
        }
    }


    @Override
    public boolean existsByTitle(String title) {
        return repo.existsByTitle(title);
    }


    @Override
    @Transactional(readOnly = true)
    public List<Document> findAll() {
        return repo.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Document findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Document " + id + " not found"));
    }

    @Override
    @Transactional
    public Document update(Long id, Document update) {
        Document existing = findById(id);
        String titleBefore = existing.getTitle();
        existing.setTitle(update.getTitle());
        //publishing
        publisher.publishDocumentUpdated(
                new DocumentUpdatedEvent(id, titleBefore, update.getTitle(), Instant.now() )
                // Changed from update.getId() to id
        );
        return repo.save(existing);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Document doc = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Document " + id + " not found"));

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket("documents")
                            .object(doc.getTitle())
                            .build()
            );

            repo.deleteById(id);

        } catch (Exception e) {
            throw new RuntimeException("Error deleting document from MinIO: " + e.getMessage(), e);
        }
    }

}