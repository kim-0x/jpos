package com.jpos.sale.repository.implementation.file;

import com.jpos.sale.model.SaleHeader;
import com.jpos.sale.repository.SaleHeaderRepository;
import utils.AbstractBinRepository;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.UUID;

public class BinSaleHeaderRepository extends AbstractBinRepository<SaleHeader> implements SaleHeaderRepository {
    private final ArrayList<SaleHeader> headers = new ArrayList<>();

    public BinSaleHeaderRepository() {
        this(getDefaultDatFilePath("saletransaction.dat"));
    }

    public BinSaleHeaderRepository(Path filePath) {
        super(filePath);
        headers.addAll(loadFromDat());
    }

    @Override
    public void add(SaleHeader saleHeader) {
        if (saleHeader == null) {
            throw new IllegalArgumentException("Sale header is required.");
        }
        headers.add(saleHeader);
        persistToDat(headers);
    }

    @Override
    public SaleHeader[] getAll() {
        return headers.toArray(new SaleHeader[0]);
    }

    @Override
    public SaleHeader getById(UUID transactionId) {
        if (transactionId == null) {
            throw new IllegalArgumentException("Transaction id is required.");
        }

        for (SaleHeader header : headers) {
            if (transactionId.equals(header.getTransactionId())) {
                return header;
            }
        }
        return null;
    }

    @Override
    protected Class<SaleHeader> getEntityType() {
        return SaleHeader.class;
    }
}
