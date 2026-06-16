package com.jpos.configuration;

import com.jpos.inventory.repository.InventoryRepository;
import com.jpos.inventory.repository.ProductRepository;
import com.jpos.inventory.repository.implementation.file.BinInventoryRepository;
import com.jpos.inventory.repository.implementation.file.BinProductRepository;
import com.jpos.sale.repository.PriceBookRepository;
import com.jpos.sale.repository.SaleHeaderRepository;
import com.jpos.sale.repository.SaleItemRepository;
import com.jpos.sale.repository.implementation.file.BinPriceBookRepository;
import com.jpos.sale.repository.implementation.file.BinSaleHeaderRepository;
import com.jpos.sale.repository.implementation.file.BinSaleItemRepository;
import com.jpos.user.repository.UserRepository;
import com.jpos.user.repository.implementation.file.BinUserRepository;

public class BinRepositoryFactory implements RepositoryFactory {

    @Override
    public UserRepository createUserRepository() {
        return new BinUserRepository();
    }

    @Override
    public InventoryRepository createInventoryRepository() {
        return new BinInventoryRepository();
    }

    @Override
    public ProductRepository createProductRepository() {
        return new BinProductRepository();
    }

    @Override
    public SaleHeaderRepository createSaleHeaderRepository() {
        return new BinSaleHeaderRepository();
    }

    @Override
    public SaleItemRepository createSaleItemRepository() {
        return new BinSaleItemRepository();
    }

    @Override
    public PriceBookRepository createPriceBookRepository() {
        return new BinPriceBookRepository();
    }
}
