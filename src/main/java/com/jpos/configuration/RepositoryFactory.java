package com.jpos.configuration;

import com.jpos.inventory.repository.InventoryRepository;
import com.jpos.inventory.repository.ProductRepository;
import com.jpos.sale.repository.PriceBookRepository;
import com.jpos.sale.repository.SaleHeaderRepository;
import com.jpos.sale.repository.SaleItemRepository;
import com.jpos.user.repository.UserRepository;

public interface RepositoryFactory {
    UserRepository createUserRepository();
    InventoryRepository createInventoryRepository();
    ProductRepository createProductRepository();
    SaleHeaderRepository createSaleHeaderRepository();
    SaleItemRepository createSaleItemRepository();
    PriceBookRepository createPriceBookRepository();
}
