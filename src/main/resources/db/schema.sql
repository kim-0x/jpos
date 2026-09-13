CREATE TABLE IF NOT EXISTS users (
    id       TEXT PRIMARY KEY,
    username TEXT NOT NULL UNIQUE,
    role     TEXT NOT NULL CHECK (role IN ('Admin', 'Manager', 'Cashier')),
    password TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS products (
    id               TEXT PRIMARY KEY,
    barcode          TEXT UNIQUE,
    name             TEXT NOT NULL,
    product_category TEXT NOT NULL CHECK (product_category IN ('food', 'beverage', 'household', 'fruit', 'dairy'))
);

CREATE TABLE IF NOT EXISTS inventory (
    id               TEXT PRIMARY KEY,
    number_in_stock  REAL NOT NULL,
    cost             REAL NOT NULL,
    product_id       TEXT NOT NULL,
    created_at       INTEGER NOT NULL,
    FOREIGN KEY (product_id) REFERENCES products (id)
);

CREATE TABLE IF NOT EXISTS price_books (
    product_id   TEXT NOT NULL,
    cost         REAL NOT NULL,
    margin       REAL NOT NULL,
    sale_price   REAL NOT NULL,
    effective_at INTEGER NOT NULL,
    PRIMARY KEY (product_id, effective_at),
    FOREIGN KEY (product_id) REFERENCES products (id)
);

CREATE TABLE IF NOT EXISTS sale_transactions (
    transaction_id   TEXT PRIMARY KEY,
    receipt_number   TEXT NOT NULL,
    grand_total      REAL NOT NULL,
    transaction_date INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS sale_items (
    product_id     TEXT NOT NULL,
    transaction_id TEXT NOT NULL,
    quantity       REAL NOT NULL,
    cost           REAL NOT NULL,
    price          REAL NOT NULL,
    PRIMARY KEY (product_id, transaction_id),
    FOREIGN KEY (product_id) REFERENCES products (id),
    FOREIGN KEY (transaction_id) REFERENCES sale_transactions (transaction_id)
);
