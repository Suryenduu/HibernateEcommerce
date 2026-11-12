package com.example.dao;

import com.example.entity.Role;
import com.example.entity.Users;
import com.example.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

public class UserDAO {

    // CREATE (hashes password with BCrypt if not already hashed)
    public Long save(Users user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            String rawPassword = user.getPassword();
            if (rawPassword != null && !rawPassword.startsWith("$2a$") && !rawPassword.startsWith("$2b$")) {
                user.setPassword(BCrypt.hashpw(rawPassword, BCrypt.gensalt(12)));
            }

            session.persist(user);
            transaction.commit();
            return user.getId();
        } catch (RuntimeException e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }
    }

    // READ: Find by ID
    public Users findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.find(Users.class, id);
        }
    }

    // READ: Find by username
    public Users findByUsername(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Users u WHERE u.username = :username", Users.class)
                    .setParameter("username", username)
                    .uniqueResult();
        }
    }

    // READ: Find all users
    public List<Users> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Users u ORDER BY u.id", Users.class)
                    .getResultList();
        }
    }

    // AUTHENTICATE: Verify plaintext password against BCrypt hash
    public boolean verifyPassword(String username, String plainPassword) {
        Users user = findByUsername(username);
        if (user == null || plainPassword == null) {
            return false;
        }
        return BCrypt.checkpw(plainPassword, user.getPassword());
    }

    // UPDATE: Email and Role
    public boolean update(Long id, String email, Role role) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Users user = session.find(Users.class, id);
            if (user == null) {
                transaction.rollback();
                return false;
            }
            user.setEmail(email);
            user.setRole(role);
            transaction.commit();
            return true;
        } catch (RuntimeException e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }
    }

    // UPDATE: Password (hashes new password with BCrypt)
    public boolean updatePassword(Long id, String newPlainPassword) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Users user = session.find(Users.class, id);
            if (user == null) {
                transaction.rollback();
                return false;
            }
            user.setPassword(BCrypt.hashpw(newPlainPassword, BCrypt.gensalt(12)));
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
            Users user = session.find(Users.class, id);
            if (user == null) {
                transaction.rollback();
                return false;
            }
            session.remove(user);
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