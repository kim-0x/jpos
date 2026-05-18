package Model;

import java.util.UUID;

public abstract class User extends BaseUser {
    private String password;
    private UUID id;
    private String role;

    public User(String username, String role) {
        super(username, role);
    }

    public abstract String[] getAccessFeatures();

    public String getPassword() {
        return password;
    }

    protected void setPassword(String password) {
        this.password = password;
    }

    public UUID getId() {
        return id;
    }

    protected void setId(UUID id) {
        this.id = id;
    }
}
