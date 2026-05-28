package com.jpos.sale.service.implementation;

import com.jpos.inventory.model.Product;
import com.jpos.inventory.model.ProductQuery;
import com.jpos.inventory.repository.ProductRepository;
import com.jpos.inventory.service.InventoryService;
import com.jpos.sale.exception.ProductNotFoundException;
import com.jpos.sale.model.ProductInfo;
import com.jpos.sale.model.ProductRef;
import com.jpos.sale.service.ProductCatalogGateway;

public class InventoryProductCatalogGateway implements ProductCatalogGateway {
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;

    public InventoryProductCatalogGateway(ProductRepository productRepository, InventoryService inventoryService) {
        this.productRepository = productRepository;
        this.inventoryService = inventoryService;
    }

    @Override
    public ProductInfo findBy(ProductRef ref) {
        ProductQuery query = new ProductQuery(ref == null ? null : ref.productId(), ref == null ? null : ref.barcode());
        Product product = productRepository.getProductBy(query);
        if (product == null) {
            throw new ProductNotFoundException(resolveIdentifier(ref));
        }

        try {
            ProductQuery normalizedQuery = new ProductQuery(product.getId(), product.getBarcode());
            double cost = inventoryService.getProductCostBy(normalizedQuery);
            return new ProductInfo(product.getId(), product.getBarcode(), product.getName(), cost);
        } catch (com.jpos.inventory.exception.ProductNotFoundException e) {
            throw new ProductNotFoundException(resolveIdentifier(ref));
        }
    }

    private String resolveIdentifier(ProductRef ref) {
        if (ref == null) {
            return "null";
        }
        return String.valueOf(ref.productId() != null ? ref.productId() : ref.barcode());
    }
}