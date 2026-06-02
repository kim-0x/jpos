package com.jpos.report.service.implementation;

import com.jpos.report.service.SaleGateway;
import com.jpos.sale.model.SaleTransaction;
import com.jpos.sale.service.SaleTransactionService;

import java.util.Arrays;
import java.util.Date;
import java.util.stream.Stream;

public class SaleGetewayImpl implements SaleGateway {
    private final SaleTransactionService saleTransactionService;

    public SaleGetewayImpl(SaleTransactionService saleTransactionService) {
        this.saleTransactionService = saleTransactionService;
    }

    @Override
    public Stream<SaleTransaction> getAllTransactions(Date fromDate, Date toDate) {
        var transactions = saleTransactionService.getAllTransactions();
        return Arrays.stream(transactions).filter(st ->
                st.getHeader().getTransactionDate().after(fromDate) &&
                        st.getHeader().getTransactionDate().before(toDate));
    }
}
