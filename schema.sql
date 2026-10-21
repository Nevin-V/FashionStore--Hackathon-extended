/* 
 To be run after establishing sql server.
 ithu automatically create databases and tables with dummy data along with admin nte profile.
*/

CREATE DATABASE IF NOT EXISTS fashion_store;
USE fashion_store;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('CUSTOMER','ADMIN') NOT NULL DEFAULT 'CUSTOMER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    category VARCHAR(50) NOT NULL,
    gender ENUM('MEN','WOMEN','UNISEX') NOT NULL DEFAULT 'UNISEX',
    size VARCHAR(10) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CHECK (price > 0),
    CHECK (stock >= 0)
);

CREATE TABLE IF NOT EXISTS orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    total DECIMAL(10,2) NOT NULL,
    status ENUM('PLACED','PROCESSING','SHIPPED','DELIVERED','CANCELLED') NOT NULL DEFAULT 'PLACED',
    payment_method VARCHAR(40) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NULL,
    product_name VARCHAR(120) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_items_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS cart_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE KEY uq_cart_user_product (user_id, product_id)
);

INSERT IGNORE INTO users(username, password, full_name, role)
VALUES ('admin', 'admin123', 'Store Administrator', 'ADMIN');

INSERT INTO products(name, category, gender, size, price, stock)
SELECT 'Black T-Shirt', 'TOPWEAR', 'MEN', 'M', 599.00, 20
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name='Black T-Shirt' AND size='M');
INSERT INTO products(name, category, gender, size, price, stock)
SELECT 'Blue Jeans', 'BOTTOMWEAR', 'MEN', 'L', 1299.00, 10
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name='Blue Jeans' AND size='L');
INSERT INTO products(name, category, gender, size, price, stock)
SELECT 'White Shirt', 'TOPWEAR', 'UNISEX', 'M', 799.00, 15
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name='White Shirt' AND size='M');
INSERT INTO products(name, category, gender, size, price, stock)
SELECT 'Red Dress', 'DRESSES', 'WOMEN', 'S', 1599.00, 8
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name='Red Dress' AND size='S');
INSERT INTO products(name, category, gender, size, price, stock)
SELECT 'Black Hoodie', 'TOPWEAR', 'UNISEX', 'XL', 999.00, 12
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name='Black Hoodie' AND size='XL');
