package com.example.dao;

import com.example.entity.OrderDetails;
import com.example.entity.Orders;
import com.example.entity.Product;
import com.example.entity.Users;
import com.example.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDAO {

    // CREATE: Create an Order with multiple OrderDetails for a User
    public Long createOrder(Long userId, List<OrderDetails> items) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Users managedUser = session.find(Users.class, userId);
            if (managedUser == null) {
                throw new IllegalArgumentException("User not found with ID: " + userId);
            }

            Orders order = new Orders();
            order.setOrderDate(LocalDateTime.now());
            order.setUser(managedUser);

            BigDecimal calculatedTotal = BigDecimal.ZERO;

            for (OrderDetails item : items) {
                Product managedProduct = session.find(Product.class, item.getProduct().getId());
                if (managedProduct == null) {
                    throw new IllegalArgumentException("Product not found with ID: " + item.getProduct().getId());
                }

                if (managedProduct.getStockQuantity() != null) {
                    if (managedProduct.getStockQuantity() < item.getQuantity()) {
                        throw new IllegalStateException("Insufficient stock for product: " + managedProduct.getName());
                    }
                    managedProduct.setStockQuantity(managedProduct.getStockQuantity() - item.getQuantity());
                }

                BigDecimal unitPrice = item.getUnitPrice() != null
                        ? item.getUnitPrice()
                        : managedProduct.getPrice();

                OrderDetails detail = new OrderDetails(item.getQuantity(), unitPrice, managedProduct);
                order.addOrderDetail(detail);

                calculatedTotal = calculatedTotal.add(
                        unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()))
                );
            }

            order.setTotalAmount(calculatedTotal);
            session.persist(order);

            transaction.commit();
            return order.getId();
        } catch (RuntimeException e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }
    }

    // READ: Fetch Order by ID along with associated User, OrderDetails, and Products
    public Orders findByIdWithDetails(Long orderId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT DISTINCT o FROM Orders o " +
                    "JOIN FETCH o.user " +
                    "LEFT JOIN FETCH o.orderDetails od " +
                    "LEFT JOIN FETCH od.product p " +
                    "LEFT JOIN FETCH p.category " +
                    "WHERE o.id = :orderId",
                    Orders.class
            ).setParameter("orderId", orderId)
             .uniqueResult();
        }
    }

    // READ: Fetch all Orders for a specific User
    public List<Orders> findByUserId(Long userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT DISTINCT o FROM Orders o " +
                    "JOIN FETCH o.user u " +
                    "LEFT JOIN FETCH o.orderDetails od " +
                    "LEFT JOIN FETCH od.product " +
                    "WHERE u.id = :userId " +
                    "ORDER BY o.orderDate DESC",
                    Orders.class
            ).setParameter("userId", userId)
             .getResultList();
        }
    }

    // UPDATE: Update order total amount
    public boolean updateTotalAmount(Long orderId, BigDecimal newTotal) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Orders order = session.find(Orders.class, orderId);
            if (order == null) {
                transaction.rollback();
                return false;
            }
            order.setTotalAmount(newTotal);
            transaction.commit();
            return true;
        } catch (RuntimeException e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }
    }

    // DELETE: Delete Order (cascades to OrderDetails)
    public boolean delete(Long orderId) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Orders order = session.find(Orders.class, orderId);
            if (order == null) {
                transaction.rollback();
                return false;
            }
            session.remove(order);
            transaction.commit();
            return true;
        } catch (RuntimeException e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }
    }
}