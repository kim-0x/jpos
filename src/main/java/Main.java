import com.jos.inventory.InventoryFacade;
import com.jos.inventory.repository.InventoryRepository;
import com.jos.inventory.repository.ProductRepository;
import com.jos.inventory.repository.implementation.MockInventoryRepository;
import com.jos.inventory.repository.implementation.MockProductRepository;
import com.jpos.user.UserFacade;
import com.jpos.user.repository.implementation.MockUserRepository;
import com.jpos.user.repository.UserRepository;
import utils.WelcomeMessage;
import view.AppMenu;
import view.InventoryFeature;
import view.ProductFeature;
import view.implementation.*;
import view.UserFeature;

public class Main {
    public static void main(String[] args) {
        UserRepository userRepository = new MockUserRepository();
        UserFacade userFacade = new UserFacade(userRepository);
        AppMenu appMenuView = new AppMenuView(userFacade);
        UserFeature userFeatureView = new UserFeatureView(userFacade);

        InventoryRepository inventoryRepository = new MockInventoryRepository();
        ProductRepository productRepository = new MockProductRepository();
        InventoryFacade inventoryFacade = new InventoryFacade(inventoryRepository, productRepository);
        ProductFeature productFeatureView = new ProductFeatureView(inventoryFacade);
        InventoryFeature inventoryFeatureView = new InventoryFeatureView(inventoryFacade);

        AppView appView = new AppView(appMenuView, userFeatureView, productFeatureView, inventoryFeatureView);
        WelcomeMessage.displayWelcomeMessage();
        appView.createSession();
    }
}
