CREATE DATABASE IF NOT EXISTS cafe_java;
USE cafe_java;

CREATE TABLE IF NOT EXISTS menu (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100)    NOT NULL,
    category    ENUM('makanan', 'minuman') NOT NULL,
    price       DECIMAL(10,2)   NOT NULL CHECK (price > 0),
    stock       INT             NOT NULL DEFAULT 0 CHECK (stock >= 0),
    created_at  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS orders (
    id              VARCHAR(30)     PRIMARY KEY,
    total           DECIMAL(10,2)   NOT NULL,
    payment_method  ENUM('cash', 'qris', 'debit') NOT NULL,
    payment_amount  DECIMAL(10,2)   NOT NULL,
    change_amount   DECIMAL(10,2)   NOT NULL DEFAULT 0,
    order_date      DATE            NOT NULL,
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_details (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    order_id    VARCHAR(30)     NOT NULL,
    menu_id     INT             DEFAULT NULL,
    menu_name   VARCHAR(100)    NOT NULL,
    price       DECIMAL(10,2)   NOT NULL,
    quantity    INT             NOT NULL CHECK (quantity > 0),
    subtotal    DECIMAL(10,2)   NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (menu_id) REFERENCES menu(id) ON DELETE SET NULL
);

INSERT IGNORE INTO menu (id, name, category, price, stock) VALUES
    (1, 'Nasi Goreng Spesial', 'makanan', 25000, 50),
    (2, 'Mie Goreng Jawa',     'makanan', 20000, 40),
    (3, 'Ayam Penyet',         'makanan', 28000, 30),
    (4, 'Sate Ayam (10 tusuk)','makanan', 30000, 25),
    (5, 'Kentang Goreng',      'makanan', 15000, 60),
    (6, 'Es Kopi Susu',        'minuman', 18000, 100),
    (7, 'Es Teh Manis',        'minuman',  8000, 150),
    (8, 'Jus Alpukat',         'minuman', 20000, 40),
    (9, 'Matcha Latte',        'minuman', 22000, 35),
    (10,'Air Mineral',         'minuman',  5000, 200);
