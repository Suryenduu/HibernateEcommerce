package com.example.dao;

import com.example.entity.*;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrderDAOTest {

    private static CategoryDAO categoryDAO;
    private static ProductDAO productDAO;
    private static UserDAO userDAO;
    private static OrderDAO orderDAO;

    private static Long categoryId;
    private static Long product1Id;
    private static Long product2Id;
    private static Long userId;
    private static Long createdOrderId;

    @BeforeAll
    static void setUp() {
        categoryDAO = new CategoryDAO();
        productDAO = new ProductDAO();
        userDAO = new UserDAO();
        orderDAO = new OrderDAO();

        String suffix = UUID.randomUUID().toString().substring(0, 8);

        Category category = new Category("OrderCat_" + suffix, "Category for OrderDAO tests");
        categoryId = categoryDAO.save(category);

        Product p1 = new Product("Wireless Mouse", new BigDecimal("1500.00"), 20);
        p1.setCategory(category);
        product1Id = productDAO.save(p1);

        Product p2 = new Product("USB-C Hub", new BigDecimal("2500.00"), 10);
        p2.setCategory(category);
        product2Id = productDAO.save(p2);

        Users user = new Users("buyer_" + suffix, "BuyerPass123!", "buyer_" + suffix + "@example.com", Role.CUSTOMER);
        userId = userDAO.save(user);
    }

    @AfterAll
    static void tearDown() {
        if (createdOrderId != null && orderDAO.findByIdWithDetails(createdOrderId) != null) {
            orderDAO.delete(createdOrderId);
        }
        if (product1Id != null) productDAO.delete(product1Id);
        if (product2Id != null) productDAO.delete(product2Id);
        if (categoryId != null) categoryDAO.delete(categoryId);
        if (userId != null) userDAO.delete(userId);
    }

    @Test
    @Order(1)
    void testCreateOrderWithMultipleDetails() {
        Product p1 = productDAO.findById(product1Id);
        Product p2 = productDAO.findById(product2Id);

        OrderDetails item1 = new OrderDetails(2, p1.getPrice(), p1); // 2 * 1500 = 3000
        OrderDetails item2 = new OrderDetails(1, p2.getPrice(), p2); // 1 * 2500 = 2500

        createdOrderId = orderDAO.createOrder(userId, List.of(item1, item2));
        assertNotNull(createdOrderId, "Created order should have a generated ID");

        // Verify stock deduction
        assertEquals(18, productDAO.findById(product1Id).getStockQuantity());
        assertEquals(9, productDAO.findById(product2Id).getStockQuantity());
    }

    @Test
    @Order(2)
    void testFetchOrderWithUserAndProducts() {
        assertNotNull(createdOrderId);
        Orders fetchedOrder = orderDAO.findByIdWithDetails(createdOrderId);

        assertNotNull(fetchedOrder, "Order should be fetched with details");
        assertNotNull(fetchedOrder.getUser(), "Associated user should be loaded");
        assertEquals(userId, fetchedOrder.getUser().getId());
        assertEquals(0, new BigDecimal("5500.00").compareTo(fetchedOrder.getTotalAmount()));
        assertEquals(2, fetchedOrder.getOrderDetails().size(), "Order should contain 2 line items");

        List<Orders> userOrders = orderDAO.findByUserId(userId);
        assertEquals(1, userOrders.size());
    }

    @Test
    @Order(3)
    void testUpdateOrder() {
        assertNotNull(createdOrderId);
        assertTrue(orderDAO.updateTotalAmount(createdOrderId, new BigDecimal("5000.00")));

        Orders updated = orderDAO.findByIdWithDetails(createdOrderId);
        assertEquals(0, new BigDecimal("5000.00").compareTo(updated.getTotalAmount()));
    }

    @Test
    @Order(4)
    void testDeleteOrder() {
        assertNotNull(createdOrderId);
        assertTrue(orderDAO.delete(createdOrderId));
        assertNull(orderDAO.findByIdWithDetails(createdOrderId));
        createdOrderId = null;
    }
}