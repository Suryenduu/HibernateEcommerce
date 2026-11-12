package com.example.dao;

import com.example.util.HibernateUtil;
import com.example.entity.Category;
import com.example.entity.Product;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductDAOTest {

    private static ProductDAO productDAO;
    private static Category testCategory;
    private static Long createdProductId;

    @BeforeAll
    static void setUp() {
        productDAO = new ProductDAO();

        // Create a dedicated test category
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            testCategory = new Category();
            testCategory.setName("TestCat_" + UUID.randomUUID());
            testCategory.setDescription("Temporary category for ProductDAO CRUD test");
            session.persist(testCategory);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw e;
        }
    }

    @AfterAll
    static void tearDown() {
        // Clean up the test category after tests finish
        if (testCategory != null && testCategory.getId() != null) {
            Transaction tx = null;
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                tx = session.beginTransaction();
                Category managed = session.find(Category.class, testCategory.getId());
                if (managed != null) {
                    session.remove(managed);
                }
                tx.commit();
            } catch (RuntimeException e) {
                if (tx != null && tx.isActive()) tx.rollback();
            }
        }
    }

    @Test
    @Order(1)
    void testCreateProduct() {
        Product product = new Product("Mechanical Keyboard", new BigDecimal("4500.00"), 15);
        product.setCategory(testCategory);

        createdProductId = productDAO.save(product);
        assertNotNull(createdProductId, "Saved product should have a generated ID");
    }

    @Test
    @Order(2)
    void testReadProduct() {
        assertNotNull(createdProductId);
        Product found = productDAO.findById(createdProductId);

        assertNotNull(found, "Product should be found by ID");
        assertEquals("Mechanical Keyboard", found.getName());
        assertEquals(0, new BigDecimal("4500.00").compareTo(found.getPrice()));
        assertEquals(15, found.getStockQuantity());

        List<Product> allProducts = productDAO.findAll();
        assertFalse(allProducts.isEmpty(), "Product list should not be empty");
    }

    @Test
    @Order(3)
    void testUpdateProduct() {
        assertNotNull(createdProductId);
        boolean updated = productDAO.update(
                createdProductId,
                "Wireless Mechanical Keyboard",
                new BigDecimal("5200.00"),
                12
        );

        assertTrue(updated, "Update should return true");

        Product updatedProduct = productDAO.findById(createdProductId);
        assertEquals("Wireless Mechanical Keyboard", updatedProduct.getName());
        assertEquals(0, new BigDecimal("5200.00").compareTo(updatedProduct.getPrice()));
        assertEquals(12, updatedProduct.getStockQuantity());
    }

    @Test
    @Order(4)
    void testDeleteProduct() {
        assertNotNull(createdProductId);
        boolean deleted = productDAO.delete(createdProductId);
        assertTrue(deleted, "Delete should return true");

        Product deletedProduct = productDAO.findById(createdProductId);
        assertNull(deletedProduct, "Deleted product should no longer exist");
    }
}