package com.example;

import com.example.dao.CategoryDAO;
import com.example.dao.OrderDAO;
import com.example.dao.ProductDAO;
import com.example.dao.UserDAO;
import com.example.entity.*;
import com.example.util.HibernateUtil;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class Main {

    public static void main(String[] args) {
        CategoryDAO categoryDAO = new CategoryDAO();
        ProductDAO productDAO = new ProductDAO();
        UserDAO userDAO = new UserDAO();
        OrderDAO orderDAO = new OrderDAO();

        try {
            String suffix = UUID.randomUUID().toString().substring(0, 6);

            // 1. Insert a new Category
            Category category = new Category("Accessories_" + suffix, "Computer peripherals and accessories");
            Long categoryId = categoryDAO.save(category);
            System.out.println("Created Category ID: " + categoryId);

            // 2. Insert new Products
            Product keyboard = new Product("RGB Mechanical Keyboard", new BigDecimal("4500.00"), 25);
            keyboard.setCategory(category);
            Long keyboardId = productDAO.save(keyboard);

            Product mouse = new Product("Ergonomic Wireless Mouse", new BigDecimal("1800.00"), 40);
            mouse.setCategory(category);
            Long mouseId = productDAO.save(mouse);

            // 3. Insert a new User (password is automatically hashed with BCrypt)
            Users customer = new Users(
                    "suryendu_" + suffix,
                    "SecurePass@2026",
                    "suryendu_" + suffix + "@example.com",
                    Role.CUSTOMER
            );
            Long userId = userDAO.save(customer);
            System.out.println("Created User ID: " + userId + " | Password Verified: "
                    + userDAO.verifyPassword(customer.getUsername(), "SecurePass@2026"));

            // 4. Create an Order with multiple OrderDetails
            Product savedKeyboard = productDAO.findById(keyboardId);
            Product savedMouse = productDAO.findById(mouseId);

            OrderDetails item1 = new OrderDetails(1, savedKeyboard.getPrice(), savedKeyboard);
            OrderDetails item2 = new OrderDetails(2, savedMouse.getPrice(), savedMouse);

            Long orderId = orderDAO.createOrder(userId, List.of(item1, item2));

            // 5. Fetch Order along with associated User and Products
            Orders order = orderDAO.findByIdWithDetails(orderId);

            System.out.println("\n========== ORDER SUMMARY ==========");
            System.out.println("Order ID   : " + order.getId());
            System.out.println("Order Date : " + order.getOrderDate());
            System.out.println("Customer   : " + order.getUser().getUsername() + " (" + order.getUser().getRole() + ")");
            System.out.println("Email      : " + order.getUser().getEmail());
            System.out.println("Total      : INR " + order.getTotalAmount());
            System.out.println("Items:");

            for (OrderDetails detail : order.getOrderDetails()) {
                System.out.println("  - " + detail.getProduct().getName()
                        + " | Qty: " + detail.getQuantity()
                        + " | Unit Price: INR " + detail.getUnitPrice());
            }
            System.out.println("===================================");

        } finally {
            HibernateUtil.shutdown();
        }
    }
}