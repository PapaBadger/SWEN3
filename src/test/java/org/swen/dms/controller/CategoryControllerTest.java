package org.swen.dms.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.swen.dms.entity.Category;
import org.swen.dms.service.CategoryService;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CategoryController.class,
        excludeAutoConfiguration = {RabbitAutoConfiguration.class, ElasticsearchDataAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CategoryService service;

    private Category createCategory(Long id, String name) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        return category;
    }

    /**
     * 1. Test Listing (Happy Path)
     */
    @Test
    void listCategories_Success() throws Exception {
        List<Category> categories = List.of(
                createCategory(1L, "Invoice"),
                createCategory(2L, "Personal")
        );

        when(service.getAllCategories()).thenReturn(categories);

        mvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Invoice"))
                .andExpect(jsonPath("$[1].name").value("Personal"));
    }

    /**
     * 2. Test Listing (Empty Database)
     */
    @Test
    void listCategories_Empty() throws Exception {
        when(service.getAllCategories()).thenReturn(List.of());

        mvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    /**
     * 3. Test Creation (Happy Path)
     * FIX: Added 'isNull()' because the service expects (name, description)
     */
    @Test
    void createCategory_Success() throws Exception {
        Category newCat = createCategory(5L, "Work");

        // Service expects (String, String), so we must mock (String, null)
        when(service.createCategory(eq("Work"), isNull())).thenReturn(newCat);

        mvc.perform(post("/api/categories")
                        .param("name", "Work")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.name").value("Work"));
    }

    /**
     * 4. Test Creation Validation (Missing Parameter)
     */
    @Test
    void createCategory_MissingName() throws Exception {
        mvc.perform(post("/api/categories"))
                .andExpect(result -> assertTrue(
                        result.getResolvedException() instanceof MissingServletRequestParameterException
                ));
    }

    /**
     * 5. Test Assignment
     */
    @Test
    void assignCategory_Success() throws Exception {
        Long catId = 1L;
        Long docId = 29L;

        mvc.perform(post("/api/categories/" + catId + "/assign/" + docId))
                .andExpect(status().isOk());

        verify(service).assignCategoryToDoc(catId, docId);
    }

    /**
     * 6. Test Duplicate/Conflict (Service Exception)
     * FIX: Added 'isNull()' to match the service signature
     */
    @Test
    void createCategory_Duplicate() throws Exception {
        // Service expects (String, String), so we must mock (String, null)
        when(service.createCategory(eq("Duplicate"), isNull()))
                .thenThrow(new IllegalArgumentException("Category exists"));

        mvc.perform(post("/api/categories")
                        .param("name", "Duplicate"))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertTrue(
                        result.getResolvedException() instanceof IllegalArgumentException
                ));
    }
}