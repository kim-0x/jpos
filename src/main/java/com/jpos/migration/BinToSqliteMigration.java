package com.jpos.migration;

import com.jpos.inventory.model.Product;
import com.jpos.inventory.model.StockItem;
import com.jpos.inventory.repository.InventoryRepository;
import com.jpos.inventory.repository.ProductRepository;
import com.jpos.inventory.repository.implementation.file.BinInventoryRepository;
import com.jpos.inventory.repository.implementation.file.BinProductRepository;
import com.jpos.sale.model.PriceBook;
import com.jpos.sale.model.SaleHeader;
import com.jpos.sale.model.SaleItem;
import com.jpos.sale.repository.PriceBookRepository;
import com.jpos.sale.repository.SaleHeaderRepository;
import com.jpos.sale.repository.SaleItemRepository;
import com.jpos.sale.repository.implementation.file.BinPriceBookRepository;
import com.jpos.sale.repository.implementation.file.BinSaleHeaderRepository;
import com.jpos.sale.repository.implementation.file.BinSaleItemRepository;
import com.jpos.user.model.User;
import com.jpos.user.repository.UserRepository;
import com.jpos.user.repository.implementation.file.BinUserRepository;
import utils.SqliteConnectionProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Standalone console application that migrates all data from the binary (.dat)
 * repositories into the SQLite database.
 *
 * <p>The migration is <em>idempotent</em>: every INSERT uses {@code INSERT OR IGNORE},
 * so rows that already exist in SQLite are silently skipped.  Re-running the tool
 * any number of times is therefore safe without side effects.
 *
 * <p>Usage:
 * <pre>
 *   java com.jpos.migration.BinToSqliteMigration          # migrate, skip existing rows
 *   java com.jpos.migration.BinToSqliteMigration --reset  # wipe SQLite tables first, then migrate
 * </pre>
 */
public final class BinToSqliteMigration {

    private static final DateTimeFormatter LOG_TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private BinToSqliteMigration() {
    }

    // -------------------------------------------------------------------------
    // Entry point
    // -------------------------------------------------------------------------

