package com.jpos.report.service;

import com.jpos.sale.model.SaleTransaction;

import java.util.Date;
import java.util.stream.Stream;

public interface SaleGateway {
   Stream<SaleTransaction> getAllTransactions(Date fromDate, Date toDate);
}
