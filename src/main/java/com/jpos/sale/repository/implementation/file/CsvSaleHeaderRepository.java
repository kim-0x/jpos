package com.jpos.sale.repository.implementation.file;

import com.jpos.sale.model.SaleHeader;
import com.jpos.sale.repository.SaleHeaderRepository;
import utils.AbstractCsvRepository;
import utils.CsvRepositorySupport;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.UUID;

public class CsvSaleHeaderRepository extends AbstractCsvRepository<SaleHeader> implements SaleHeaderRepository {
    private static final String DATA_LABEL = "Sale transaction data";
    private static final String[] HEADER = new String[] {"transactionId", "receiptNumber", "grandTotal", "transactionDate"};

    private final ArrayList<SaleHeader> headers = new ArrayList<>();

    public CsvSaleHeaderRepository() {
        this(CsvRepositorySupport.getDefaultCsvFilePath("saletransaction.csv"));
    }

    public CsvSaleHeaderRepository(Path filePath) {
        super(filePath);
        headers.addAll(loadFromCsv());
    }

    @Override
    public void add(SaleHeader saleHeader) {
        if (saleHeader == null) {
            throw new IllegalArgumentException("Sale header is required.");
        }
        headers.add(saleHeader);
        persistToCsv(headers);
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
    protected String getDataLabel() {
        return DATA_LABEL;
    }

    @Override
    protected String[] getHeaderRow() {
        return HEADER;
    }

    @Override
    protected SaleHeader toEntity(String[] row, int lineNumber) {
        if (row.length != 4) {
            throw new IllegalStateException(String.format("Invalid sale header row at line %d.", lineNumber));
        }

        try {
            return new SaleHeader(
                    UUID.fromString(row[0].trim()),
                    row[1].trim(),
                    Double.parseDouble(row[2]),
                    CsvRepositorySupport.parseTimestamp(row[3])
            );
        } catch (RuntimeException exception) {
            throw new IllegalStateException(String.format("Invalid sale header row at line %d.", lineNumber),
                    exception);
        }
    }

    @Override
    protected String[] toRow(SaleHeader header) {
        return new String[] {
                header.getTransactionId().toString(),
                header.getReceiptNumber(),
                String.valueOf(header.getGrandTotal()),
                CsvRepositorySupport.formatTimestamp(header.getTransactionDate())
        };
    }
}
