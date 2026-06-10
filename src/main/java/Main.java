import com.jpos.inventory.InventoryFacade;
import com.jpos.inventory.repository.InventoryRepository;
import com.jpos.inventory.repository.ProductRepository;
import com.jpos.inventory.repository.implementation.file.BinInventoryRepository;
import com.jpos.inventory.repository.implementation.file.BinProductRepository;
import com.jpos.inventory.service.InventoryService;
import com.jpos.inventory.service.implementation.InventoryServiceImpl;
import com.jpos.report.ReportFacade;
import com.jpos.report.service.*;
import com.jpos.report.service.implementation.*;
import com.jpos.sale.SaleFacade;
import com.jpos.sale.repository.PriceBookRepository;
import com.jpos.sale.repository.SaleHeaderRepository;
import com.jpos.sale.repository.SaleItemRepository;
import com.jpos.sale.repository.implementation.file.*;
import com.jpos.sale.service.InventoryGateway;
import com.jpos.sale.service.SaleTransactionService;
import com.jpos.sale.service.implementation.InventoryGatewayImpl;
import com.jpos.sale.service.implementation.SaleTransactionServiceImpl;
import com.jpos.user.UserFacade;
import com.jpos.user.repository.UserRepository;
import com.jpos.user.repository.implementation.file.BinUserRepository;
import utils.WelcomeMessage;
import view.*;
import view.implementation.*;

public class Main {
    public static void main(String[] args) {
        UserRepository userRepository = new BinUserRepository();
        UserFacade userFacade = new UserFacade(userRepository);
        AppMenu appMenuView = new AppMenuView(userFacade);
        UserFeature userFeatureView = new UserFeatureView(userFacade);

        InventoryRepository inventoryRepository = new BinInventoryRepository();
        ProductRepository productRepository = new BinProductRepository();
        InventoryFacade inventoryFacade = new InventoryFacade(inventoryRepository, productRepository);
        ProductFeature productFeatureView = new ProductFeatureView(inventoryFacade);
        InventoryFeature inventoryFeatureView = new InventoryFeatureView(inventoryFacade);

        SaleHeaderRepository  saleHeaderRepository = new BinSaleHeaderRepository();
        SaleItemRepository saleItemRepository = new BinSaleItemRepository();
        PriceBookRepository priceBookRepository = new BinPriceBookRepository();
        InventoryService inventoryService = new InventoryServiceImpl(inventoryRepository, productRepository);
        InventoryGateway inventoryGateway = new InventoryGatewayImpl(
                productRepository, inventoryService);

        SaleFacade saleFacade = new SaleFacade(
                saleHeaderRepository,
                saleItemRepository,
                priceBookRepository,
                inventoryGateway);
        SaleFeature saleFeatureView = new SaleFeatureView(saleFacade);

        SaleTransactionService saleTransactionService = new SaleTransactionServiceImpl(
                saleHeaderRepository,
                saleItemRepository);

        SaleReportGateway saleReportGateway = new SaleReportGatewayImpl(saleTransactionService);
        SaleReportService saleReportService = new SaleReportServiceImpl(saleReportGateway, inventoryGateway);
        InventoryReportGateway inventoryReportGateway = new InventoryReportGatewayImpl(inventoryService);
        InventoryReportService inventoryReportService = new InventoryReportServiceImpl(inventoryReportGateway);
        ReportFilterService reportFilterService = new ReportFilterServiceImpl();
        ReportFacade saleReportFacade = new ReportFacade(saleReportService, inventoryReportService, reportFilterService);
        ReportFeature reportFeatureView = new ReportFeatureView(saleReportFacade);

        AppView appView = new AppView(appMenuView,
                userFeatureView,
                productFeatureView,
                inventoryFeatureView,
                saleFeatureView,
                reportFeatureView);

        WelcomeMessage.displayWelcomeMessage();
        appView.createSession();
    }
}
