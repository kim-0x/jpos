package com.jpos.sale.repository.implementation;

import com.jpos.sale.model.PriceBook;
import com.jpos.sale.repository.implementation.file.CsvPriceBookRepository;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FilePriceBookRepositoryTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    // ---------------------------------------------------------------------------
    // Test 1: Two different product prices → two distinct product ids in getAll()
    // ---------------------------------------------------------------------------

    @Test
    public void shouldVerifyTwoDistinctProductIdsWhenSavingTwoDifferentProductPrices() throws Exception {
        File priceBookFile = createEmptyPriceBookFile("pricebook.csv");
        CsvPriceBookRepository repository = new CsvPriceBookRepository(priceBookFile.toPath());

        UUID productIdA = UUID.randomUUID();
        UUID productIdB = UUID.randomUUID();

        repository.add(buildPriceBook(productIdA, 10.0, 0.2f, 12.0, daysAgo(2)));
        repository.add(buildPriceBook(productIdB, 20.0, 0.3f, 26.0, daysAgo(1)));

        CsvPriceBookRepository reloadedRepository = new CsvPriceBookRepository(priceBookFile.toPath());
        PriceBook[] allEntries = reloadedRepository.getAll();

        assertEquals(2, allEntries.length);

        Set<UUID> distinctProductIds = Arrays.stream(allEntries)
                .map(PriceBook::getProductId)
                .collect(Collectors.toSet());

        assertEquals(2, distinctProductIds.size());
    }

    // ---------------------------------------------------------------------------
    // Test 2: Same product id saved twice → two records sharing one product id
    // ---------------------------------------------------------------------------

    @Test
    public void shouldVerifyTwoRecordsWithSingleProductIdWhenSavingSameProductPriceTwice() throws Exception {
        File priceBookFile = createEmptyPriceBookFile("pricebook.csv");
        CsvPriceBookRepository repository = new CsvPriceBookRepository(priceBookFile.toPath());

        UUID sharedProductId = UUID.randomUUID();

        repository.add(buildPriceBook(sharedProductId, 10.0, 0.2f, 12.0, daysAgo(2)));
        repository.add(buildPriceBook(sharedProductId, 11.0, 0.2f, 13.2, daysAgo(1)));

        CsvPriceBookRepository reloadedRepository = new CsvPriceBookRepository(priceBookFile.toPath());
        PriceBook[] allEntries = reloadedRepository.getAll();

        assertEquals(2, allEntries.length);

        long matchingCount = Arrays.stream(allEntries)
                .filter(pb -> sharedProductId.equals(pb.getProductId()))
                .count();

        assertEquals(2, matchingCount);
    }

    // ---------------------------------------------------------------------------
    // Test 3: getById() returns the record with the latest effectiveAt date
    // ---------------------------------------------------------------------------

    @Test
    public void shouldReturnLatestPriceByEffectiveAtDateWhenProductIdIsDuplicated() throws Exception {
        File priceBookFile = createEmptyPriceBookFile("pricebook.csv");
        CsvPriceBookRepository repository = new CsvPriceBookRepository(priceBookFile.toPath());

        UUID sharedProductId = UUID.randomUUID();
        Date olderDate  = daysAgo(3);
        Date newerDate  = daysAgo(1);

        repository.add(buildPriceBook(sharedProductId, 10.0, 0.2f, 12.0, olderDate));
        repository.add(buildPriceBook(sharedProductId, 15.0, 0.25f, 18.75, newerDate));

        CsvPriceBookRepository reloadedRepository = new CsvPriceBookRepository(priceBookFile.toPath());
        PriceBook latest = reloadedRepository.getById(sharedProductId);

        assertNotNull(latest);
        assertEquals(15.0, latest.getCost(), 0.0);
        assertEquals(18.75, latest.getSalePrice(), 0.0);
        assertEquals(newerDate.getTime(), latest.getEffectiveAt().getTime());
    }

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

    private PriceBook buildPriceBook(UUID productId, double cost, float margin,
                                     double salePrice, Date effectiveAt) {
        return new PriceBook(productId, cost, margin, salePrice, effectiveAt);
    }

    /**
     * Returns a Date that is {@code days} days before the current moment,
     * with milliseconds truncated to zero so it survives the CSV timestamp
     * formatter (which records microseconds but not sub-second parts beyond
     * that).
     */
    private Date daysAgo(int days) {
        long millis = System.currentTimeMillis() - (long) days * 24 * 60 * 60 * 1000;
        // Truncate to whole seconds to survive round-trip through the formatter
        return new Date((millis / 1000) * 1000);
    }

    private File createEmptyPriceBookFile(String fileName) throws Exception {
        File file = temporaryFolder.newFile(fileName);
        Files.writeString(file.toPath(), "productId,cost,margin,salePrice,effectiveAt\n",
                StandardCharsets.UTF_8);
        return file;
    }
}
