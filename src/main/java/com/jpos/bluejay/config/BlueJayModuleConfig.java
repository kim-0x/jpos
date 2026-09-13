package com.jpos.bluejay.config;

import com.jpos.inventory.InventoryFacade;
import com.jpos.inventory.repository.InventoryRepository;
import com.jpos.inventory.repository.ProductRepository;
import com.jpos.inventory.repository.implementation.jdbc.JdbcInventoryRepository;
import com.jpos.inventory.repository.implementation.jdbc.JdbcProductRepository;
import com.jpos.inventory.service.InventoryService;
import com.jpos.inventory.service.ProductService;
import com.jpos.inventory.service.implementation.InventoryServiceImpl;
import com.jpos.inventory.service.implementation.ProductServiceImpl;
import com.jpos.report.ReportFacade;
import com.jpos.report.service.InventoryReportGateway;
import com.jpos.report.service.InventoryReportService;
import com.jpos.report.service.ReportFilterService;
import com.jpos.report.service.SaleReportGateway;
import com.jpos.report.service.SaleReportService;
import com.jpos.report.service.implementation.InventoryReportGatewayImpl;
import com.jpos.report.service.implementation.InventoryReportServiceImpl;
import com.jpos.report.service.implementation.ReportFilterServiceImpl;
import com.jpos.report.service.implementation.SaleReportGatewayImpl;
import com.jpos.report.service.implementation.SaleReportServiceImpl;
import com.jpos.sale.SaleFacade;
import com.jpos.sale.repository.PriceBookRepository;
import com.jpos.sale.repository.SaleHeaderRepository;
import com.jpos.sale.repository.SaleItemRepository;
import com.jpos.sale.repository.implementation.jdbc.JdbcPriceBookRepository;
import com.jpos.sale.repository.implementation.jdbc.JdbcSaleHeaderRepository;
import com.jpos.sale.repository.implementation.jdbc.JdbcSaleItemRepository;
import com.jpos.sale.service.InventoryGateway;
import com.jpos.sale.service.ProductPriceService;
import com.jpos.sale.service.SaleTransactionService;
import com.jpos.sale.service.implementation.InventoryGatewayImpl;
import com.jpos.sale.service.implementation.ProductPriceServiceImpl;
import com.jpos.sale.service.implementation.SaleTransactionServiceImpl;
import com.jpos.user.repository.UserRepository;
import com.jpos.user.repository.implementation.jdbc.JdbcUserRepository;
import com.jpos.user.service.UserService;
import com.jpos.user.service.implementation.UserServiceImpl;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import utils.SqliteConnectionProvider;

@Configuration
@EnableConfigurationProperties(BlueJayDataProperties.class)
public class BlueJayModuleConfig {

    @Bean
    SqliteConnectionProvider connectionProvider(BlueJayDataProperties props) {
        try {
            if (props.getDriverClassName() != null && !props.getDriverClassName().isBlank()) {
                Class.forName(props.getDriverClassName());
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Database driver class not found: " + props.getDriverClassName(), e);
        }
        return new SqliteConnectionProvider(props.getUrl());
    }

    @Bean
    UserRepository userRepository(SqliteConnectionProvider connectionProvider) {
        return new JdbcUserRepository(connectionProvider);
    }

    @Bean
    ProductRepository productRepository(SqliteConnectionProvider connectionProvider) {
        return new JdbcProductRepository(connectionProvider);
    }

    @Bean
    InventoryRepository inventoryRepository(SqliteConnectionProvider connectionProvider) {
        return new JdbcInventoryRepository(connectionProvider);
    }

    @Bean
    SaleHeaderRepository saleHeaderRepository(SqliteConnectionProvider connectionProvider) {
        return new JdbcSaleHeaderRepository(connectionProvider);
    }

    @Bean
    SaleItemRepository saleItemRepository(SqliteConnectionProvider connectionProvider) {
        return new JdbcSaleItemRepository(connectionProvider);
    }

    @Bean
    PriceBookRepository priceBookRepository(SqliteConnectionProvider connectionProvider) {
        return new JdbcPriceBookRepository(connectionProvider);
    }

    @Bean
    ProductService productService(ProductRepository productRepository) {
        return new ProductServiceImpl(productRepository);
    }

    @Bean
    InventoryService inventoryService(InventoryRepository inventoryRepository, ProductRepository productRepository) {
        return new InventoryServiceImpl(inventoryRepository, productRepository);
    }

    @Bean
    UserService userService(UserRepository userRepository) {
        return new UserServiceImpl(userRepository);
    }

    @Bean
    InventoryGateway inventoryGateway(ProductRepository productRepository, InventoryService inventoryService) {
        return new InventoryGatewayImpl(productRepository, inventoryService);
    }

    @Bean
    SaleTransactionService saleTransactionService(SaleHeaderRepository saleHeaderRepository,
                                                  SaleItemRepository saleItemRepository) {
        return new SaleTransactionServiceImpl(saleHeaderRepository, saleItemRepository);
    }

    @Bean
    ProductPriceService productPriceService(PriceBookRepository priceBookRepository, InventoryGateway inventoryGateway) {
        return new ProductPriceServiceImpl(priceBookRepository, inventoryGateway);
    }

    @Bean
    InventoryFacade inventoryFacade(InventoryRepository inventoryRepository, ProductRepository productRepository) {
        return new InventoryFacade(inventoryRepository, productRepository);
    }

    @Bean
    SaleFacade saleFacade(SaleHeaderRepository saleHeaderRepository,
                          SaleItemRepository saleItemRepository,
                          PriceBookRepository priceBookRepository,
                          InventoryGateway inventoryGateway) {
        return new SaleFacade(saleHeaderRepository, saleItemRepository, priceBookRepository, inventoryGateway);
    }

    @Bean
    SaleReportGateway saleReportGateway(SaleTransactionService saleTransactionService) {
        return new SaleReportGatewayImpl(saleTransactionService);
    }

    @Bean
    SaleReportService saleReportService(SaleReportGateway saleReportGateway, InventoryGateway inventoryGateway) {
        return new SaleReportServiceImpl(saleReportGateway, inventoryGateway);
    }

    @Bean
    InventoryReportGateway inventoryReportGateway(InventoryService inventoryService) {
        return new InventoryReportGatewayImpl(inventoryService);
    }

    @Bean
    InventoryReportService inventoryReportService(InventoryReportGateway inventoryReportGateway) {
        return new InventoryReportServiceImpl(inventoryReportGateway);
    }

    @Bean
    ReportFilterService reportFilterService() {
        return new ReportFilterServiceImpl();
    }

    @Bean
    ReportFacade reportFacade(SaleReportService saleReportService,
                              InventoryReportService inventoryReportService,
                              ReportFilterService reportFilterService) {
        return new ReportFacade(saleReportService, inventoryReportService, reportFilterService);
    }
}
