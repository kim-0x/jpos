package com.jpos.sale.repository.implementation.jdbc;

import com.jpos.sale.model.SaleItem;
import com.jpos.sale.repository.SaleItemRepository;
import utils.SqliteConnectionProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

public class JdbcSaleItemRepository implements SaleItemRepository {

    private final SqliteConnectionProvider connectionProvider;

    public JdbcSaleItemRepository(SqliteConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public void add(SaleItem saleItem) {
        if (saleItem == null) {
            throw new IllegalArgumentException("SaleItem is required.");
        }

        String sql = "INSERT INTO sale_items (product_id, transaction_id, quantity, cost, price)"
                + " VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, saleItem.getProductId().toString());
            ps.setString(2, saleItem.getTransactionId().toString());
            ps.setFloat(3,  saleItem.getQuantity());
            ps.setDouble(4, saleItem.getCost());
            ps.setDouble(5, saleItem.getPrice());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert sale item: " + e.getMessage(), e);
        }
    }

    @Override
    public SaleItem[] getAll() {
        String sql = "SELECT product_id, transaction_id, quantity, cost, price FROM sale_items";
        ArrayList<SaleItem> items = new ArrayList<>();
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                items.add(mapSaleItem(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load sale items: " + e.getMessage(), e);
        }
        return items.toArray(new SaleItem[0]);
    }

    @Override
    public SaleItem[] getByTransactionId(UUID transactionId) {
        if (transactionId == null) {
            return new SaleItem[0];
        }

        String sql = "SELECT product_id, transaction_id, quantity, cost, price"
                + " FROM sale_items WHERE transaction_id = ?";
        ArrayList<SaleItem> items = new ArrayList<>();
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, transactionId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapSaleItem(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get sale items by transaction: " + e.getMessage(), e);
        }
        return items.toArray(new SaleItem[0]);
    }

    private SaleItem mapSaleItem(ResultSet rs) throws SQLException {
        UUID   productId     = UUID.fromString(rs.getString("product_id"));
        UUID   transactionId = UUID.fromString(rs.getString("transaction_id"));
        float  quantity      = rs.getFloat("quantity");
        double cost          = rs.getDouble("cost");
        double price         = rs.getDouble("price");
        return new SaleItem(productId, quantity, cost, price, transactionId);
    }
}
