package com.jpos.configuration;

import com.jpos.inventory.repository.InventoryRepository;
import com.jpos.inventory.repository.ProductRepository;
import com.jpos.inventory.repository.implementation.file.CsvInventoryRepository;
import com.jpos.inventory.repository.implementation.file.CsvProductRepository;
import com.jpos.sale.repository.PriceBookRepository;
import com.jpos.sale.repository.SaleHeaderRepository;
import com.jpos.sale.repository.SaleItemRepository;
import com.jpos.sale.repository.implementation.file.CsvPriceBookRepository;
import com.jpos.sale.repository.implementation.file.CsvSaleHeaderRepository;
import com.jpos.sale.repository.implementation.file.CsvSaleItemRepository;
import com.jpos.user.repository.UserRepository;
import com.jpos.user.repository.implementation.file.CsvUserRepository;

public class CsvRepositoryFactory implements RepositoryFactory {

    @Override
    public UserRepository createUserRepository() {
        return new CsvUserRepository();
    }

    @Override
    public InventoryRepository createInventoryRepository() {
        return new CsvInventoryRepository();
    }

    @Override
    public ProductRepository createProductRepository() {
        return new CsvProductRepository();
    }

    @Override
    public SaleHeaderRepository createSaleHeaderRepository() {
        return new CsvSaleHeaderRepository();
    }

    @Override
    public SaleItemRepository createSaleItemRepository() {
        return new CsvSaleItemRepository();
    }

    @Override
    public PriceBookRepository createPriceBookRepository() {
        return new CsvPriceBookRepository();
    }
}
