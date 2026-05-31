import com.jpos.inventory.InventoryFacade;
import com.jpos.inventory.repository.InventoryRepository;
import com.jpos.inventory.repository.ProductRepository;
import com.jpos.inventory.repository.implementation.file.CsvInventoryRepository;
import com.jpos.inventory.repository.implementation.file.CsvProductRepository;
import com.jpos.inventory.service.implementation.InventoryServiceImpl;
import com.jpos.sale.SaleFacade;
import com.jpos.sale.repository.PriceBookRepository;
import com.jpos.sale.repository.SaleHeaderRepository;
import com.jpos.sale.repository.SaleItemRepository;
import com.jpos.sale.repository.implementation.file.CsvPriceBookRepository;
import com.jpos.sale.repository.implementation.file.CsvSaleHeaderRepository;
import com.jpos.sale.repository.implementation.file.CsvSaleItemRepository;
import com.jpos.sale.service.ProductCatalogGateway;
import com.jpos.sale.service.implementation.InventoryProductCatalogGateway;
import com.jpos.user.UserFacade;
import com.jpos.user.repository.UserRepository;
import com.jpos.user.repository.implementation.file.DatUserRepository;
import utils.WelcomeMessage;
import view.*;
import view.implementation.*;

public class Main {
    public static void main(String[] args) {
        UserRepository userRepository = new DatUserRepository();
        UserFacade userFacade = new UserFacade(userRepository);
        AppMenu appMenuView = new AppMenuView(userFacade);
        UserFeature userFeatureView = new UserFeatureView(userFacade);

        InventoryRepository inventoryRepository = new CsvInventoryRepository();
        ProductRepository productRepository = new CsvProductRepository();
        InventoryFacade inventoryFacade = new InventoryFacade(inventoryRepository, productRepository);
        ProductFeature productFeatureView = new ProductFeatureView(inventoryFacade);
        InventoryFeature inventoryFeatureView = new InventoryFeatureView(inventoryFacade);

        SaleHeaderRepository  saleHeaderRepository = new CsvSaleHeaderRepository();
        SaleItemRepository saleItemRepository = new CsvSaleItemRepository();
        PriceBookRepository priceBookRepository = new CsvPriceBookRepository();
        ProductCatalogGateway productCatalogGateway = new InventoryProductCatalogGateway(
                productRepository,
                new InventoryServiceImpl(inventoryRepository, productRepository));

        SaleFacade saleFacade = new SaleFacade(
                saleHeaderRepository,
                saleItemRepository,
                priceBookRepository,
                productCatalogGateway);
        SaleFeature saleFeatureView = new SaleFeatureView(saleFacade);

        AppView appView = new AppView(appMenuView,
                userFeatureView,
                productFeatureView,
                inventoryFeatureView,
                saleFeatureView);

        WelcomeMessage.displayWelcomeMessage();
        appView.createSession();
    }
}
