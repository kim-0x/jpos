package com.jpos.sale.repository.implementation;

import com.jpos.sale.model.PriceBook;
import com.jpos.sale.repository.PriceBookRepository;
import utils.AbstractCsvRepository;
import utils.CsvRepositorySupport;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.UUID;

public class FilePriceBookRepository extends AbstractCsvRepository<PriceBook> implements PriceBookRepository {
    private static final String DATA_LABEL = "Price book data";
    private static final String[] HEADER = new String[] {"productId", "cost", "margin", "salePrice", "lastModified"};

    private final ArrayList<PriceBook> priceBooks = new ArrayList<>();

    public FilePriceBookRepository() {
        this(CsvRepositorySupport.getDefaultDataFilePath("pricebook.csv"));
    }

    public FilePriceBookRepository(Path filePath) {
        super(filePath);
        priceBooks.addAll(loadFromCsv());
    }

    @Override
    public void add(PriceBook priceBook) {
        if (priceBook == null) {
            throw new IllegalArgumentException("Price book entry is required.");
        }
        priceBooks.add(priceBook);
        persistToCsv(priceBooks);
    }

    @Override
    public PriceBook[] getAll() {
        return priceBooks.toArray(new PriceBook[0]);
    }

    @Override
    public PriceBook getById(UUID productId) {
        if (productId == null) {
            throw new IllegalArgumentException("Product id is required.");
        }

        PriceBook latest = null;
        for (PriceBook priceBook : priceBooks) {
            if (!productId.equals(priceBook.getProductId())) {
                continue;
            }
            if (latest == null || (priceBook.getLastModified() != null
                    && priceBook.getLastModified().after(latest.getLastModified()))) {
                latest = priceBook;
            }
        }
        return latest;
    }

    @Override
    protected String getDataLabel() {
        return DATA_LABEL;
    }

    @Override
    protected String[] getHeaderRow() {
        return HEADER;
    }

    @Override
    protected PriceBook toEntity(String[] row, int lineNumber) {
        if (row.length != 5) {
            throw new IllegalStateException(String.format("Invalid price book row at line %d.", lineNumber));
        }

        try {
            PriceBook priceBook = new PriceBook();
            priceBook.setProductId(UUID.fromString(row[0].trim()));
            priceBook.setCost(Double.parseDouble(row[1]));
            priceBook.setMargin(Float.parseFloat(row[2]));
            priceBook.setSalePrice(Double.parseDouble(row[3]));
            priceBook.setLastModified(CsvRepositorySupport.parseTimestamp(row[4]));
            return priceBook;
        } catch (RuntimeException exception) {
            throw new IllegalStateException(String.format("Invalid price book row at line %d.", lineNumber),
                    exception);
        }
    }

    @Override
    protected String[] toRow(PriceBook priceBook) {
        return new String[] {
                priceBook.getProductId().toString(),
                String.valueOf(priceBook.getCost()),
                String.valueOf(priceBook.getMargin()),
                String.valueOf(priceBook.getSalePrice()),
                CsvRepositorySupport.formatTimestamp(priceBook.getLastModified())
        };
    }
}
