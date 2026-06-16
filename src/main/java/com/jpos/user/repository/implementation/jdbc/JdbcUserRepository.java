package com.jpos.user.repository.implementation.jdbc;

import com.jpos.user.exception.AdminAlreadyExistsException;
import com.jpos.user.exception.UserCreationException;
import com.jpos.user.model.LoginUser;
import com.jpos.user.model.User;
import com.jpos.user.model.UserRole;
import com.jpos.user.repository.UserRepository;
import com.jpos.user.utils.UserBuilder;
import utils.SqliteConnectionProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

public class JdbcUserRepository implements UserRepository {

    private final SqliteConnectionProvider connectionProvider;

    public JdbcUserRepository(SqliteConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public boolean addUser(String username, String password, String role) throws UserCreationException {
        User user = UserBuilder.createUser(username, password, role);
        if (user == null) {
            throw new UserCreationException("Unable to create user object");
        }

        if (user.getRole().equalsIgnoreCase(UserRole.ADMIN.getValue()) && adminExists()) {
            throw new AdminAlreadyExistsException();
        }

        String sql = "INSERT INTO users (id, username, role, password) VALUES (?, ?, ?, ?)";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getRole());
            ps.setString(4, user.getPassword());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            throw new UserCreationException("Failed to persist user: " + e.getMessage());
        }
    }

    @Override
    public boolean isNameTaken(String name) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check username: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean validUser(String username, String password) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ? AND password = ?";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to validate user: " + e.getMessage(), e);
        }
    }

    @Override
    public LoginUser getUserLogin(String username) {
        String sql = "SELECT username, role FROM users WHERE username = ?";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new LoginUser(rs.getString("username"), rs.getString("role"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get user login: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public User[] getUsers() {
        String sql = "SELECT id, username, role, password FROM users";
        ArrayList<User> users = new ArrayList<>();
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                User user = UserBuilder.createUser(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"));
                if (user != null) {
                    user.setId(UUID.fromString(rs.getString("id")));
                    users.add(user);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load users: " + e.getMessage(), e);
        }
        return users.toArray(new User[0]);
    }

    private boolean adminExists() {
        String sql = "SELECT COUNT(*) FROM users WHERE role = ?";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, UserRole.ADMIN.getValue());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check admin existence: " + e.getMessage(), e);
        }
    }
}
