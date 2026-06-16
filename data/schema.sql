-- jpos SQLite Schema
-- Author: Kimleng LIM
-- Target Engine: SQLite 3

PRAGMA foreign_keys = ON;

-- Table: users
-- Primary user accounts for managing user roles: admin, manager, and cashier
CREATE TABLE IF NOT EXISTS users (
    id       TEXT PRIMARY KEY,
    username TEXT NOT NULL,
    role     TEXT NOT NULL CHECK (role IN ('Admin', 'Manager', 'Cashier')),
    password TEXT NOT NULL
);

-- Seed default admin user (password: Admin)
INSERT OR IGNORE INTO users (id, username, role, password)
VALUES ('1', 'admin', 'Admin', 'Admin');

-- Table: products
-- Product catalog for Point of Sale items
CREATE TABLE IF NOT EXISTS products (
    id               TEXT PRIMARY KEY,
    barcode          TEXT UNIQUE,
    name             TEXT NOT NULL,
    product_category TEXT NOT NULL CHECK (product_category IN ('food', 'beverage', 'household', 'fruit', 'dairy'))
);

-- Table: inventory
-- Historical record of stock items flowing in and out
CREATE TABLE IF NOT EXISTS inventory (
    id               TEXT PRIMARY KEY,
    number_in_stock  REAL,
    cost             REAL,
    product_id       TEXT,
    created_at       DATE,
    FOREIGN KEY (product_id) REFERENCES products (id)
);

-- Table: price_books
-- Historical price changes per product
CREATE TABLE IF NOT EXISTS price_books (
    product_id   TEXT,
    cost         REAL,
    margin       REAL,
    sale_price   REAL,
    effective_at DATE,
    PRIMARY KEY (product_id, effective_at),
    FOREIGN KEY (product_id) REFERENCES products (id)
);

-- Table: sale_transactions
-- Historical sale transactions
CREATE TABLE IF NOT EXISTS sale_transactions (
    transaction_id   TEXT PRIMARY KEY,
    receipt_number   TEXT,
    grand_total      REAL,
    transaction_date DATE
);

-- Table: sale_items
-- Detail line items for sale transactions
CREATE TABLE IF NOT EXISTS sale_items (
    product_id     TEXT,
    transaction_id TEXT,
    quantity       REAL,
    cost           REAL,
    price          REAL,
    PRIMARY KEY (product_id, transaction_id),
    FOREIGN KEY (product_id)     REFERENCES products (id),
    FOREIGN KEY (transaction_id) REFERENCES sale_transactions (transaction_id)
);
