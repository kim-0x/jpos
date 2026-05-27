package com.jpos.sale.service.implementation;

import com.jpos.inventory.model.ProductQuery;
import com.jpos.sale.model.PriceBook;
import com.jpos.sale.repository.PriceBookRepository;
import com.jpos.sale.service.ProductIdentifierProvider;
import com.jpos.sale.service.ProductPriceService;
import com.jpos.sale.service.ProductCostProvider;

public class ProductPriceServiceImpl implements ProductPriceService {
    private final PriceBookRepository priceBookRepository;
    private final ProductCostProvider costProvider;
    private final ProductIdentifierProvider productIdentifierProvider;

    public ProductPriceServiceImpl(PriceBookRepository priceBookRepository,
                                   ProductCostProvider costProvider,
                                   ProductIdentifierProvider productIdentifierProvider) {
        this.priceBookRepository = priceBookRepository;
        this.costProvider = costProvider;
        this.productIdentifierProvider = productIdentifierProvider;
    }

    @Override
    public void setProductPrice(ProductQuery productQuery, float margin) {
        if (productQuery == null) {
            throw new IllegalArgumentException("Product query is required.");
        }

        ProductQuery normalizedQuery = normalizeQuery(productQuery);

        // Get the product cost from the provider (which looks it up in inventory)
        double cost = costProvider.getProductCost(normalizedQuery);
        
        // Calculate sale price: cost * (1 + margin)
        double salePrice = cost * (1 + margin);
        
        // Create and save the new price book entry
        PriceBook priceBook = new PriceBook(normalizedQuery.getProductId(), cost, margin, salePrice);
        priceBookRepository.add(priceBook);
    }

    @Override
    public PriceBook getCurrentProductPrice(ProductQuery productQuery) {
        if (productQuery == null) {
            throw new IllegalArgumentException("Product query is required.");
        }

        ProductQuery normalizedQuery = normalizeQuery(productQuery);
        return priceBookRepository.getById(normalizedQuery.getProductId());
    }

    private ProductQuery normalizeQuery(ProductQuery productQuery) {
        return new ProductQuery(productIdentifierProvider.getProductId(productQuery), productQuery.getBarcode());
    }
}
