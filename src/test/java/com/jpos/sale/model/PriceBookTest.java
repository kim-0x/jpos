package com.jpos.sale.model;

import org.junit.Test;

import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PriceBookTest {

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectNullProductId() {
        new PriceBook(null, 10.0, 0.2f, 12.0, new Date());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectZeroOrNegativeCost() {
        new PriceBook(UUID.randomUUID(), 0.0, 0.2f, 12.0, new Date());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectZeroOrNegativeSalePrice() {
        new PriceBook(UUID.randomUUID(), 10.0, 0.2f, -1.0, new Date());
    }

    @Test
    public void shouldGenerateCurrentTimestampWhenEffectiveAtIsMissing() {
        long beforeCreate = System.currentTimeMillis();

        PriceBook priceBook = new PriceBook(UUID.randomUUID(), 10.0, 0.2f, 12.0, null);

        long afterCreate = System.currentTimeMillis();
        assertNotNull(priceBook.getEffectiveAt());
        long effectiveAtMillis = priceBook.getEffectiveAt().getTime();
        assertTrue(effectiveAtMillis >= beforeCreate && effectiveAtMillis <= afterCreate);
    }

    @Test
    public void shouldDefensivelyCopyEffectiveAt() {
        Date source = new Date();
        PriceBook priceBook = new PriceBook(UUID.randomUUID(), 10.0, 0.2f, 12.0, source);

        source.setTime(source.getTime() + 100000);
        long initialStoredValue = priceBook.getEffectiveAt().getTime();

        Date fetched = priceBook.getEffectiveAt();
        fetched.setTime(fetched.getTime() + 100000);

        assertTrue(priceBook.getEffectiveAt().getTime() == initialStoredValue);
    }
}
