package org.swen.dms.service;

import io.minio.MinioClient;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.swen.dms.entity.Document;
import org.swen.dms.exception.NotFoundException;
import org.swen.dms.messaging.DocumentEventPublisher;
import org.swen.dms.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swen.dms.messaging.DocumentCreatedEvent;
import org.swen.dms.messaging.DocumentEventPublisher;
import org.swen.dms.exception.PersistenceException;

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

    public DocumentServiceImpl(DocumentRepository repo, DocumentEventPublisher publisher, MinioClient minioClient) {

        this.repo = repo;
        this.publisher = publisher;
        this.minioClient = minioClient;
    }

    @Override
    @Transactional
    public Document create(Document doc) {

        try{
            Document saved = repo.save(doc);
            //publish event (don't block request if it fails)
            publisher.publishDocumentCreated(
                    new DocumentCreatedEvent(saved.getId(), saved.getTitle(), Instant.now())
            );
            return saved;
        } catch (DataAccessException dae) {
            throw new PersistenceException("Failed to save document to database", dae);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> uploadDocuments(MultipartFile file) {
        minioClient.putObject(PutObjectArgs
                .builder()
                .bucket("user1")
                .object("Resume.pdf")
                .stream(
                        new

                                FileInputStream
                                ("/tmp/Resume.pdf")
                                .build());
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
        existing.setTitle(update.getTitle());
        existing.setContent(update.getContent());
        return repo.save(existing);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new NotFoundException("Document " + id + " not found");
        }
        repo.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Document> findByTitle(String title) {
        return repo.findByTitle(title);
    }
}