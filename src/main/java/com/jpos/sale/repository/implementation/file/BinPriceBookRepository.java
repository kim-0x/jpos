package com.jpos.sale.repository.implementation.file;

import com.jpos.sale.model.PriceBook;
import com.jpos.sale.repository.PriceBookRepository;
import utils.AbstractBinRepository;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.UUID;

public class BinPriceBookRepository extends AbstractBinRepository<PriceBook> implements PriceBookRepository {
    private final ArrayList<PriceBook> priceBooks = new ArrayList<>();

    public BinPriceBookRepository() {
        this(getDefaultDatFilePath("pricebook.dat"));
    }

    public BinPriceBookRepository(Path filePath) {
        super(filePath);
        priceBooks.addAll(loadFromDat());
    }

    @Override
    public void add(PriceBook priceBook) {
        if (priceBook == null) {
            throw new IllegalArgumentException("Price book entry is required.");
        }
        priceBooks.add(priceBook);
        persistToDat(priceBooks);
    }

    @Override
    public PriceBook[] getAll() {
        return priceBooks.toArray(new PriceBook[0]);
    }

    @Override
    public PriceBook getById(UUID productId) {
        if (productId == null) {
            throw new IllegalArgumentException("Product id is required.");
        }

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

    @Override
    protected Class<PriceBook> getEntityType() {
        return PriceBook.class;
    }
}
