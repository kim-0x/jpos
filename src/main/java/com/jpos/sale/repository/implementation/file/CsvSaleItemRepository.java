package com.jpos.sale.repository.implementation.file;

import com.jpos.sale.model.SaleItem;
import com.jpos.sale.repository.SaleItemRepository;
import utils.AbstractCsvRepository;
import utils.CsvRepositorySupport;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.UUID;

public class CsvSaleItemRepository extends AbstractCsvRepository<SaleItem> implements SaleItemRepository {
    private static final String DATA_LABEL = "Sale item data";
    private static final String[] HEADER = new String[] {"productId", "quantity", "cost", "price", "transactionId"};

    private final ArrayList<SaleItem> saleItems = new ArrayList<>();

    public CsvSaleItemRepository() {
        this(CsvRepositorySupport.getDefaultCsvFilePath("saleitem.csv"));
    }

    public CsvSaleItemRepository(Path filePath) {
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
        if (row.length != 5) {
            throw new IllegalStateException(String.format("Invalid sale item row at line %d.", lineNumber));
        }

        try {
            return new SaleItem(
                    UUID.fromString(row[0].trim()),
                    Float.parseFloat(row[1]),
                    Double.parseDouble(row[2]),
                    Double.parseDouble(row[3]),
                    UUID.fromString(row[4].trim())
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
                String.valueOf(saleItem.getCost()),
                String.valueOf(saleItem.getPrice()),
                saleItem.getTransactionId().toString()
        };
    }
}