    public static void main(String[] args) {
        Options options = Options.from(args);

        log("=== BinToSqliteMigration ===");
        log("Mode : " + (options.reset ? "RESET then migrate" : "incremental (INSERT OR IGNORE)"));

        SqliteConnectionProvider connectionProvider = new SqliteConnectionProvider();

        try (Connection conn = connectionProvider.getConnection()) {
            initSchema(conn);

            if (options.reset) {
                log("Resetting SQLite tables...");
                resetTables(conn);
                log("All tables cleared.");
            }

            log("Reading from binary (.dat) files and writing to SQLite...");

            int users        = migrateUsers(conn, new BinUserRepository());
            int products     = migrateProducts(conn, new BinProductRepository());
            int inventory    = migrateInventory(conn, new BinInventoryRepository());
            int priceBooks   = migratePriceBooks(conn, new BinPriceBookRepository());
            int saleHeaders  = migrateSaleHeaders(conn, new BinSaleHeaderRepository());
            int saleItems    = migrateSaleItems(conn, new BinSaleItemRepository());

            log("-------------------------------------------");
            log(String.format("  %-24s %6d row(s) inserted", "users",             users));
            log(String.format("  %-24s %6d row(s) inserted", "products",          products));
            log(String.format("  %-24s %6d row(s) inserted", "inventory",         inventory));
            log(String.format("  %-24s %6d row(s) inserted", "price_books",       priceBooks));
            log(String.format("  %-24s %6d row(s) inserted", "sale_transactions", saleHeaders));
            log(String.format("  %-24s %6d row(s) inserted", "sale_items",        saleItems));
            log("-------------------------------------------");
            log("Migration complete.");

        } catch (Exception e) {
            System.err.println("[ERROR] Migration failed: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    // -------------------------------------------------------------------------
    // Schema initialisation
    // -------------------------------------------------------------------------

    private static void initSchema(Connection conn) throws SQLException {
        log("Initialising schema (CREATE TABLE IF NOT EXISTS)...");
        String[] ddl = {
            "CREATE TABLE IF NOT EXISTS users ("
                + "id TEXT PRIMARY KEY, "
                + "username TEXT NOT NULL, "
                + "role TEXT NOT NULL CHECK (role IN ('Admin','Manager','Cashier')), "
                + "password TEXT NOT NULL)",

            "CREATE TABLE IF NOT EXISTS products ("
                + "id TEXT PRIMARY KEY, "
                + "barcode TEXT UNIQUE, "
                + "name TEXT NOT NULL, "
                + "product_category TEXT NOT NULL "
                + "CHECK (product_category IN ('food','beverage','household','fruit','dairy')))",

            "CREATE TABLE IF NOT EXISTS inventory ("
                + "id TEXT PRIMARY KEY, "
                + "number_in_stock REAL, "
                + "cost REAL, "
                + "product_id TEXT, "
                + "created_at DATE, "
                + "FOREIGN KEY (product_id) REFERENCES products (id))",

            "CREATE TABLE IF NOT EXISTS price_books ("
                + "product_id TEXT, "
                + "cost REAL, "
                + "margin REAL, "
                + "sale_price REAL, "
                + "effective_at DATE, "
                + "PRIMARY KEY (product_id, effective_at), "
                + "FOREIGN KEY (product_id) REFERENCES products (id))",

            "CREATE TABLE IF NOT EXISTS sale_transactions ("
                + "transaction_id TEXT PRIMARY KEY, "
                + "receipt_number TEXT, "
                + "grand_total REAL, "
                + "transaction_date DATE)",

            "CREATE TABLE IF NOT EXISTS sale_items ("
                + "product_id TEXT, "
                + "transaction_id TEXT, "
                + "quantity REAL, "
                + "cost REAL, "
                + "price REAL, "
                + "PRIMARY KEY (product_id, transaction_id), "
                + "FOREIGN KEY (product_id) REFERENCES products (id), "
                + "FOREIGN KEY (transaction_id) REFERENCES sale_transactions (transaction_id))"
        };

        try (var stmt = conn.createStatement()) {
            for (String sql : ddl) {
                stmt.execute(sql);
            }
        }
        log("Schema ready.");
    }

    // -------------------------------------------------------------------------
    // Reset (FK-safe delete order: children before parents)
    // -------------------------------------------------------------------------

    private static void resetTables(Connection conn) throws SQLException {
        String[] tables = {
            "sale_items",
            "sale_transactions",
            "price_books",
            "inventory",
            "products",
            "users"
        };
        try (var stmt = conn.createStatement()) {
            for (String table : tables) {
                stmt.execute("DELETE FROM " + table);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Per-table migration helpers
    // -------------------------------------------------------------------------

    private static int migrateUsers(Connection conn, UserRepository source) throws SQLException {
        User[] users = source.getUsers();
        String sql = "INSERT OR IGNORE INTO users (id, username, role, password) VALUES (?, ?, ?, ?)";
        int count = 0;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (User user : users) {
                ps.setString(1, user.getId().toString());
                ps.setString(2, user.getUsername());
                ps.setString(3, user.getRole());
                ps.setString(4, user.getPassword());
                count += ps.executeUpdate();
            }
        }
        log(String.format("  users           : %d source record(s), %d inserted", users.length, count));
        return count;
    }

    private static int migrateProducts(Connection conn, ProductRepository source) throws SQLException {
        Product[] products = source.getProducts();
        String sql = "INSERT OR IGNORE INTO products (id, barcode, name, product_category) VALUES (?, ?, ?, ?)";
        int count = 0;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Product product : products) {
                ps.setString(1, product.getId().toString());
                ps.setString(2, product.getBarcode());
                ps.setString(3, product.getName());
                ps.setString(4, product.getCategory().getValue());
                count += ps.executeUpdate();
            }
        }
        log(String.format("  products        : %d source record(s), %d inserted", products.length, count));
        return count;
    }

    private static int migrateInventory(Connection conn, InventoryRepository source) throws SQLException {
        StockItem[] items = source.getStockItems();
        String sql = "INSERT OR IGNORE INTO inventory (id, number_in_stock, cost, product_id, created_at)"
                + " VALUES (?, ?, ?, ?, ?)";
        int count = 0;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (StockItem item : items) {
                ps.setString(1, item.getId().toString());
                ps.setFloat(2,  item.getNumberInStock());
                ps.setDouble(3, item.getCost());
                ps.setString(4, item.getProductId().toString());
                ps.setLong(5,   item.getCreatedAt() != null ? item.getCreatedAt().getTime() : 0L);
                count += ps.executeUpdate();
            }
        }
        log(String.format("  inventory       : %d source record(s), %d inserted", items.length, count));
        return count;
    }

    private static int migratePriceBooks(Connection conn, PriceBookRepository source) throws SQLException {
        PriceBook[] priceBooks = source.getAll();
        String sql = "INSERT OR IGNORE INTO price_books (product_id, cost, margin, sale_price, effective_at)"
                + " VALUES (?, ?, ?, ?, ?)";
        int count = 0;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (PriceBook pb : priceBooks) {
                ps.setString(1, pb.getProductId().toString());
                ps.setDouble(2, pb.getCost());
                ps.setFloat(3,  pb.getMargin());
                ps.setDouble(4, pb.getSalePrice());
                ps.setLong(5,   pb.getEffectiveAt() != null ? pb.getEffectiveAt().getTime() : 0L);
                count += ps.executeUpdate();
            }
        }
        log(String.format("  price_books     : %d source record(s), %d inserted", priceBooks.length, count));
        return count;
    }

    private static int migrateSaleHeaders(Connection conn, SaleHeaderRepository source) throws SQLException {
        SaleHeader[] headers = source.getAll();
        String sql = "INSERT OR IGNORE INTO sale_transactions"
                + " (transaction_id, receipt_number, grand_total, transaction_date) VALUES (?, ?, ?, ?)";
        int count = 0;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (SaleHeader header : headers) {
                ps.setString(1, header.getTransactionId().toString());
                ps.setString(2, header.getReceiptNumber());
                ps.setDouble(3, header.getGrandTotal());
                ps.setLong(4,   header.getTransactionDate() != null ? header.getTransactionDate().getTime() : 0L);
                count += ps.executeUpdate();
            }
        }
        log(String.format("  sale_transactions: %d source record(s), %d inserted", headers.length, count));
        return count;
    }

