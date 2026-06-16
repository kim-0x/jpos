package com.jpos.configuration;

import com.jpos.inventory.repository.InventoryRepository;
import com.jpos.inventory.repository.ProductRepository;
import com.jpos.inventory.repository.implementation.jdbc.JdbcInventoryRepository;
import com.jpos.inventory.repository.implementation.jdbc.JdbcProductRepository;
import com.jpos.sale.repository.PriceBookRepository;
import com.jpos.sale.repository.SaleHeaderRepository;
import com.jpos.sale.repository.SaleItemRepository;
import com.jpos.sale.repository.implementation.jdbc.JdbcPriceBookRepository;
import com.jpos.sale.repository.implementation.jdbc.JdbcSaleHeaderRepository;
import com.jpos.sale.repository.implementation.jdbc.JdbcSaleItemRepository;
import com.jpos.user.repository.UserRepository;
import com.jpos.user.repository.implementation.jdbc.JdbcUserRepository;
import utils.SqliteConnectionProvider;

public class JdbcRepositoryFactory implements RepositoryFactory {

    private final SqliteConnectionProvider connectionProvider;

    public JdbcRepositoryFactory() {
        this.connectionProvider = new SqliteConnectionProvider();
    }

    @Override
    public UserRepository createUserRepository() {
        return new JdbcUserRepository(connectionProvider);
    }

    @Override
    public InventoryRepository createInventoryRepository() {
        return new JdbcInventoryRepository(connectionProvider);
    }

    @Override
    public ProductRepository createProductRepository() {
        return new JdbcProductRepository(connectionProvider);
    }

    @Override
    public SaleHeaderRepository createSaleHeaderRepository() {
        return new JdbcSaleHeaderRepository(connectionProvider);
    }

    @Override
    public SaleItemRepository createSaleItemRepository() {
        return new JdbcSaleItemRepository(connectionProvider);
    }

    @Override
    public PriceBookRepository createPriceBookRepository() {
        return new JdbcPriceBookRepository(connectionProvider);
    }
}
