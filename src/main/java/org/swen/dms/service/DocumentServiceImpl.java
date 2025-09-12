package org.swen.dms.service;

import org.swen.dms.entity.Document;
import org.swen.dms.exception.NotFoundException;
import org.swen.dms.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Concrete implementation of the {@link DocumentService}.
 * <p>
 * Encapsulates business logic for creating, reading, updating, and deleting documents.
 * Uses {@link DocumentRepository} for persistence operations. Annotated with
 * {@link Service} to be detected as a Spring-managed service component.
 */

@Service
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository repo;

    public DocumentServiceImpl(DocumentRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public Document create(Document doc) {
        return repo.save(doc);
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