package com.jpos.sale;

import com.jpos.inventory.model.Product;
import com.jpos.inventory.model.ProductCategory;
import com.jpos.inventory.model.ProductQuery;
import com.jpos.inventory.repository.implementation.jdbc.JdbcInventoryRepository;
import com.jpos.inventory.repository.implementation.jdbc.JdbcProductRepository;
import com.jpos.inventory.service.implementation.InventoryServiceImpl;
import com.jpos.sale.model.SaleItemData;
import com.jpos.sale.repository.implementation.jdbc.JdbcPriceBookRepository;
import com.jpos.sale.repository.implementation.jdbc.JdbcSaleHeaderRepository;
import com.jpos.sale.repository.implementation.jdbc.JdbcSaleItemRepository;
import com.jpos.sale.service.implementation.InventoryGatewayImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sqlite.SQLiteErrorCode;
import org.sqlite.SQLiteException;
import utils.SqliteConnectionProvider;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Stress test that simulates multiple cashiers concurrently processing sale
 * transactions against a shared SQLite database.
 *
 * <p>SQLite allows only one writer at a time, so concurrent writes will trigger
 * {@link SQLiteException} with {@link SQLiteErrorCode#SQLITE_BUSY}. Each cashier
 * task retries automatically up to {@value #MAX_RETRIES} times with a short
 * back-off before giving up.
 */
public class ConcurrentCashierStressTest {

    // -------------------------------------------------------------------------
    // Configuration
    // -------------------------------------------------------------------------

    /** Number of concurrent cashier threads. */
    private static final int CASHIER_COUNT = 10;

    /** Number of sale transactions each cashier will attempt. */
    private static final int TRANSACTIONS_PER_CASHIER = 5;

    /** Maximum retry attempts when a SQLITE_BUSY is encountered. */
    private static final int MAX_RETRIES = 10;

    /** Initial back-off in milliseconds; doubles on each retry (exponential). */
    private static final long INITIAL_BACKOFF_MS = 50;

    // -------------------------------------------------------------------------
    // Product catalogue used by all cashiers
    // -------------------------------------------------------------------------

    private static final String BARCODE_APPLE  = "BC-APPLE";
    private static final String BARCODE_MILK   = "BC-MILK";
    private static final String BARCODE_BREAD  = "BC-BREAD";

    /** Stock seeded per product — must be high enough for all concurrent sales. */
    private static final float SEED_STOCK = 1_000f;

    /** Cost and margin for price-book seeding. */
    private static final double PRODUCT_COST   = 10.0;
    private static final float  PRODUCT_MARGIN = 0.20f;

    // -------------------------------------------------------------------------
    // Per-test state
    // -------------------------------------------------------------------------

    private Path             tempDbFile;
    private SqliteConnectionProvider connectionProvider;

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @Before
    public void setUp() throws Exception {
        tempDbFile         = Files.createTempFile("jpos-stress-", ".db");
        connectionProvider = new SqliteConnectionProvider(tempDbFile);

        try (Connection conn = connectionProvider.getConnection();
             Statement  stmt = conn.createStatement()) {

            // Enable WAL mode so concurrent readers and a single writer
            // can operate simultaneously, reducing BUSY contention.
            stmt.execute("PRAGMA journal_mode = WAL");
            stmt.execute("PRAGMA busy_timeout = 5000");

            createSchema(stmt);
        }

        seedProducts();
        seedInventory();
        seedPriceBooks();
    }

    @After
    public void tearDown() throws Exception {
        if (tempDbFile != null) {
            Files.deleteIfExists(tempDbFile);
            Files.deleteIfExists(tempDbFile.resolveSibling(tempDbFile.getFileName() + "-wal"));
            Files.deleteIfExists(tempDbFile.resolveSibling(tempDbFile.getFileName() + "-shm"));
        }
    }

    // -------------------------------------------------------------------------
    // Stress test
    // -------------------------------------------------------------------------

    @Test
    public void shouldProcessAllTransactionsWhenConcurrentCashiersRunSimultaneously()
            throws InterruptedException {

        int expectedTransactions = CASHIER_COUNT * TRANSACTIONS_PER_CASHIER;

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger busyRetries  = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(CASHIER_COUNT);

        try {
            List<Callable<Void>> tasks = new ArrayList<>();

            for (int cashierId = 1; cashierId <= CASHIER_COUNT; cashierId++) {
                final int id = cashierId;
                tasks.add(() -> {
                    SaleFacade facade = buildSaleFacade();

                    for (int txn = 1; txn <= TRANSACTIONS_PER_CASHIER; txn++) {
                        String receiptNumber = String.format("C%02d-TXN%03d", id, txn);
                        SaleItemData[] items = buildSaleItems(id);

                        processWithRetry(facade, receiptNumber, items, busyRetries);
                        successCount.incrementAndGet();
                    }

                    return null;
                });
            }

            List<Future<Void>> futures = executor.invokeAll(tasks);
            collectErrors(futures);

        } finally {
            executor.shutdown();
        }

        System.out.printf(
                "[StressTest] Completed %d / %d transactions; SQLITE_BUSY retries: %d%n",
                successCount.get(), expectedTransactions, busyRetries.get());

        assertEquals(
                "All cashier transactions must complete successfully",
                expectedTransactions, successCount.get());

        long recordedTransactions;
        try {
            recordedTransactions = countSaleTransactions();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to query sale_transactions count", ex);
        }
        assertEquals(
                "Every committed transaction must be persisted in the database",
                expectedTransactions, recordedTransactions);
    }

    // -------------------------------------------------------------------------
    // Retry helper
    // -------------------------------------------------------------------------

    /**
     * Processes a single sale transaction, retrying on {@link SQLiteException}
     * with {@link SQLiteErrorCode#SQLITE_BUSY} up to {@value #MAX_RETRIES} times.
     *
     * <p>A short exponential back-off is applied between retries to give the
     * current writer time to release the database lock.
     *
     * @param facade        the {@link SaleFacade} instance for the calling cashier
     * @param receiptNumber unique receipt identifier for this transaction
     * @param items         the line items to sell
     * @param busyRetries   shared counter incremented on each BUSY retry
     */
    private void processWithRetry(SaleFacade facade,
                                  String receiptNumber,
                                  SaleItemData[] items,
                                  AtomicInteger busyRetries) {
        int    attempt   = 0;
        long   backoffMs = INITIAL_BACKOFF_MS;

        while (true) {
            try {
                facade.processSaleTransaction(receiptNumber, items);
                return;
            } catch (RuntimeException ex) {
                SQLiteException sqliteEx = findSQLiteBusyException(ex);

                if (sqliteEx == null) {
                    // Not a busy error — propagate immediately.
                    throw ex;
                }

                attempt++;
                busyRetries.incrementAndGet();

                if (attempt > MAX_RETRIES) {
                    throw new RuntimeException(
                            String.format("Receipt %s failed after %d retries due to SQLITE_BUSY",
                                    receiptNumber, MAX_RETRIES),
                            sqliteEx);
                }

                System.out.printf(
                        "[StressTest] SQLITE_BUSY on receipt %s – retry %d/%d (backoff %dms)%n",
                        receiptNumber, attempt, MAX_RETRIES, backoffMs);

                try {
                    Thread.sleep(backoffMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during SQLITE_BUSY back-off", ie);
                }

                backoffMs = Math.min(backoffMs * 2, 2_000);
            }
        }
    }

    /**
     * Walks the exception cause chain looking for a {@link SQLiteException}
     * whose result code indicates {@code SQLITE_BUSY}.
     *
     * @return the {@link SQLiteException} if found, otherwise {@code null}
     */
    private SQLiteException findSQLiteBusyException(Throwable t) {
        while (t != null) {
            if (t instanceof SQLiteException sqlEx) {
                SQLiteErrorCode code = sqlEx.getResultCode();
                if (code == SQLiteErrorCode.SQLITE_BUSY
                        || code == SQLiteErrorCode.SQLITE_BUSY_RECOVERY
                        || code == SQLiteErrorCode.SQLITE_BUSY_SNAPSHOT
                        || code == SQLiteErrorCode.SQLITE_BUSY_TIMEOUT) {
                    return sqlEx;
                }
            }
            t = t.getCause();
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Fixture helpers
    // -------------------------------------------------------------------------

    private SaleFacade buildSaleFacade() {
        JdbcProductRepository   productRepo   = new JdbcProductRepository(connectionProvider);
        JdbcInventoryRepository inventoryRepo = new JdbcInventoryRepository(connectionProvider);
        InventoryServiceImpl    inventorySvc  = new InventoryServiceImpl(inventoryRepo, productRepo);
        InventoryGatewayImpl    inventoryGw   = new InventoryGatewayImpl(productRepo, inventorySvc);

        return new SaleFacade(
                new JdbcSaleHeaderRepository(connectionProvider),
                new JdbcSaleItemRepository(connectionProvider),
                new JdbcPriceBookRepository(connectionProvider),
                inventoryGw
        );
    }

    /**
     * Returns a small, deterministic basket for the given cashier.
     * Each cashier buys a different product so that they don't compete for
     * the same inventory row and cause stock-level conflicts.
     */
    private SaleItemData[] buildSaleItems(int cashierId) {
        String barcode = switch (cashierId % 3) {
            case 1  -> BARCODE_APPLE;
            case 2  -> BARCODE_MILK;
            default -> BARCODE_BREAD;
        };
        return new SaleItemData[]{ new SaleItemData(barcode, 1.0f) };
    }

    // -------------------------------------------------------------------------
    // Schema & seed helpers
    // -------------------------------------------------------------------------

    private void createSchema(Statement stmt) throws Exception {
        stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS products ("
                        + "id TEXT PRIMARY KEY, barcode TEXT UNIQUE, "
                        + "name TEXT NOT NULL, product_category TEXT NOT NULL)");

        stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS inventory ("
                        + "id TEXT PRIMARY KEY, number_in_stock REAL, cost REAL, "
                        + "product_id TEXT, created_at DATE, "
                        + "FOREIGN KEY (product_id) REFERENCES products(id))");

        stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS price_books ("
                        + "product_id TEXT, cost REAL, margin REAL, sale_price REAL, effective_at DATE, "
                        + "PRIMARY KEY (product_id, effective_at), "
                        + "FOREIGN KEY (product_id) REFERENCES products(id))");

        stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS sale_transactions ("
                        + "transaction_id TEXT PRIMARY KEY, receipt_number TEXT, "
                        + "grand_total REAL, transaction_date DATE)");

        stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS sale_items ("
                        + "product_id TEXT, transaction_id TEXT, quantity REAL, cost REAL, price REAL, "
                        + "PRIMARY KEY (product_id, transaction_id), "
                        + "FOREIGN KEY (product_id)     REFERENCES products(id), "
                        + "FOREIGN KEY (transaction_id) REFERENCES sale_transactions(transaction_id))");
    }

    private void seedProducts() throws Exception {
        JdbcProductRepository productRepo = new JdbcProductRepository(connectionProvider);

        productRepo.saveProduct(newProduct(BARCODE_APPLE, "Apple",      ProductCategory.FRUIT));
        productRepo.saveProduct(newProduct(BARCODE_MILK,  "Milk",       ProductCategory.DAIRY));
        productRepo.saveProduct(newProduct(BARCODE_BREAD, "Bread",      ProductCategory.FOOD));
    }

    private void seedInventory() {
        JdbcProductRepository   productRepo   = new JdbcProductRepository(connectionProvider);
        JdbcInventoryRepository inventoryRepo = new JdbcInventoryRepository(connectionProvider);
        InventoryServiceImpl    inventorySvc  = new InventoryServiceImpl(inventoryRepo, productRepo);

        inventorySvc.entryStock(BARCODE_APPLE, PRODUCT_COST, SEED_STOCK);
        inventorySvc.entryStock(BARCODE_MILK,  PRODUCT_COST, SEED_STOCK);
        inventorySvc.entryStock(BARCODE_BREAD, PRODUCT_COST, SEED_STOCK);
    }

    private void seedPriceBooks() {
        JdbcProductRepository   productRepo   = new JdbcProductRepository(connectionProvider);
        JdbcInventoryRepository inventoryRepo = new JdbcInventoryRepository(connectionProvider);
        InventoryServiceImpl    inventorySvc  = new InventoryServiceImpl(inventoryRepo, productRepo);
        InventoryGatewayImpl    inventoryGw   = new InventoryGatewayImpl(productRepo, inventorySvc);

        SaleFacade facade = new SaleFacade(
                new JdbcSaleHeaderRepository(connectionProvider),
                new JdbcSaleItemRepository(connectionProvider),
                new JdbcPriceBookRepository(connectionProvider),
                inventoryGw
        );

        for (String barcode : new String[]{ BARCODE_APPLE, BARCODE_MILK, BARCODE_BREAD }) {
            Product product = productRepo.getProductBy(new ProductQuery(null, barcode));
            facade.setProductPrice(
                    new com.jpos.inventory.model.ProductQuery(product.getId(), barcode),
                    PRODUCT_MARGIN);
        }
    }

    private Product newProduct(String barcode, String name, ProductCategory category) {
        Product p = new Product();
        p.setId(UUID.randomUUID());
        p.setBarcode(barcode);
        p.setName(name);
        p.setCategory(category);
        return p;
    }

    // -------------------------------------------------------------------------
    // Verification helper
    // -------------------------------------------------------------------------

    private long countSaleTransactions() throws Exception {
        try (Connection conn = connectionProvider.getConnection();
             Statement  stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT COUNT(*) FROM sale_transactions")) {
            return rs.next() ? rs.getLong(1) : 0L;
        }
    }

    // -------------------------------------------------------------------------
    // Future error collector
    // -------------------------------------------------------------------------

    private void collectErrors(List<Future<Void>> futures) {
        List<String> errors = new ArrayList<>();
        for (int i = 0; i < futures.size(); i++) {
            try {
                futures.get(i).get();
            } catch (ExecutionException ex) {
                errors.add(String.format("Cashier %d failed: %s", i + 1, ex.getCause().getMessage()));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                errors.add(String.format("Cashier %d was interrupted", i + 1));
            }
        }
        if (!errors.isEmpty()) {
            fail("One or more cashier tasks failed:\n" + String.join("\n", errors));
        }
    }
}
