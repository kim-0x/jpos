package com.jpos.sale.repository.implementation.file;

import com.jpos.sale.model.SaleItem;
import com.jpos.sale.repository.SaleItemRepository;
import utils.AbstractBinRepository;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.UUID;

public class BinSaleItemRepository extends AbstractBinRepository<SaleItem> implements SaleItemRepository {
    private final ArrayList<SaleItem> saleItems = new ArrayList<>();

    public BinSaleItemRepository() {
        this(getDefaultDatFilePath("saleitem.dat"));
    }

    public BinSaleItemRepository(Path filePath) {
        super(filePath);
        saleItems.addAll(loadFromDat());
    }

    @Override
    public void add(SaleItem saleItem) {
        if (saleItem == null) {
            throw new IllegalArgumentException("Sale item is required.");
        }
        saleItems.add(saleItem);
        persistToDat(saleItems);
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
    protected Class<SaleItem> getEntityType() {
        return SaleItem.class;
    }
}
