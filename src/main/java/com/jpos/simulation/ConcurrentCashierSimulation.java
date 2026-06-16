package com.jpos.simulation;

import com.jpos.configuration.RepositoryConfiguration;
import com.jpos.configuration.RepositoryConfiguration.RepositoryType;
import com.jpos.configuration.RepositoryFactory;
import com.jpos.inventory.InventoryFacade;
import com.jpos.inventory.model.Product;
import com.jpos.inventory.model.ProductQuery;
import com.jpos.inventory.repository.InventoryRepository;
import com.jpos.inventory.repository.ProductRepository;
import com.jpos.inventory.repository.implementation.file.CsvProductRepository;
import com.jpos.inventory.service.implementation.InventoryServiceImpl;
import com.jpos.sale.SaleFacade;
import com.jpos.sale.model.SaleItemData;
import com.jpos.sale.service.InventoryGateway;
import com.jpos.sale.service.implementation.InventoryGatewayImpl;
import com.jpos.user.UserFacade;
import com.jpos.user.model.UserRole;
import org.sqlite.SQLiteErrorCode;
import org.sqlite.SQLiteException;
import utils.SqliteConnectionProvider;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Standalone console application that simulates multiple concurrent cashiers
 * processing sale transactions against a shared data source.
 *
 * <p>The simulation has three phases:
 * <ol>
 *   <li><strong>Setup</strong> – Admin signs in via {@link UserFacade}, creates one
 *       {@link UserRole#CASHIER} account per simulated cashier, then signs out.
 *       Products, inventory and price-book entries are seeded when missing.</li>
 *   <li><strong>Concurrent run</strong> – An {@link ExecutorService} fixed-thread-pool
 *       launches one task per cashier.  Each task signs in, processes the configured
 *       number of sale transactions via {@link SaleFacade}, then signs out.</li>
 *   <li><strong>Summary</strong> – Elapsed time, success / failure counts, retry
 *       statistics (JDBC) and throughput are printed to stdout.</li>
 * </ol>
 *
 * <p><strong>Concurrency strategy per data source:</strong>
 * <ul>
 *   <li><em>jdbc</em> – Each transaction retries automatically on
 *       {@link SQLiteException} with a {@code SQLITE_BUSY} result code (exponential
 *       back-off, configurable via {@code --maxRetries} and {@code --retryBackoffMs}).
 *       SQLite WAL mode is activated so readers never block writers.</li>
 *   <li><em>csv / bin</em> – File-based repositories perform a read-all / write-all
 *       on every operation and are <em>not</em> thread-safe.  A shared
 *       {@link ReentrantLock} serialises all {@code processSaleTransaction} calls so
 *       that file integrity is preserved.  This intentionally demonstrates that
 *       file-backed stores cannot scale horizontally.</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>
 *   java com.jpos.simulation.ConcurrentCashierSimulation
 *   java com.jpos.simulation.ConcurrentCashierSimulation --datasource=jdbc
 *   java com.jpos.simulation.ConcurrentCashierSimulation --datasource=csv  --cashiers=3 --transactions=5
 *   java com.jpos.simulation.ConcurrentCashierSimulation --datasource=bin  --cashiers=4 --transactions=2
 *   java com.jpos.simulation.ConcurrentCashierSimulation --datasource=jdbc --cashiers=10 --transactions=10 --margin=0.30
 * </pre>
 *
 * <p>All options and their defaults:
 * <pre>
 *   --datasource=csv|bin|jdbc          default: jdbc
 *   --cashiers=N                        default: 5
 *   --transactions=N                    default: 3
 *   --margin=0.25                       default: 0.25
 *   --admin-username=admin              default: admin
 *   --admin-******              default: Admin
 *   --stock=500                         default: 500  (units seeded per product)
 *   --maxRetries=10                     default: 10   (JDBC busy retries)
 *   --retryBackoffMs=50                 default: 50   (initial back-off, doubles each retry)
 * </pre>
 */
public final class ConcurrentCashierSimulation {

    private static final DateTimeFormatter LOG_TS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Low-stock guard used by {@link com.jpos.inventory.repository.implementation.jdbc.JdbcInventoryRepository}. */
    private static final float LOW_STOCK_GUARD = 3.0f;

    private ConcurrentCashierSimulation() {
    }

    // =========================================================================
    // Entry point
    // =========================================================================

    public static void main(String[] args) {
        Options options = Options.from(args);

        log("=== ConcurrentCashierSimulation ===");
        log(String.format("Data source  : %s", options.datasource));
        log(String.format("Cashiers     : %d", options.cashiers));
        log(String.format("Transactions : %d per cashier  (%d total)",
                options.transactions, options.cashiers * options.transactions));
        log(String.format("Margin       : %.0f%%", options.margin * 100));
        if (options.datasource == RepositoryType.JDBC) {
            log(String.format("Busy retries : max %d, initial back-off %dms",
                    options.maxRetries, options.retryBackoffMs));
        } else {
            log("NOTE: CSV/BIN repositories are not thread-safe. " +
                    "A shared lock will serialise all sale writes.");
        }
        log("-------------------------------------------");

        try {
            RepositoryFactory factory =
                    new RepositoryConfiguration(options.datasource).createFactory();

            // ------------------------------------------------------------------
            // Phase 0 – JDBC schema bootstrap
            // ------------------------------------------------------------------
            if (options.datasource == RepositoryType.JDBC) {
                initJdbcSchema();
            }

            // ------------------------------------------------------------------
            // Phase 1 – Admin setup: create cashier accounts and seed catalogue
            // ------------------------------------------------------------------
            List<CashierCredentials> cashiers = runSetupPhase(factory, options);

            // ------------------------------------------------------------------
            // Phase 2 – Concurrent simulation
            // ------------------------------------------------------------------
            SimulationResult result = runConcurrentPhase(factory, cashiers, options);

            // ------------------------------------------------------------------
            // Phase 3 – Summary
            // ------------------------------------------------------------------
            printSummary(result, options);

        } catch (Exception e) {
            System.err.println("[ERROR] Simulation failed: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    // =========================================================================
    // Phase 0 – JDBC schema
    // =========================================================================

    private static void initJdbcSchema() throws Exception {
        log("Initialising JDBC schema...");
        SqliteConnectionProvider cp = new SqliteConnectionProvider();
        try (Connection conn = cp.getConnection();
             Statement stmt = conn.createStatement()) {

            // WAL mode: readers never block writers, reduces SQLITE_BUSY under load.
            stmt.execute("PRAGMA journal_mode = WAL");
            stmt.execute("PRAGMA busy_timeout = 5000");

            String[] ddl = {
                "CREATE TABLE IF NOT EXISTS users ("
                    + "id TEXT PRIMARY KEY, username TEXT NOT NULL, "
                    + "role TEXT NOT NULL CHECK (role IN ('Admin','Manager','Cashier')), "
                    + "password TEXT NOT NULL)",

                "INSERT OR IGNORE INTO users (id, username, role, password) "
                    + "VALUES ('1', 'admin', 'Admin', 'Admin')",

                "CREATE TABLE IF NOT EXISTS products ("
                    + "id TEXT PRIMARY KEY, barcode TEXT UNIQUE, name TEXT NOT NULL, "
                    + "product_category TEXT NOT NULL "
                    + "CHECK (product_category IN ('food','beverage','household','fruit','dairy')))",

                "CREATE TABLE IF NOT EXISTS inventory ("
                    + "id TEXT PRIMARY KEY, number_in_stock REAL, cost REAL, "
                    + "product_id TEXT, created_at DATE, "
                    + "FOREIGN KEY (product_id) REFERENCES products(id))",

                "CREATE TABLE IF NOT EXISTS price_books ("
                    + "product_id TEXT, cost REAL, margin REAL, sale_price REAL, effective_at DATE, "
                    + "PRIMARY KEY (product_id, effective_at), "
                    + "FOREIGN KEY (product_id) REFERENCES products(id))",

                "CREATE TABLE IF NOT EXISTS sale_transactions ("
                    + "transaction_id TEXT PRIMARY KEY, receipt_number TEXT, "
                    + "grand_total REAL, transaction_date DATE)",

                "CREATE TABLE IF NOT EXISTS sale_items ("
                    + "product_id TEXT, transaction_id TEXT, quantity REAL, cost REAL, price REAL, "
                    + "PRIMARY KEY (product_id, transaction_id), "
                    + "FOREIGN KEY (product_id)     REFERENCES products(id), "
                    + "FOREIGN KEY (transaction_id) REFERENCES sale_transactions(transaction_id))"
            };

            for (String sql : ddl) {
                stmt.execute(sql);
            }
        }
        log("Schema ready.");
    }

    // =========================================================================
    // Phase 1 – Setup
    // =========================================================================

    private static List<CashierCredentials> runSetupPhase(RepositoryFactory factory,
                                                          Options options) {
        log("");
        log("--- Phase 1: Setup ---");

        // 1a. Sign in as admin via UserFacade
        UserFacade adminFacade = new UserFacade(factory.createUserRepository());
        boolean signedIn = adminFacade.signIn(options.adminUsername, options.adminPassword);
        if (!signedIn) {
            throw new IllegalStateException(
                    "Admin sign-in failed. Check --admin-username and --admin-password.");
        }
        log(String.format("Signed in as '%s' (%s).",
                options.adminUsername, adminFacade.getCurrentUserLogin().getRole()));

        // 1b. Create cashier accounts
        List<CashierCredentials> cashiers = new ArrayList<>();
        for (int i = 1; i <= options.cashiers; i++) {
            String username = String.format("cashier%02d", i);
            String password = String.format("cashier%02d", i);
            try {
                adminFacade.createNewUser(username, password, UserRole.CASHIER);
                log(String.format("  Created user: %s [%s]", username, UserRole.CASHIER));
            } catch (Exception e) {
                // User may already exist from a previous run — log and continue.
                log(String.format("  User '%s' already exists or could not be created: %s",
                        username, e.getMessage()));
            }
            cashiers.add(new CashierCredentials(i, username, password));
        }

        // 1c. Sign out admin
        adminFacade.signOut();
        log(String.format("Signed out '%s'.", options.adminUsername));

        // 1d. Ensure product catalogue, inventory and price books are seeded
        seedCatalogue(factory, options);

        log("Setup complete.");
        return cashiers;
    }

    /**
     * Ensures that at least one product with stock and a price-book entry exists.
     * Products are seeded from the predefined {@link CsvProductRepository} when
     * none are present (mirrors the behaviour of {@code SeedDataGenerator}).
     */
    private static void seedCatalogue(RepositoryFactory factory, Options options) {
        ProductRepository   productRepo   = factory.createProductRepository();
        InventoryRepository inventoryRepo = factory.createInventoryRepository();
        InventoryServiceImpl inventorySvc = new InventoryServiceImpl(inventoryRepo, productRepo);
        InventoryGateway    inventoryGw   = new InventoryGatewayImpl(productRepo, inventorySvc);
        InventoryFacade     inventoryFacade = new InventoryFacade(inventoryRepo, productRepo);

        SaleFacade saleFacade = new SaleFacade(
                factory.createSaleHeaderRepository(),
                factory.createSaleItemRepository(),
                factory.createPriceBookRepository(),
                inventoryGw);

        Product[] existing = inventoryFacade.getProducts();
        if (existing.length == 0) {
            log("  No products found — loading predefined product catalogue from CSV...");
            for (Product p : new CsvProductRepository().getProducts()) {
                productRepo.saveProduct(p);
            }
            existing = inventoryFacade.getProducts();
            log(String.format("  Loaded %d products.", existing.length));
        }

        if (existing.length == 0) {
            throw new IllegalStateException(
                    "Product catalogue is empty and could not be loaded.");
        }

        // Ensure each product has sufficient stock and a price-book entry.
        int stocked = 0;
        int priced  = 0;
        for (Product product : existing) {
            String barcode = product.getBarcode();

            // Stock: check current level; restock if at or below guard.
            float stockLevel = inventoryRepo.getStockLevelOf(
                    new ProductQuery(product.getId(), barcode));
            float needed = options.cashiers * options.transactions * 1.0f + LOW_STOCK_GUARD + 1;
            if (stockLevel <= LOW_STOCK_GUARD) {
                double cost = inventoryRepo.getProductCost(
                        new ProductQuery(product.getId(), barcode));
                if (cost <= 0) {
                    cost = 10.0;
                }
                inventoryFacade.stockEntry(barcode, cost, Math.max(options.seedStock, needed));
                stocked++;
            }

            // Price book: set if missing.
            if (saleFacade.getCurrentProductPrice(
                    new ProductQuery(product.getId(), barcode)) == null) {
                saleFacade.setProductPrice(
                        new ProductQuery(product.getId(), barcode), options.margin);
                priced++;
            }
        }

        log(String.format("  Products: %d total, %d restocked, %d priced.",
                existing.length, stocked, priced));
    }

    // =========================================================================
    // Phase 2 – Concurrent simulation
    // =========================================================================

    private static SimulationResult runConcurrentPhase(RepositoryFactory factory,
                                                       List<CashierCredentials> cashiers,
                                                       Options options) throws InterruptedException {
        log("");
        log("--- Phase 2: Concurrent simulation ---");
        log(String.format("Launching %d cashier threads...", cashiers.size()));

        // Shared lock for file-backed stores (CSV/BIN are not thread-safe).
        ReentrantLock fileLock = new ReentrantLock(true /* fair */);

        AtomicInteger totalSuccess = new AtomicInteger(0);
        AtomicInteger totalFailure = new AtomicInteger(0);
        AtomicInteger totalRetries = new AtomicInteger(0);

        List<Callable<CashierResult>> tasks = new ArrayList<>();
        for (CashierCredentials creds : cashiers) {
            tasks.add(new CashierTask(
                    creds, factory, options, fileLock,
                    totalSuccess, totalFailure, totalRetries));
        }

        long startMs = System.currentTimeMillis();

        ExecutorService executor = Executors.newFixedThreadPool(cashiers.size());
        List<Future<CashierResult>> futures;
        try {
            futures = executor.invokeAll(tasks);
        } finally {
            executor.shutdown();
        }

        long elapsedMs = System.currentTimeMillis() - startMs;

        // Collect per-cashier results and surface any errors.
        List<CashierResult> results = new ArrayList<>();
        List<String> errors        = new ArrayList<>();
        for (int i = 0; i < futures.size(); i++) {
            try {
                results.add(futures.get(i).get());
            } catch (ExecutionException ex) {
                CashierCredentials creds = cashiers.get(i);
                String msg = String.format("Cashier %s threw an unexpected error: %s",
                        creds.username(), ex.getCause().getMessage());
                errors.add(msg);
                log("[WARN] " + msg);
            }
        }

        if (!errors.isEmpty()) {
            log(String.format("[WARN] %d cashier task(s) failed unexpectedly.", errors.size()));
        }

        return new SimulationResult(
                totalSuccess.get(), totalFailure.get(), totalRetries.get(),
                elapsedMs, results);
    }

    // =========================================================================
    // Phase 3 – Summary
    // =========================================================================

    private static void printSummary(SimulationResult result, Options options) {
        int expected = options.cashiers * options.transactions;
        double elapsedSec = result.elapsedMs() / 1000.0;
        double tps = elapsedSec > 0 ? result.success() / elapsedSec : 0;

        log("");
        log("=== Simulation Summary ===");
        log(String.format("  Data source         : %s", options.datasource));
        log(String.format("  Cashiers            : %d", options.cashiers));
        log(String.format("  Transactions/cashier: %d", options.transactions));
        log(String.format("  Expected total      : %d", expected));
        log(String.format("  Successful          : %d", result.success()));
        log(String.format("  Failed              : %d", result.failure()));
        log(String.format("  JDBC busy retries   : %d", result.retries()));
        log(String.format("  Elapsed time        : %.3f s", elapsedSec));
        log(String.format("  Throughput          : %.1f txn/s", tps));
        log("-------------------------------------------");

        if (result.perCashier() != null) {
            log("  Per-cashier breakdown:");
            for (CashierResult cr : result.perCashier()) {
                log(String.format("    %-12s  success=%-3d  failed=%-3d  retries=%-3d  time=%dms",
                        cr.username(), cr.success(), cr.failure(), cr.retries(), cr.elapsedMs()));
            }
        }
        log("===========================================");
    }

    // =========================================================================
    // CashierTask
    // =========================================================================

    /**
     * A single cashier task executed inside the thread pool.
     *
     * <p>The task:
     * <ol>
     *   <li>Builds its own {@link SaleFacade} (and {@link UserFacade}) from the
     *       shared {@link RepositoryFactory} so each cashier holds independent
     *       service objects.</li>
     *   <li>Signs in with its own credentials (demonstrates the sign-in flow;
     *       note that {@code LoginServiceImpl} uses a {@code static} session field
     *       which is a known single-JVM limitation of the current implementation).</li>
     *   <li>Processes the configured number of sale transactions, retrying on
     *       {@code SQLITE_BUSY} (JDBC) or acquiring the shared file lock
     *       (CSV/BIN).</li>
     *   <li>Signs out.</li>
     * </ol>
     */
    private static final class CashierTask implements Callable<CashierResult> {

        private final CashierCredentials creds;
        private final RepositoryFactory  factory;
        private final Options            options;
        private final ReentrantLock      fileLock;   // only used for CSV/BIN
        private final AtomicInteger      sharedSuccess;
        private final AtomicInteger      sharedFailure;
        private final AtomicInteger      sharedRetries;

        CashierTask(CashierCredentials creds,
                    RepositoryFactory factory,
                    Options options,
                    ReentrantLock fileLock,
                    AtomicInteger sharedSuccess,
                    AtomicInteger sharedFailure,
                    AtomicInteger sharedRetries) {
            this.creds         = creds;
            this.factory       = factory;
            this.options       = options;
            this.fileLock      = fileLock;
            this.sharedSuccess = sharedSuccess;
            this.sharedFailure = sharedFailure;
            this.sharedRetries = sharedRetries;
        }

        @Override
        public CashierResult call() {
            int success = 0;
            int failure = 0;
            int retries = 0;
            long start  = System.currentTimeMillis();

            // Build per-cashier facades from the shared factory.
            ProductRepository    productRepo   = factory.createProductRepository();
            InventoryRepository  inventoryRepo = factory.createInventoryRepository();
            InventoryServiceImpl inventorySvc  = new InventoryServiceImpl(inventoryRepo, productRepo);
            InventoryGateway     inventoryGw   = new InventoryGatewayImpl(productRepo, inventorySvc);

            SaleFacade saleFacade = new SaleFacade(
                    factory.createSaleHeaderRepository(),
                    factory.createSaleItemRepository(),
                    factory.createPriceBookRepository(),
                    inventoryGw);

            UserFacade userFacade = new UserFacade(factory.createUserRepository());

            // Sign in (demonstrates workflow; static session field is a known limitation).
            boolean loggedIn = userFacade.signIn(creds.username(), creds.password());
            log(String.format("[%s] Sign-in %s.", creds.username(), loggedIn ? "OK" : "FAILED"));

            // Prepare a stable list of available product barcodes for this cashier.
            Product[] products = productRepo.getProducts();
            if (products.length == 0) {
                log(String.format("[%s] No products available — skipping.", creds.username()));
                userFacade.signOut();
                return new CashierResult(creds.username(), 0, options.transactions, 0,
                        System.currentTimeMillis() - start);
            }

            Random random = new Random(creds.id() * 31L + System.nanoTime());

            // Process each transaction.
            for (int t = 1; t <= options.transactions; t++) {
                String receiptNumber = String.format("C%02d-%05d-%s",
                        creds.id(), t, UUID.randomUUID().toString().substring(0, 8));
                SaleItemData[] items = buildSaleItems(products, random);

                int txRetries = processWithRetry(saleFacade, receiptNumber, items);

                if (txRetries >= 0) {
                    success++;
                    retries += txRetries;
                    sharedSuccess.incrementAndGet();
                    sharedRetries.addAndGet(txRetries);
                    log(String.format("[%s] TXN %d/%d  receipt=%-22s  OK  retries=%d",
                            creds.username(), t, options.transactions, receiptNumber, txRetries));
                } else {
                    failure++;
                    sharedFailure.incrementAndGet();
                    log(String.format("[%s] TXN %d/%d  receipt=%-22s  FAILED",
                            creds.username(), t, options.transactions, receiptNumber));
                }
            }

            userFacade.signOut();
            log(String.format("[%s] Signed out.  success=%d  failed=%d  retries=%d",
                    creds.username(), success, failure, retries));

            return new CashierResult(creds.username(), success, failure, retries,
                    System.currentTimeMillis() - start);
        }

        /**
         * Processes one sale transaction with the appropriate concurrency strategy.
         *
         * <ul>
         *   <li>JDBC: retries on {@code SQLITE_BUSY} up to {@code maxRetries} times.</li>
         *   <li>CSV/BIN: acquires a shared lock to serialise file writes.</li>
         * </ul>
         *
         * @return number of retries on success, or {@code -1} on final failure
         */
        private int processWithRetry(SaleFacade saleFacade,
                                     String receiptNumber,
                                     SaleItemData[] items) {
            if (options.datasource != RepositoryType.JDBC) {
                // File-backed repos: serialise via shared lock.
                fileLock.lock();
                try {
                    saleFacade.processSaleTransaction(receiptNumber, items);
                    return 0;
                } catch (Exception e) {
                    log(String.format("[WARN] [%s] Transaction %s failed (file store): %s",
                            creds.username(), receiptNumber, e.getMessage()));
                    return -1;
                } finally {
                    fileLock.unlock();
                }
            }

            // JDBC: exponential back-off retry on SQLITE_BUSY.
            int  attempt   = 0;
            long backoffMs = options.retryBackoffMs;
            while (true) {
                try {
                    saleFacade.processSaleTransaction(receiptNumber, items);
                    return attempt; // success; return retry count
                } catch (RuntimeException ex) {
                    SQLiteException busyEx = findSQLiteBusyException(ex);
                    if (busyEx == null) {
                        // Not a busy error — log and give up.
                        log(String.format("[WARN] [%s] Transaction %s failed: %s",
                                creds.username(), receiptNumber, ex.getMessage()));
                        return -1;
                    }

                    attempt++;
                    sharedRetries.incrementAndGet();

                    if (attempt > options.maxRetries) {
                        log(String.format("[WARN] [%s] Transaction %s abandoned after %d SQLITE_BUSY retries.",
                                creds.username(), receiptNumber, options.maxRetries));
                        return -1;
                    }

                    log(String.format("[%s] SQLITE_BUSY on %s – retry %d/%d (backoff %dms)",
                            creds.username(), receiptNumber, attempt, options.maxRetries, backoffMs));

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
         * Picks 1–3 random products from the catalogue and returns
         * {@link SaleItemData} records with quantity {@code 1.0}.
         */
        private SaleItemData[] buildSaleItems(Product[] products, Random random) {
            int count = 1 + random.nextInt(Math.min(3, products.length));
            List<SaleItemData> items = new ArrayList<>();
            List<Integer> indices    = new ArrayList<>();
            for (int i = 0; i < products.length; i++) {
                indices.add(i);
            }
            java.util.Collections.shuffle(indices, random);
            for (int i = 0; i < count; i++) {
                String barcode = products[indices.get(i)].getBarcode();
                if (barcode != null && !barcode.isBlank()) {
                    items.add(new SaleItemData(barcode, 1.0f));
                }
            }
            return items.toArray(new SaleItemData[0]);
        }

        /**
         * Walks the exception cause chain looking for a {@link SQLiteException}
         * with a {@code SQLITE_BUSY*} result code.
         */
        private static SQLiteException findSQLiteBusyException(Throwable t) {
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
    }

    // =========================================================================
    // Result types
    // =========================================================================

    private record CashierCredentials(int id, String username, String password) {}

    private record CashierResult(String username,
                                 int success,
                                 int failure,
                                 int retries,
                                 long elapsedMs) {}

    private record SimulationResult(int success,
                                    int failure,
                                    int retries,
                                    long elapsedMs,
                                    List<CashierResult> perCashier) {}

    // =========================================================================
    // Utilities
    // =========================================================================

    private static void log(String message) {
        System.out.printf("[%s] %s%n", LocalDateTime.now().format(LOG_TS), message);
    }

    // =========================================================================
    // CLI options
    // =========================================================================

    private static final class Options {
        RepositoryType datasource    = RepositoryType.JDBC;
        int            cashiers      = 5;
        int            transactions  = 3;
        float          margin        = 0.25f;
        String         adminUsername = "admin";
        String         adminPassword = "Admin";
        float          seedStock     = 500.0f;
        int            maxRetries    = 10;
        long           retryBackoffMs = 50L;

        private Options() {}

        static Options from(String[] args) {
            Options o = new Options();
            for (String arg : args) {
                if (!arg.startsWith("--") || !arg.contains("=")) {
                    continue;
                }
                String[] pair  = arg.substring(2).split("=", 2);
                String   key   = pair[0];
                String   value = pair[1];
                switch (key) {
                    case "datasource" -> o.datasource = switch (value.toLowerCase()) {
                        case "csv"  -> RepositoryType.CSV;
                        case "bin"  -> RepositoryType.BIN;
                        default     -> RepositoryType.JDBC;
                    };
                    case "cashiers"       -> o.cashiers       = Math.max(1, Integer.parseInt(value));
                    case "transactions"   -> o.transactions   = Math.max(1, Integer.parseInt(value));
                    case "margin"         -> o.margin         = Math.max(0f, Float.parseFloat(value));
                    case "admin-username" -> o.adminUsername  = value;
                    case "admin-password" -> o.adminPassword  = value;
                    case "stock"          -> o.seedStock      = Math.max(10f, Float.parseFloat(value));
                    case "maxRetries"     -> o.maxRetries     = Math.max(0, Integer.parseInt(value));
                    case "retryBackoffMs" -> o.retryBackoffMs = Math.max(0L, Long.parseLong(value));
                    default -> { /* unknown flag — ignore */ }
                }
            }
            return o;
        }
    }
}
