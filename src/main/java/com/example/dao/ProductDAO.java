
package com.example.dao;

import com.example.entity.Product;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.math.BigDecimal;
import java.util.List;

import com.example.util.HibernateUtil;

public class ProductDAO {

    // CREATE
    public Long save(Product product) {
        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            session.persist(product);

            transaction.commit();
            return product.getId();

        } catch (RuntimeException e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }
    }

    // READ: Find by ID
    public Product findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.find(Product.class, id);
        }
    }

    // READ: Find all products
    public List<Product> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Product p JOIN FETCH p.category ORDER BY p.id",
                    Product.class
            ).getResultList();
        }
    }

    // UPDATE
    public boolean update(Long id, String name,
                          BigDecimal price, Integer stockQuantity) {
        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Product product = session.find(Product.class, id);

            if (product == null) {
                transaction.rollback();
                return false;
            }

            product.setName(name);
            product.setPrice(price);
            product.setStockQuantity(stockQuantity);

            transaction.commit();
            return true;

        } catch (RuntimeException e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }
    }

    // DELETE
    public boolean delete(Long id) {
        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Product product = session.find(Product.class, id);

            if (product == null) {
                transaction.rollback();
                return false;
            }

            session.remove(product);

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
