package org.swen.dms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.swen.dms.entity.Document;
import org.swen.dms.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;           // BDD style
import static org.mockito.ArgumentMatchers.any;       // matchers
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web layer tests for {@link org.swen.dms.controller.DocumentController}.
 * <p>
 * Uses {@link org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest}
 * and mocks {@link org.swen.dms.service.DocumentService}.
 * Ensures REST endpoints behave correctly (HTTP status, JSON output).
 */


@WebMvcTest(DocumentController.class)
class DocumentControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    DocumentService service;

    /** Verifies that POST /documents creates a document and returns JSON with correct fields. */
    @Test
    void post_createsDocument() throws Exception {
        Document input = new Document();
        input.setTitle("Spec");
        input.setContent("Hello");

        Document saved = new Document();
        saved.setId(1L);
        saved.setTitle("Spec");
        saved.setContent("Hello");

        // Use BDDMockito (or Mockito.when if you prefer)
        given(service.create(any(Document.class))).willReturn(saved);

        mvc.perform(post("/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Spec")));
    }

    /** Verifies that GET /documents returns a list of all documents as JSON. */
    @Test
    void get_returnsAll() throws Exception {
        Document d = new Document();
        d.setId(1L); d.setTitle("T"); d.setContent("C");
        given(service.findAll()).willReturn(List.of(d));

        mvc.perform(get("/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));
    }
}
