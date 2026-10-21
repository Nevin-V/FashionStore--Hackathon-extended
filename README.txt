
           FASHION STORE MANAGEMENT SYSTEM (JAVA SWING + MYSQL)

A desktop-based Fashion Store application built with Java Swing and MySQL

------------------------------------------------------------------------
PREREQUISITES
------------------------------------------------------------------------
1. Java Development Kit (JDK 17 or higher)
   - Verify by running: java -version and javac -version
2. MySQL Server (Running on localhost:3306)
   - MySQL Community Server OR XAMPP / WAMP with MySQL service started.

------------------------------------------------------------------------
HOW TO RUN AFTER FORKING / CLONING FROM GITHUB
------------------------------------------------------------------------

STEP 1: CLONE THE REPOSITORY
   git clone https://github.com/<your-username>/FashionStoreProject.git
   cd FashionStoreProject

STEP 2: START MYSQL SERVER
   - Make sure your MySQL service is active and running on port 3306.

STEP 3: IMPORT THE DATABASE SCHEMA
   Run schema.sql to create the database ('fashion_store'), all 4 tables,
   the default admin account, and sample products.

   Choose ONE of the following methods:
   - Option A (Terminal / Command Prompt):
       mysql -u root -p < schema.sql
       (Enter your MySQL root password when prompted)


STEP 4: CONFIGURE DATABASE CREDENTIALS
   - Open src/DB.java in any text editor or IDE.
   - Check lines 6-7:
       private static final String USER = "root";
       private static final String PASSWORD = "your_mysql_password";
   - Set PASSWORD to your local MySQL root password.
     (If using default XAMPP, keep password empty: "")

STEP 5: COMPILE AND RUN THE APPLICATION

   Method 1: Using run.bat (Windows - Recommended)
     Simply double-click run.bat
     OR run in Command Prompt:
       run.bat

   Method 2: Manually from Windows Terminal (Command Prompt / PowerShell)
     Compile:
       if not exist out mkdir out
       javac -cp ".;mysql-connector-j-8.4.0.jar" -d out src\*.java
     Run:
       java -cp ".;out;mysql-connector-j-8.4.0.jar" FashionStoreApp

   Method 3: From Linux / macOS Terminal
     Compile:
       mkdir -p out
       javac -cp ".:mysql-connector-j-8.4.0.jar" -d out src/*.java
     Run:
       java -cp ".:out:mysql-connector-j-8.4.0.jar" FashionStoreApp

------------------------------------------------------------------------
DEFAULT LOGIN CREDENTIALS
------------------------------------------------------------------------
- Administrator Login:
    Username: admin
    Password: admin123
  (Features: Add/Edit/Delete products, update order status, view inventory)

- Customer Login:
    Click "Create New Account" on the login screen to register a new user.
  (Features: Browse products, filter categories, cart, checkout)

------------------------------------------------------------------------
PROJECT STRUCTURE
------------------------------------------------------------------------
  src/
    User.java                    Abstract base class (Inheritance)
    Admin.java                   Admin subclass
    Customer.java                Customer subclass
    Product.java                 Product model class
    CartItem.java                Shopping cart item model
    OrderRecord.java             Order history record model
    Payment.java                 Payment interface (Polymorphism)
    CODPayment.java              Cash on Delivery implementation
    CardPayment.java             Credit/Debit card implementation
    PaymentFactory.java          Factory design pattern for payment
    InsufficientStockException.java Custom checked exception
    DB.java                      JDBC database connection & queries
    FashionStoreApp.java         Swing GUI application (Entry point)

  schema.sql                     Database creation & seed data script
  run.bat                        One-click compile & launch script
  mysql-connector-j-8.4.0.jar    Official MySQL JDBC driver
  .gitignore                     Excludes build binaries

========================================================================
