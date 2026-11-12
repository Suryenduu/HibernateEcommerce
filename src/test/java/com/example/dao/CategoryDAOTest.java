package com.example.dao;

import com.example.entity.Category;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CategoryDAOTest {

    private static CategoryDAO categoryDAO;
    private static Long createdCategoryId;
    private static String uniqueName;

    @BeforeAll
    static void setUp() {
        categoryDAO = new CategoryDAO();
        uniqueName = "Category_" + UUID.randomUUID();
    }

    @Test
    @Order(1)
    void testCreateCategory() {
        Category category = new Category(uniqueName, "Initial category description");
        createdCategoryId = categoryDAO.save(category);
        assertNotNull(createdCategoryId, "Saved category should have a generated ID");
    }

    @Test
    @Order(2)
    void testReadCategory() {
        assertNotNull(createdCategoryId);
        Category found = categoryDAO.findById(createdCategoryId);

        assertNotNull(found, "Category should be found by ID");
        assertEquals(uniqueName, found.getName());
        assertEquals("Initial category description", found.getDescription());

        List<Category> allCategories = categoryDAO.findAll();
        assertFalse(allCategories.isEmpty(), "Category list should not be empty");
    }

    @Test
    @Order(3)
    void testUpdateCategory() {
        assertNotNull(createdCategoryId);
        String updatedName = uniqueName + "_Updated";
        boolean updated = categoryDAO.update(createdCategoryId, updatedName, "Updated category description");

        assertTrue(updated, "Update should return true");

        Category updatedCategory = categoryDAO.findById(createdCategoryId);
        assertEquals(updatedName, updatedCategory.getName());
        assertEquals("Updated category description", updatedCategory.getDescription());
    }

    @Test
    @Order(4)
    void testDeleteCategory() {
        assertNotNull(createdCategoryId);
        boolean deleted = categoryDAO.delete(createdCategoryId);
        assertTrue(deleted, "Delete should return true");

        assertNull(categoryDAO.findById(createdCategoryId), "Deleted category should no longer exist");
    }
}