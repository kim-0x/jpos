package com.jpos.sale.repository.implementation;

import com.jpos.sale.model.SaleItem;
import com.jpos.sale.repository.SaleItemRepository;
import utils.AbstractCsvRepository;
import utils.CsvRepositorySupport;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.UUID;

public class FileSaleItemRepository extends AbstractCsvRepository<SaleItem> implements SaleItemRepository {
    private static final String DATA_LABEL = "Sale item data";
    private static final String[] HEADER = new String[] {"productId", "quantity", "price", "transactionId"};

    private final ArrayList<SaleItem> saleItems = new ArrayList<>();

    public FileSaleItemRepository() {
        this(CsvRepositorySupport.getDefaultCsvFilePath("saleitem.csv"));
    }

    public FileSaleItemRepository(Path filePath) {
        super(filePath);
        saleItems.addAll(loadFromCsv());
    }

    @Override
    public void add(SaleItem saleItem) {
        if (saleItem == null) {
            throw new IllegalArgumentException("Sale item is required.");
        }
        saleItems.add(saleItem);
        persistToCsv(saleItems);
    }

    @Override
    public SaleItem[] getAll() {
        return saleItems.toArray(new SaleItem[0]);
    }

    @Override
    public SaleItem[] getByTransactionId(UUID transactionId) {
        if (transactionId == null) {
            throw new IllegalArgumentException("Transaction id is required.");
        }

        ArrayList<SaleItem> matchedItems = new ArrayList<>();
        for (SaleItem saleItem : saleItems) {
            if (transactionId.equals(saleItem.getTransactionId())) {
                matchedItems.add(saleItem);
            }
        }

        return matchedItems.toArray(new SaleItem[0]);
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
    protected SaleItem toEntity(String[] row, int lineNumber) {
        if (row.length != 4) {
            throw new IllegalStateException(String.format("Invalid sale item row at line %d.", lineNumber));
        }

        try {
            return new SaleItem(
                    UUID.fromString(row[0].trim()),
                    Float.parseFloat(row[1]),
                    Double.parseDouble(row[2]),
                    UUID.fromString(row[3].trim())
            );
        } catch (RuntimeException exception) {
            throw new IllegalStateException(String.format("Invalid sale item row at line %d.", lineNumber), exception);
        }
    }

    @Override
    protected String[] toRow(SaleItem saleItem) {
        return new String[] {
                saleItem.getProductId().toString(),
                String.valueOf(saleItem.getQuantity()),
                String.valueOf(saleItem.getPrice()),
                saleItem.getTransactionId().toString()
        };
    }
}
