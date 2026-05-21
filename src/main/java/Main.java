import com.jpos.inventory.InventoryFacade;
import com.jpos.inventory.repository.InventoryRepository;
import com.jpos.inventory.repository.ProductRepository;
import com.jpos.inventory.repository.implementation.FileInventoryRepository;
import com.jpos.inventory.repository.implementation.FileProductRepository;
import com.jpos.user.UserFacade;
import com.jpos.user.repository.UserRepository;
import com.jpos.user.repository.implementation.FileUserRepository;
import utils.WelcomeMessage;
import view.AppMenu;
import view.InventoryFeature;
import view.ProductFeature;
import view.implementation.*;
import view.UserFeature;

public class Main {
    public static void main(String[] args) {
        UserRepository userRepository = new FileUserRepository();
        UserFacade userFacade = new UserFacade(userRepository);
        AppMenu appMenuView = new AppMenuView(userFacade);
        UserFeature userFeatureView = new UserFeatureView(userFacade);

        InventoryRepository inventoryRepository = new FileInventoryRepository();
        ProductRepository productRepository = new FileProductRepository();
        InventoryFacade inventoryFacade = new InventoryFacade(inventoryRepository, productRepository);
        ProductFeature productFeatureView = new ProductFeatureView(inventoryFacade);
        InventoryFeature inventoryFeatureView = new InventoryFeatureView(inventoryFacade);

        AppView appView = new AppView(appMenuView, userFeatureView, productFeatureView, inventoryFeatureView);
        WelcomeMessage.displayWelcomeMessage();
        appView.createSession();
    }
}
