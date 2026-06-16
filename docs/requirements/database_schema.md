# Datbase Schema Design: jpos (SQLite)

**Author:** Kimleng LIM
**Target Engine:** SQLite 3

## Table: users

Primary user accounts for managing user roles: admin, manager, and cashier

- `id`: TEXT, PRIMARY KEY
- `username`: TEXT, NOT NULL
- `role`: TEXT, NOT NULL (role IN ('Admin', 'Manager', 'Cashier'))
- `password`: TEXT, NOT NULL

> **NOTE**
> Seed a default username/password: admin/Admin so that application has login user for interact with system.

## Table: products

Product catalog for Point of Sale items.

- `id`: TEXT, PRIMARY KEY
- `barcode`: TEXT, UNIQUE
- `name`: TEXT, NOT NULL
- `product_category`: TEXT NOT NULL (product_category IN ('food', 'beverage', 'household', 'fruit', 'dairy'))

## Table: inventory

Historial of stock items flow in and out.

- `id`: TEXT, PRIMARY KEY
- `number_in_stock`: REAL
- `cost`: REAL
- `product_id`: TEXT, FOREIGN KEY (product_id) REFERENCES products (id)
- `created_at`: DATE

## Table: price_books

Historial price changes of product.

- `product_id`: TEXT
- `cost`: REAL
- `margin`: REAL
- `sale_price`: REAL
- `effective_at`: DATE
- PRIMARY KEY (product_id, effective_at)

## Table: sale_items

Detail items for sale transactions

- `product_id`: TEXT, REFERENCES products (id)
- `transaction_id`: TEXT, REFERENCES sale_transactions (transaction_id)
- `quantity`: REAL
- `cost`: REAL
- `price`: REAL
- PRIMARY KEY (product_id, transaction_id)

## Table: sale_transactions

Historial sale transactions

- `transaction_id`: TEXT, PRIMARY KEY
- `receipt_number`: TEXT,
- `grand_total`: REAL,
- `transaction_date`: DATE