    private static int migrateSaleItems(Connection conn, SaleItemRepository source) throws SQLException {
        SaleItem[] items = source.getAll();
        String sql = "INSERT OR IGNORE INTO sale_items (product_id, transaction_id, quantity, cost, price)"
                + " VALUES (?, ?, ?, ?, ?)";
        int count = 0;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (SaleItem item : items) {
                ps.setString(1, item.getProductId().toString());
                ps.setString(2, item.getTransactionId().toString());
                ps.setFloat(3,  item.getQuantity());
                ps.setDouble(4, item.getCost());
                ps.setDouble(5, item.getPrice());
                count += ps.executeUpdate();
            }
        }
        log(String.format("  sale_items      : %d source record(s), %d inserted", items.length, count));
        return count;
    }

    // -------------------------------------------------------------------------
    // Utilities
    // -------------------------------------------------------------------------

    private static void log(String message) {
        System.out.printf("[%s] %s%n", LocalDateTime.now().format(LOG_TS), message);
    }

    // -------------------------------------------------------------------------
    // CLI options
    // -------------------------------------------------------------------------

    private static final class Options {
        private boolean reset = false;

        private static Options from(String[] args) {
            Options options = new Options();
            for (String arg : args) {
                if ("--reset".equals(arg)) {
                    options.reset = true;
                }
            }
            return options;
        }
    }
}
