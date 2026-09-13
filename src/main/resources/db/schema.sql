CREATE TABLE IF NOT EXISTS users (
    id       TEXT PRIMARY KEY,
    username TEXT NOT NULL UNIQUE,
    role     TEXT NOT NULL CHECK (role IN ('Admin', 'Manager', 'Cashier')),
    password TEXT NOT NULL
);

INSERT OR IGNORE INTO users (id, username, role, password)
VALUES ('00000000-0000-0000-0000-000000000001', 'admin', 'Admin', 'admin');

CREATE TABLE IF NOT EXISTS products (
    id               TEXT PRIMARY KEY,
    barcode          TEXT UNIQUE,
    name             TEXT NOT NULL,
    product_category TEXT NOT NULL CHECK (product_category IN ('food', 'beverage', 'household', 'fruit', 'dairy'))
);

CREATE TABLE IF NOT EXISTS inventory (
    id               TEXT PRIMARY KEY,
    number_in_stock  REAL,
    cost             REAL,
    product_id       TEXT,
    created_at       INTEGER,
    FOREIGN KEY (product_id) REFERENCES products (id)
);

CREATE TABLE IF NOT EXISTS price_books (
    product_id   TEXT,
    cost         REAL,
    margin       REAL,
    sale_price   REAL,
    effective_at INTEGER,
    PRIMARY KEY (product_id, effective_at),
    FOREIGN KEY (product_id) REFERENCES products (id)
);

CREATE TABLE IF NOT EXISTS sale_transactions (
    transaction_id   TEXT PRIMARY KEY,
    receipt_number   TEXT,
    grand_total      REAL,
    transaction_date INTEGER
);

CREATE TABLE IF NOT EXISTS sale_items (
    product_id     TEXT,
    transaction_id TEXT,
    quantity       REAL,
    cost           REAL,
    price          REAL,
    PRIMARY KEY (product_id, transaction_id),
    FOREIGN KEY (product_id) REFERENCES products (id),
    FOREIGN KEY (transaction_id) REFERENCES sale_transactions (transaction_id)
);
