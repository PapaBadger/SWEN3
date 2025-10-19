package org.swen.dms.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.swen.dms.entity.Document;
import org.swen.dms.exception.NotFoundException;
import org.swen.dms.repository.DocumentRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link org.swen.dms.service.DocumentServiceImpl}.
 * <p>
 * Uses Mockito to mock {@link org.swen.dms.repository.DocumentRepository}.
 * Ensures business logic is correct without touching the database.
 */

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

    @Mock
    private DocumentRepository repo;

    @InjectMocks
    private DocumentServiceImpl service;

//    /** Verifies that creating a document delegates to repo.save() and returns the saved entity. */
//    @Test
//    void create_savesAndReturnsEntity() {
//        Document input = new Document();
//        input.setTitle("Spec");
//        input.setContent("Hello");
//
//        Document saved = new Document();
//        saved.setId(1L);
//        saved.setTitle("Spec");
//        saved.setContent("Hello");
//
//        when(repo.save(input)).thenReturn(saved);
//
//        Document result = service.create(input);
//
//        assertThat(result.getId()).isEqualTo(1L);
//        verify(repo).save(input);
//    }

    /**
     * Verifies that findById returns the entity when it exists.
     */
    @Test
    void findById_returnsEntity() {
        Document d = new Document();
        d.setId(42L);
        when(repo.findById(42L)).thenReturn(Optional.of(d));

        Document result = service.findById(42L);

        assertThat(result.getId()).isEqualTo(42L);
        verify(repo).findById(42L);
    }

    /**
     * Verifies that findById throws NotFoundException when the entity does not exist.
     */
    @Test
    void findById_throwsNotFound() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    /** Verifies that update changes fields and persists the updated entity. */
//    @Test
//    void update_appliesChanges() {
//        Document existing = new Document();
//        existing.setId(5L);
//        existing.setTitle("Old");
//        existing.setContent("OldC");
//
//        when(repo.findById(5L)).thenReturn(Optional.of(existing));
//        when(repo.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));
//
//        Document patch = new Document();
//        patch.setTitle("New");
//        patch.setContent("NewC");
//
//        Document updated = service.update(5L, patch);
//
//        assertThat(updated.getTitle()).isEqualTo("New");
//        assertThat(updated.getContent()).isEqualTo("NewC");
//        verify(repo).save(existing);
//    }

    /**
     * Verifies that delete removes an entity if it exists.
     */
    @Test
    void delete_existing_deletes() {
        when(repo.existsById(7L)).thenReturn(true);

        service.delete(7L);

        verify(repo).deleteById(7L);
    }

    /**
     * Verifies that delete throws NotFoundException if the entity does not exist.
     */
    @Test
    void delete_missing_throws() {
        when(repo.existsById(8L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(8L))
                .isInstanceOf(NotFoundException.class);
        verify(repo, never()).deleteById(anyLong());
    }

//    /** Verifies that findByTitle delegates to the repository method. */
//    @Test
//    void findByTitle_delegates() {
//        when(repo.findByTitle("Spec")).thenReturn(List.of(new Document()));
//        assertThat(service.findByTitle("Spec")).hasSize(1);
//        verify(repo).findByTitle("Spec");
//    }
}
