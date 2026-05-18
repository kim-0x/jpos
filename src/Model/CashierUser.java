package Model;

import java.util.UUID;

public class CashierUser extends User {
    private final String[] accessFeatures = new String[] {
            "Check Latest Price for Product",
            "Start Sale"
    };

    @Override
    public String[] getAccessFeatures() {
        return accessFeatures;
    }

    public CashierUser(String username, String password) {
        super(username, "Cashier");
        this.setPassword(password);
        this.setId(UUID.randomUUID());
    }
}
