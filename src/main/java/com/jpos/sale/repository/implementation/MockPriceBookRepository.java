package com.jpos.sale.repository.implementation;

import com.jpos.sale.model.PriceBook;
import com.jpos.sale.repository.PriceBookRepository;

import java.util.ArrayList;
import java.util.UUID;

public class MockPriceBookRepository implements PriceBookRepository {
    private final ArrayList<PriceBook> priceBooks = new ArrayList<>();

    @Override
    public void add(PriceBook priceBook) {
        priceBooks.add(priceBook);
    }

    @Override
    public PriceBook[] getAll() {
        return priceBooks.toArray(new PriceBook[0]);
    }

    @Override
    public PriceBook getById(UUID productId) {
        PriceBook latest = null;
        for (PriceBook priceBook : priceBooks) {
            if (!productId.equals(priceBook.getProductId())) {
                continue;
            }
            if (latest == null || priceBook.getEffectiveAt().after(latest.getEffectiveAt())) {
                latest = priceBook;
            }
        }
        return latest;
    }
}
