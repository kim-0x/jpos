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
import view.ProductFeature;
import view.implementation.AppMenuView;
import view.implementation.AppView;
import view.implementation.ProductFeatureView;
import view.implementation.UserFeatureView;
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

        AppView appView = new AppView(appMenuView, userFeatureView, productFeatureView);
        WelcomeMessage.displayWelcomeMessage();
        appView.createSession();
    }
}
