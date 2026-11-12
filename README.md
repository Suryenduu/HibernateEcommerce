# Hibernate E-Commerce Application

A Java-based e-commerce management system built with Hibernate ORM, Jakarta Persistence (JPA), BCrypt password hashing, and MySQL.

## Technologies Used

- **Java 17**
- **Hibernate ORM 6.6**
- **Jakarta Persistence (JPA) 3.1**
- **MySQL 8.0**
- **jBCrypt 0.4** (password hashing)
- **JUnit 5 (Jupiter)** (CRUD integration testing)
- **Apache Maven**

## Project Structure

```text
HibernateEcommerce/
├── pom.xml
├── schema.sql
├── README.md
└── src/
    ├── main/
    │   ├── java/com/example/
    │   │   ├── Main.java
    │   │   ├── TestConnection.java
    │   │   ├── dao/
    │   │   │   ├── CategoryDAO.java
    │   │   │   ├── ProductDAO.java
    │   │   │   ├── UserDAO.java
    │   │   │   └── OrderDAO.java
    │   │   ├── entity/
    │   │   │   ├── Category.java
    │   │   │   ├── Product.java
    │   │   │   ├── Users.java
    │   │   │   ├── Role.java
    │   │   │   ├── Orders.java
    │   │   │   └── OrderDetails.java
    │   │   └── util/
    │   │       └── HibernateUtil.java
    │   └── resources/
    │       └── hibernate.cfg.xml
    └── test/
        └── java/com/example/dao/
            ├── CategoryDAOTest.java
            ├── ProductDAOTest.java
            ├── UserDAOTest.java
            └── OrderDAOTest.java


Entity Relationships
Category ↔ Product: One-to-Many / Many-to-One

Users ↔ Orders: One-to-Many / Many-to-One

Orders ↔ OrderDetails: One-to-Many / Many-to-One

Product ↔ OrderDetails: One-to-Many / Many-to-One

Setup Instructions
Create the MySQL database and schema:

SQL


CREATE DATABASE ecommerce;
(Optionally import schema.sql directly, or let Hibernate auto-update the tables via hibernate.hbm2ddl.auto=update.)

Database configuration (src/main/resources/hibernate.cfg.xml):

URL: jdbc:mysql://localhost:3306/ecommerce

Username: root

Password: Provided at runtime via the mysql.password system property.

Execution Guide
1. Run JUnit CRUD Tests
Bash


mvn test "-Dtest=CategoryDAOTest,ProductDAOTest,UserDAOTest,OrderDAOTest" "-Dmysql.password=YOUR_MYSQL_PASSWORD"
2. Run the Main Demonstration Application
Bash


mvn compile exec:java "-Dexec.mainClass=com.example.Main" "-Dmysql.password=YOUR_MYSQL_PASSWORD"

---

### Final Verification & Git Push
Save `Main.java`, `schema.sql`, and `README.md`, then verify `Main.java` runs cleanly:

```powershell
mvn compile exec:java "-Dexec.mainClass=com.example.Main" "-Dmysql.password=YOUR_MYSQL_PASSWORD"
Once that outputs the Order Summary, check git status (making sure no plaintext passwords are in any tracked file) and push your changes to GitHub:

PowerShell


git status
git add .
git commit -m "Implement full CRUD DAOs, BCrypt hashing, JUnit tests, schema.sql, and README"
git push origin main
Run Main.java and paste the output if you want a final sanity check before pushing.