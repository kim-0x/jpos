package com.jpos.seed;

import com.jpos.inventory.InventoryFacade;
import com.jpos.inventory.model.Product;
import com.jpos.inventory.model.ProductCategory;
import com.jpos.inventory.model.ProductQuery;
import com.jpos.inventory.model.StockRecord;
import com.jpos.inventory.repository.InventoryRepository;
import com.jpos.inventory.repository.ProductRepository;
import com.jpos.inventory.repository.implementation.file.BinInventoryRepository;
import com.jpos.inventory.repository.implementation.file.BinProductRepository;
import com.jpos.inventory.repository.implementation.file.CsvProductRepository;
import com.jpos.inventory.service.implementation.InventoryServiceImpl;
import com.jpos.sale.SaleFacade;
import com.jpos.sale.model.PriceBook;
import com.jpos.sale.model.SaleItemData;
import com.jpos.sale.repository.PriceBookRepository;
import com.jpos.sale.repository.SaleHeaderRepository;
import com.jpos.sale.repository.SaleItemRepository;
import com.jpos.sale.repository.implementation.file.BinPriceBookRepository;
import com.jpos.sale.repository.implementation.file.BinSaleHeaderRepository;
import com.jpos.sale.repository.implementation.file.BinSaleItemRepository;
import com.jpos.sale.service.InventoryGateway;
import com.jpos.sale.service.implementation.InventoryGatewayImpl;

import java.io.BufferedWriter;
import java.io.Console;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import utils.CsvRepositorySupport;

public final class SeedDataGenerator {
    private static final DateTimeFormatter RECEIPT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter LOG_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final float REPOSITORY_LOW_STOCK_LEVEL = 3.0f;

    private SeedDataGenerator() {
    }

