package com.jpos.sale.service.implementation;

import com.jpos.inventory.model.ProductQuery;
import com.jpos.sale.model.PriceBook;
import com.jpos.sale.repository.PriceBookRepository;
import com.jpos.sale.service.ProductPriceService;
import com.jpos.sale.service.ProductCostProvider;

import java.util.UUID;

public class ProductPriceServiceImpl implements ProductPriceService {
    private final PriceBookRepository priceBookRepository;
    private final ProductCostProvider costProvider;

    public ProductPriceServiceImpl(PriceBookRepository priceBookRepository, ProductCostProvider costProvider) {
        this.priceBookRepository = priceBookRepository;
        this.costProvider = costProvider;
    }

    @Override
    public void setProductPrice(ProductQuery productQuery, float margin) {
        if (productQuery == null || productQuery.getProductId() == null) {
            throw new IllegalArgumentException("Product id is required.");
        }

        // Get the product cost from the provider (which looks it up in inventory)
        double cost = costProvider.getProductCost(productQuery);
        
        // Calculate sale price: cost * (1 + margin)
        double salePrice = cost * (1 + margin);
        
        // Create and save the new price book entry
        PriceBook priceBook = new PriceBook(productQuery.getProductId(), cost, margin, salePrice);
        priceBookRepository.add(priceBook);
    }

    @Override
    public PriceBook getCurrentProductPrice(UUID productId) {
        return priceBookRepository.getById(productId);
    }
}
