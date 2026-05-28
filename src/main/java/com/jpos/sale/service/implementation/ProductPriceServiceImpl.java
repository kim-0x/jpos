package com.jpos.sale.service.implementation;

import com.jpos.inventory.model.ProductQuery;
import com.jpos.sale.model.PriceBook;
import com.jpos.sale.model.ProductInfo;
import com.jpos.sale.model.ProductRef;
import com.jpos.sale.repository.PriceBookRepository;
import com.jpos.sale.service.ProductCatalogGateway;
import com.jpos.sale.service.ProductPriceService;

public class ProductPriceServiceImpl implements ProductPriceService {
    private final PriceBookRepository priceBookRepository;
    private final ProductCatalogGateway productCatalogGateway;

    public ProductPriceServiceImpl(PriceBookRepository priceBookRepository,
                                   ProductCatalogGateway productCatalogGateway) {
        this.priceBookRepository = priceBookRepository;
        this.productCatalogGateway = productCatalogGateway;
    }

    @Override
    public void setProductPrice(ProductQuery productQuery, float margin) {
        if (productQuery == null) {
            throw new IllegalArgumentException("Product query is required.");
        }

        ProductInfo productInfo = resolveProduct(productQuery);
        double cost = productInfo.cost();
        
        // Calculate sale price: cost * (1 + margin)
        double salePrice = cost * (1 + margin);
        
        // Create and save the new price book entry
        PriceBook priceBook = new PriceBook(productInfo.productId(), cost, margin, salePrice);
        priceBookRepository.add(priceBook);
    }

    @Override
    public PriceBook getCurrentProductPrice(ProductQuery productQuery) {
        if (productQuery == null) {
            throw new IllegalArgumentException("Product query is required.");
        }

        ProductInfo productInfo = resolveProduct(productQuery);
        return priceBookRepository.getById(productInfo.productId());
    }

    private ProductInfo resolveProduct(ProductQuery productQuery) {
        ProductRef ref = new ProductRef(productQuery.getProductId(), productQuery.getBarcode());
        return productCatalogGateway.findBy(ref);
    }
}
