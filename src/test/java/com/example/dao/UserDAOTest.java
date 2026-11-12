package com.example.dao;

import com.example.entity.Role;
import com.example.entity.Users;
import org.junit.jupiter.api.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserDAOTest {

    private static UserDAO userDAO;
    private static Long createdUserId;
    private static String uniqueUsername;
    private static String uniqueEmail;

    @BeforeAll
    static void setUp() {
        userDAO = new UserDAO();
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        uniqueUsername = "user_" + suffix;
        uniqueEmail = "user_" + suffix + "@example.com";
    }

    @Test
    @Order(1)
    void testCreateUserWithHashedPassword() {
        Users user = new Users(uniqueUsername, "PlainSecret123!", uniqueEmail, Role.CUSTOMER);
        createdUserId = userDAO.save(user);

        assertNotNull(createdUserId, "Saved user should have a generated ID");

        Users savedUser = userDAO.findById(createdUserId);
        assertNotNull(savedUser);
        assertNotEquals("PlainSecret123!", savedUser.getPassword(), "Password must not be stored in plaintext");
        assertTrue(savedUser.getPassword().startsWith("$2a$"), "Password must be hashed with BCrypt");
    }

    @Test
    @Order(2)
    void testAuthenticateUser() {
        assertTrue(userDAO.verifyPassword(uniqueUsername, "PlainSecret123!"), "Correct password should verify");
        assertFalse(userDAO.verifyPassword(uniqueUsername, "WrongPassword"), "Incorrect password should fail");
    }

    @Test
    @Order(3)
    void testUpdateUserAndPassword() {
        assertNotNull(createdUserId);
        String newEmail = "updated_" + uniqueEmail;

        assertTrue(userDAO.update(createdUserId, newEmail, Role.ADMIN));
        Users updated = userDAO.findById(createdUserId);
        assertEquals(newEmail, updated.getEmail());
        assertEquals(Role.ADMIN, updated.getRole());

        assertTrue(userDAO.updatePassword(createdUserId, "NewSecret456!"));
        assertTrue(userDAO.verifyPassword(uniqueUsername, "NewSecret456!"));
        assertFalse(userDAO.verifyPassword(uniqueUsername, "PlainSecret123!"));
    }

    @Test
    @Order(4)
    void testDeleteUser() {
        assertNotNull(createdUserId);
        assertTrue(userDAO.delete(createdUserId));
        assertNull(userDAO.findById(createdUserId));
    }
}