    public static void main(String[] args) {
        Options options = Options.from(args);
        try {
            if (options.reset) {
                truncateDataFiles();
            }

            GeneratorContext context = buildContext();
            runSeed(context, options);
        } catch (Exception exception) {
            System.err.println("Seed generation failed: " + exception.getMessage());
            exception.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private static GeneratorContext buildContext() {
        InventoryRepository inventoryRepository = new BinInventoryRepository();
        ProductRepository productRepository = new BinProductRepository();
        InventoryFacade inventoryFacade = new InventoryFacade(inventoryRepository, productRepository);

        SaleHeaderRepository saleHeaderRepository = new BinSaleHeaderRepository();
        SaleItemRepository saleItemRepository = new BinSaleItemRepository();
        PriceBookRepository priceBookRepository = new BinPriceBookRepository();
        InventoryGateway inventoryGateway = new InventoryGatewayImpl(
                productRepository,
                new InventoryServiceImpl(inventoryRepository, productRepository));

        SaleFacade saleFacade = new SaleFacade(
                saleHeaderRepository,
                saleItemRepository,
                priceBookRepository,
                inventoryGateway);

        return new GeneratorContext(
                inventoryFacade,
                saleFacade,
                productRepository,
                inventoryRepository,
                priceBookRepository,
                saleHeaderRepository,
                saleItemRepository);
    }

    private static void runSeed(GeneratorContext context, Options options) {
        if (!options.append) {
            truncateDataFiles();
            context = buildContext();
        }

        Random random = new Random(options.randomSeed);
        Map<String, ProductState> productStateByBarcode = ensureProductCatalog(context, options, random);

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(options.months).plusDays(1);
        long txSerial = 0L;
        long createdTransactions = 0L;
        long skippedTransactions = 0L;
        long restockEvents = 0L;
        YearMonth activeMonth = null;

        System.out.printf("Starting seed generation for period %s to %s%n", startDate, endDate);

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            YearMonth processingMonth = YearMonth.from(date);
            if (!processingMonth.equals(activeMonth)) {
                activeMonth = processingMonth;
                LocalDate monthEndDate = processingMonth.atEndOfMonth().isAfter(endDate) ? endDate : processingMonth.atEndOfMonth();
                long processingDays = ChronoUnit.DAYS.between(date, monthEndDate) + 1;
                long estimatedMonthlyTransactions = Math.round(
                        processingDays * ((options.minTransactionsPerDay + options.maxTransactionsPerDay) / 2.0));
                System.out.printf("Processing month: %s%n", processingMonth);
                System.out.printf("Estimated transactions for %s: %d%n", processingMonth, estimatedMonthlyTransactions);
                applyMonthlyProductPrices(context, productStateByBarcode, options.margin);
            }

            LocalDateTime dayStartTime = LocalDateTime.now();
            long dayCreatedStart = createdTransactions;
            long daySkippedStart = skippedTransactions;
            System.out.printf("Day %s started at %s%n", date, dayStartTime.format(LOG_TIMESTAMP_FORMAT));

            if (date.getDayOfMonth() == 1) {
                restockEvents += bulkRestockMonthStart(context, productStateByBarcode, options);
            }

            int dailyTxCount = randomInRange(random, options.minTransactionsPerDay, options.maxTransactionsPerDay);
            for (int txIndex = 0; txIndex < dailyTxCount; txIndex++) {
                txSerial++;
                SaleItemData[] saleItems = buildSaleItems(context, productStateByBarcode, options, random);
                if (saleItems.length == 0) {
                    skippedTransactions++;
                    continue;
                }

                Date transactionDate = randomDateTimeInDay(random, date);
                String receiptNumber = date.format(RECEIPT_DATE_FORMAT) + "-" + String.format("%08d", txSerial);

                try {
                    context.saleFacade.processSaleTransaction(receiptNumber, saleItems, transactionDate);
                    createdTransactions++;
                } catch (Exception exception) {
                    skippedTransactions++;
                }
            }

            LocalDateTime dayEndTime = LocalDateTime.now();
            long dayDurationSeconds = Math.max(0, ChronoUnit.SECONDS.between(dayStartTime, dayEndTime));
            long dayCreatedTransactions = createdTransactions - dayCreatedStart;
            long daySkippedTransactions = skippedTransactions - daySkippedStart;
            System.out.printf(
                    "Day %s ended at %s | duration=%ds | created=%d | skipped=%d%n",
                    date,
                    dayEndTime.format(LOG_TIMESTAMP_FORMAT),
                    dayDurationSeconds,
                    dayCreatedTransactions,
                    daySkippedTransactions);

            if (dayDurationSeconds > options.dayProcessingTimeoutSeconds
                    && !shouldContinueAfterTimeout(date, dayDurationSeconds, options.dayProcessingTimeoutSeconds)) {
                System.out.println("Seed generation stopped by timeout safeguard.");
                break;
            }
        }

        System.out.println("Seed generation completed.");
        System.out.printf("Period: %s to %s%n", startDate, endDate);
        System.out.printf("Transactions created: %d%n", createdTransactions);
        System.out.printf("Transactions skipped: %d%n", skippedTransactions);
        System.out.printf("Restock events: %d%n", restockEvents);
        System.out.printf("Products in catalog: %d%n", productStateByBarcode.size());

        if (options.exportCsvDir != null && !options.exportCsvDir.isBlank()) {
            Path exportDirectory = resolveExportDirectory(options.exportCsvDir);
            System.out.printf("Starting CSV export to: %s%n", exportDirectory);
            exportCsvSnapshot(context, exportDirectory);
            System.out.printf("CSV export generated at: %s%n", exportDirectory);
        }
    }

    private static Path resolveExportDirectory(String exportCsvDir) {
        Path exportPath = Path.of(exportCsvDir);
        if (exportPath.isAbsolute()) {
            return exportPath.normalize();
        }
        return locateProjectRoot().resolve(exportPath).normalize();
    }

    private static boolean shouldContinueAfterTimeout(LocalDate processingDate, long elapsedSeconds, long timeoutSeconds) {
        System.out.printf(
                "Day %s exceeded timeout (%ds > %ds).%n",
                processingDate,
                elapsedSeconds,
                timeoutSeconds);
        Console console = System.console();
        if (console == null) {
            System.out.println("No interactive console detected. Exiting to honor timeout safeguard.");
            return false;
        }
        String userAnswer = console.readLine("Continue processing remaining days? [y/N]: ");
        return userAnswer != null
                && ("y".equalsIgnoreCase(userAnswer.trim()) || "yes".equalsIgnoreCase(userAnswer.trim()));
    }

    private static void exportCsvSnapshot(GeneratorContext context, Path exportDirectory) {
        try {
            Files.createDirectories(exportDirectory);

            writeCsvFile(
                    exportDirectory.resolve("product.csv"),
                    "id,barcode,name,category",
                    List.of(context.productRepository.getProducts()).stream()
                            .map(product -> new String[] {
                                    product.getId().toString(),
                                    product.getBarcode(),
                                    product.getName(),
                                    product.getCategory().getValue()
                            }).toList()
            );

            writeCsvFile(
                    exportDirectory.resolve("inventory.csv"),
                    "id,numberInStock,cost,productId,createdAt",
                    List.of(context.inventoryRepository.getStockItems()).stream()
                            .map(stockItem -> new String[] {
                                    stockItem.getId().toString(),
                                    String.valueOf(stockItem.getNumberInStock()),
                                    String.valueOf(stockItem.getCost()),
                                    stockItem.getProductId().toString(),
                                    CsvRepositorySupport.formatTimestamp(stockItem.getCreatedAt())
                            }).toList()
            );

            writeCsvFile(
                    exportDirectory.resolve("pricebook.csv"),
                    "productId,cost,margin,salePrice,effectiveAt",
                    List.of(context.priceBookRepository.getAll()).stream()
                            .map(priceBook -> new String[] {
                                    priceBook.getProductId().toString(),
                                    String.valueOf(priceBook.getCost()),
                                    String.valueOf(priceBook.getMargin()),
                                    String.valueOf(priceBook.getSalePrice()),
                                    CsvRepositorySupport.formatTimestamp(priceBook.getEffectiveAt())
                            }).toList()
            );

            writeCsvFile(
                    exportDirectory.resolve("saletransaction.csv"),
                    "transactionId,receiptNumber,grandTotal,transactionDate",
                    List.of(context.saleHeaderRepository.getAll()).stream()
                            .map(header -> new String[] {
                                    header.getTransactionId().toString(),
                                    header.getReceiptNumber(),
                                    String.valueOf(header.getGrandTotal()),
                                    CsvRepositorySupport.formatTimestamp(header.getTransactionDate())
                            }).toList()
            );

            writeCsvFile(
                    exportDirectory.resolve("saleitem.csv"),
                    "productId,quantity,cost,price,transactionId",
                    List.of(context.saleItemRepository.getAll()).stream()
                            .map(item -> new String[] {
                                    item.getProductId().toString(),
                                    String.valueOf(item.getQuantity()),
                                    String.valueOf(item.getCost()),
                                    String.valueOf(item.getPrice()),
                                    item.getTransactionId().toString()
                            }).toList()
            );
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to export CSV records.", exception);
        }
    }

    private static void writeCsvFile(Path outputPath, String header, List<String[]> rows) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
            writer.write(header);
            writer.newLine();
            for (String[] row : rows) {
                writer.write(toCsvLine(row));
                writer.newLine();
            }
        }
    }

    private static String toCsvLine(String[] row) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < row.length; index++) {
            if (index > 0) {
                builder.append(',');
            }
            String value = row[index] == null ? "" : row[index];
            boolean requiresQuotes = value.contains(",") || value.contains("\"") || value.contains("\n");
            if (requiresQuotes) {
                builder.append('"');
                builder.append(value.replace("\"", "\"\""));
                builder.append('"');
                continue;
            }
            builder.append(value);
        }
        return builder.toString();
    }

    private static Map<String, ProductState> ensureProductCatalog(GeneratorContext context, Options options, Random random) {
        Map<String, ProductState> productStateByBarcode = new HashMap<>();
        Product[] existingProducts = context.inventoryFacade.getProducts();
        if (existingProducts.length == 0) {
            Product[] predefinedProducts = new CsvProductRepository().getProducts();
            for (Product predefinedProduct : predefinedProducts) {
                context.productRepository.saveProduct(predefinedProduct);
            }
            existingProducts = context.inventoryFacade.getProducts();
            System.out.printf("Loaded predefined product catalog: %d products%n", existingProducts.length);
        }

        if (existingProducts.length == 0) {
            throw new IllegalStateException("No product catalog available for seed generation.");
        }

        Map<String, Float> stockByBarcode = currentStockByBarcode(context.inventoryFacade.getStockReport());
        for (Product product : existingProducts) {
            String barcode = product.getBarcode();
            double currentCost = currentCostForBarcode(context.inventoryFacade.getStockReport(), barcode);
            if (currentCost <= 0) {
                currentCost = randomCost(random);
            }

            float currentStock = stockByBarcode.getOrDefault(barcode, 0.0f);
            if (currentStock <= REPOSITORY_LOW_STOCK_LEVEL) {
                context.inventoryFacade.stockEntry(barcode, currentCost, options.monthlyOpeningStock);
                currentStock += options.monthlyOpeningStock;
            }

            productStateByBarcode.put(barcode, new ProductState(product.getId(), barcode, currentCost, currentStock));
        }

        return productStateByBarcode;
    }

    private static void applyMonthlyProductPrices(GeneratorContext context,
                                                  Map<String, ProductState> productStateByBarcode,
                                                  float margin) {
        for (ProductState productState : productStateByBarcode.values()) {
            context.saleFacade.setProductPrice(new ProductQuery(productState.productId, productState.barcode), margin);
        }
    }

    private static long bulkRestockMonthStart(GeneratorContext context,
                                              Map<String, ProductState> productStateByBarcode,
                                              Options options) {
        long restockEvents = 0L;
        for (ProductState productState : productStateByBarcode.values()) {
            context.inventoryFacade.stockEntry(productState.barcode, productState.cost, options.monthlyOpeningStock);
            productState.currentStock += options.monthlyOpeningStock;
            restockEvents++;
        }
        return restockEvents;
    }

    private static SaleItemData[] buildSaleItems(GeneratorContext context,
                                                 Map<String, ProductState> productStateByBarcode,
                                                 Options options,
                                                 Random random) {
        ArrayList<SaleItemData> items = new ArrayList<>();
        ArrayList<ProductState> products = new ArrayList<>(productStateByBarcode.values());
        int itemCount = randomInRange(random, options.minItemsPerTransaction, options.maxItemsPerTransaction);

        for (int index = 0; index < itemCount && !products.isEmpty(); index++) {
            ProductState product = products.get(random.nextInt(products.size()));
            float quantity = 1.0f;
            float projectedStock = product.currentStock - quantity;

            if (projectedStock <= REPOSITORY_LOW_STOCK_LEVEL) {
                context.inventoryFacade.stockEntry(product.barcode, product.cost, options.lowStockRestockQuantity);
                product.currentStock += options.lowStockRestockQuantity;
                projectedStock = product.currentStock - quantity;
            }

            if (projectedStock <= REPOSITORY_LOW_STOCK_LEVEL) {
                continue;
            }

            product.currentStock = projectedStock;
            items.add(new SaleItemData(product.barcode, quantity));
        }

        return items.toArray(new SaleItemData[0]);
    }

    private static Map<String, Float> currentStockByBarcode(StockRecord[] stockRecords) {
        Map<String, Float> stockByBarcode = new HashMap<>();
        for (StockRecord stockRecord : stockRecords) {
            if (stockRecord.getProduct() == null || stockRecord.getProduct().getBarcode() == null) {
                continue;
            }
            stockByBarcode.put(stockRecord.getProduct().getBarcode(), (float) stockRecord.getNumberInStock());
        }
        return stockByBarcode;
    }

    private static double currentCostForBarcode(StockRecord[] stockRecords, String barcode) {
        for (StockRecord stockRecord : stockRecords) {
            if (stockRecord.getProduct() != null && barcode.equals(stockRecord.getProduct().getBarcode())) {
                return stockRecord.getCost();
            }
        }
        return 0;
    }

    private static Date randomDateTimeInDay(Random random, LocalDate date) {
        LocalTime localTime = LocalTime.of(
                randomInRange(random, 8, 21),
                randomInRange(random, 0, 59),
                randomInRange(random, 0, 59));
        LocalDateTime localDateTime = date.atTime(localTime);
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private static int randomInRange(Random random, int min, int max) {
        if (max <= min) {
            return min;
        }
        return random.nextInt((max - min) + 1) + min;
    }

    private static float randomInRange(Random random, float min, float max) {
        if (max <= min) {
            return min;
        }
        return min + random.nextFloat() * (max - min);
    }

    private static double randomCost(Random random) {
        return randomInRange(random, 1.0f, 40.0f);
    }

    private static void truncateDataFiles() {
        Path binDirectory = locateProjectRoot()
                .resolve("data")
                .resolve("bin");

        truncateFile(binDirectory.resolve("product.dat"));
        truncateFile(binDirectory.resolve("inventory.dat"));
        truncateFile(binDirectory.resolve("pricebook.dat"));
        truncateFile(binDirectory.resolve("saleitem.dat"));
        truncateFile(binDirectory.resolve("saletransaction.dat"));
    }

    private static void truncateFile(Path filePath) {
        try {
            Files.createDirectories(filePath.getParent());
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }
            try (var ignored = Files.newOutputStream(filePath, java.nio.file.StandardOpenOption.TRUNCATE_EXISTING)) {
                // truncate existing content
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to reset seed data file: " + filePath, exception);
        }
    }

    private static Path locateProjectRoot() {
        Path startPath = Path.of("").toAbsolutePath().normalize();
        for (Path currentPath = startPath; currentPath != null; currentPath = currentPath.getParent()) {
            if (Files.isRegularFile(currentPath.resolve("pom.xml"))
                    && Files.isDirectory(currentPath.resolve("data"))) {
                return currentPath;
            }
        }
        return startPath;
    }

    private record GeneratorContext(InventoryFacade inventoryFacade,
                                    SaleFacade saleFacade,
                                    ProductRepository productRepository,
                                    InventoryRepository inventoryRepository,
                                    PriceBookRepository priceBookRepository,
                                    SaleHeaderRepository saleHeaderRepository,
                                    SaleItemRepository saleItemRepository) {
    }

    private static final class ProductState {
        private final java.util.UUID productId;
        private final String barcode;
        private final double cost;
        private float currentStock;

        private ProductState(java.util.UUID productId, String barcode, double cost, float currentStock) {
            this.productId = productId;
            this.barcode = barcode;
            this.cost = cost;
            this.currentStock = currentStock;
        }
    }

    private static final class Options {
        private int months = 3;
        private int minTransactionsPerDay = 500;
        private int maxTransactionsPerDay = 1000;
        private int productCount = 120;
        private float monthlyOpeningStock = 4000.0f;
        private float lowStockRestockQuantity = 1500.0f;
        private int minItemsPerTransaction = 1;
        private int maxItemsPerTransaction = 5;
        private float minQuantityPerItem = 1.0f;
        private float maxQuantityPerItem = 4.0f;
        private float margin = 0.25f;
        private boolean reset = false;
        private boolean append = true;
        private String exportCsvDir = null;
        private long dayProcessingTimeoutSeconds = 120L;
        private long randomSeed = System.currentTimeMillis();

        private static Options from(String[] args) {
            Options options = new Options();
            for (String arg : args) {
                if ("--reset".equals(arg)) {
                    options.reset = true;
                    options.append = false;
                    continue;
                }
                if ("--append".equals(arg)) {
                    options.append = true;
                    continue;
                }
                if (!arg.startsWith("--") || !arg.contains("=")) {
                    continue;
                }
                String[] pair = arg.substring(2).split("=", 2);
                String key = pair[0];
                String value = pair[1];
                switch (key) {
                    case "months" -> options.months = Math.max(1, Integer.parseInt(value));
                    case "minTxPerDay" -> options.minTransactionsPerDay = Math.max(1, Integer.parseInt(value));
                    case "maxTxPerDay" -> options.maxTransactionsPerDay = Math.max(options.minTransactionsPerDay, Integer.parseInt(value));
                    case "products" -> options.productCount = Math.max(1, Integer.parseInt(value));
                    case "monthlyOpeningStock" -> options.monthlyOpeningStock = Math.max(10.0f, Float.parseFloat(value));
                    case "lowStockRestock" -> options.lowStockRestockQuantity = Math.max(10.0f, Float.parseFloat(value));
                    case "margin" -> options.margin = Math.max(0.0f, Float.parseFloat(value));
                    case "seed" -> options.randomSeed = Long.parseLong(value);
                    case "dayTimeoutSeconds" -> options.dayProcessingTimeoutSeconds = Math.max(1L, Long.parseLong(value));
                    case "exportCsvDir" -> options.exportCsvDir = value;
                    default -> {
                    }
                }
            }

            if (options.maxTransactionsPerDay < options.minTransactionsPerDay) {
                options.maxTransactionsPerDay = options.minTransactionsPerDay;
            }

            return options;
        }
    }
}
