package com.jpos.sale.service.implementation;

import com.jpos.sale.model.SaleHeader;
import com.jpos.sale.model.SaleItem;
import com.jpos.sale.model.SaleTransaction;
import com.jpos.sale.repository.SaleHeaderRepository;
import com.jpos.sale.repository.SaleItemRepository;
import com.jpos.sale.service.SaleTransactionService;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class SaleTransactionServiceImpl implements SaleTransactionService {
    private final SaleHeaderRepository saleHeaderRepository;
    private final SaleItemRepository saleItemRepository;

    public SaleTransactionServiceImpl(SaleHeaderRepository saleHeaderRepository, SaleItemRepository saleItemRepository) {
        this.saleHeaderRepository = Objects.requireNonNull(saleHeaderRepository, "saleHeaderRepository must not be null");
        this.saleItemRepository = Objects.requireNonNull(saleItemRepository, "saleItemRepository must not be null");
    }

    @Override
    public SaleTransaction createTransaction(String receiptNumber) {
        return createTransaction(receiptNumber, new Date());
    }

    @Override
    public SaleTransaction createTransaction(String receiptNumber, Date transactionDate) {
        Objects.requireNonNull(receiptNumber, "receiptNumber must not be null");
        Objects.requireNonNull(transactionDate, "transactionDate must not be null");

        UUID transactionId = UUID.randomUUID();
        SaleHeader header = new SaleHeader(transactionId, receiptNumber, 0.0, new Date(transactionDate.getTime()));
        return new SaleTransaction(header);
    }

    @Override
    public boolean addItemToTransaction(SaleTransaction transaction, SaleItem saleItem) {
        Objects.requireNonNull(transaction, "transaction must not be null");
        Objects.requireNonNull(saleItem, "saleItem must not be null");
        
        try {
            transaction.addItem(saleItem);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public SaleTransaction getTransactionById(UUID transactionId) {
        Objects.requireNonNull(transactionId, "transactionId must not be null");
        
        SaleHeader header = saleHeaderRepository.getById(transactionId);
        if (header == null) {
            return null;
        }
        
        SaleTransaction transaction = new SaleTransaction(header);
        SaleItem[] items = saleItemRepository.getByTransactionId(transactionId);
        
        for (SaleItem item : items) {
            transaction.addItem(item);
        }
        
        return transaction;
    }

    @Override
    public SaleTransaction[] getAllTransactions() {
        SaleHeader[] headers = saleHeaderRepository.getAll();
        SaleTransaction[] transactions = new SaleTransaction[headers.length];
        
        for (int i = 0; i < headers.length; i++) {
            SaleHeader header = headers[i];
            SaleTransaction transaction = new SaleTransaction(header);
            
            SaleItem[] items = saleItemRepository.getByTransactionId(header.getTransactionId());
            for (SaleItem item : items) {
                transaction.addItem(item);
            }
            
            transactions[i] = transaction;
        }

        return transactions;
    }

    @Override
    public boolean completeTransaction(SaleTransaction transaction) {
        Objects.requireNonNull(transaction, "transaction must not be null");
        
        try {
            // Save the header
            saleHeaderRepository.add(transaction.getHeader());
            
            // Save all items
            for (SaleItem item : transaction.getSaleItems()) {
                saleItemRepository.add(item);
            }
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
