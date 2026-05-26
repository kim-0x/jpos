package com.jpos.sale.repository;

import com.jpos.sale.model.PriceBook;

import java.util.UUID;

public interface PriceBookRepository {
    abstract void add(PriceBook priceBook);
    abstract PriceBook[] getAll();
    abstract PriceBook getById(UUID productId);
}
