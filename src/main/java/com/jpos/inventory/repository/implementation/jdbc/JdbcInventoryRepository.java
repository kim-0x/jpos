package com.jpos.inventory.repository.implementation.jdbc;

import com.jpos.inventory.model.ProductQuery;
import com.jpos.inventory.model.StockItem;
import com.jpos.inventory.repository.InventoryRepository;
import utils.SqliteConnectionProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class JdbcInventoryRepository implements InventoryRepository {

    private static final float LOW_STOCK_LEVEL = 3.0f;

    private final SqliteConnectionProvider connectionProvider;

    public JdbcInventoryRepository(SqliteConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public void stockOut(StockItem stockItem) {
        validateStockItem(stockItem);

        float currentLevel = getStockLevelOf(new ProductQuery(stockItem.getProductId(), null));
        float remaining    = currentLevel - Math.abs(stockItem.getNumberInStock());
        if (remaining <= LOW_STOCK_LEVEL) {
            throw new IllegalStateException(
                    String.format("Product %s is low in stock.", stockItem.getProductId()));
        }

        stockItem.setNumberInStock(-Math.abs(stockItem.getNumberInStock()));
        insertStockItem(stockItem);
    }

    @Override
    public void stockIn(StockItem stockItem) {
        validateStockItem(stockItem);
        stockItem.setNumberInStock(Math.abs(stockItem.getNumberInStock()));
        insertStockItem(stockItem);
    }

    @Override
    public StockItem[] getStockItems() {
        String sql = "SELECT id, number_in_stock, cost, product_id, created_at FROM inventory";
        ArrayList<StockItem> items = new ArrayList<>();
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                items.add(mapStockItem(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load stock items: " + e.getMessage(), e);
        }
        return items.toArray(new StockItem[0]);
    }

    @Override
    public double getProductCost(ProductQuery productQuery) {
        validateProductQuery(productQuery);

        String sql = "SELECT cost FROM inventory"
                + " WHERE product_id = ? AND number_in_stock > 0"
                + " ORDER BY created_at DESC LIMIT 1";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productQuery.getProductId().toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("cost");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get product cost: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public float getStockLevelOf(ProductQuery productQuery) {
        validateProductQuery(productQuery);

        String sql = "SELECT COALESCE(SUM(number_in_stock), 0) AS total FROM inventory WHERE product_id = ?";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productQuery.getProductId().toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getFloat("total");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get stock level: " + e.getMessage(), e);
        }
        return 0;
    }

    private void insertStockItem(StockItem stockItem) {
        if (stockItem.getId() == null) {
            stockItem.setId(UUID.randomUUID());
        }
        if (stockItem.getCreatedAt() == null) {
            stockItem.setCreatedAt(new Date());
        }

        String sql = "INSERT INTO inventory (id, number_in_stock, cost, product_id, created_at)"
                + " VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, stockItem.getId().toString());
            ps.setFloat(2,  stockItem.getNumberInStock());
            ps.setDouble(3, stockItem.getCost());
            ps.setString(4, stockItem.getProductId().toString());
            ps.setLong(5,   stockItem.getCreatedAt().getTime());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert stock item: " + e.getMessage(), e);
        }
    }

    private StockItem mapStockItem(ResultSet rs) throws SQLException {
        StockItem item = new StockItem();
        item.setId(UUID.fromString(rs.getString("id")));
        item.setNumberInStock(rs.getFloat("number_in_stock"));
        item.setCost(rs.getDouble("cost"));
        item.setProductId(UUID.fromString(rs.getString("product_id")));
        item.setCreatedAt(new Date(rs.getLong("created_at")));
        return item;
    }

    private void validateProductQuery(ProductQuery productQuery) {
        if (productQuery == null) {
            throw new IllegalArgumentException("Product query is required.");
        }
        if (productQuery.getProductId() == null) {
            throw new IllegalArgumentException("Product id is required.");
        }
    }

    private void validateStockItem(StockItem stockItem) {
        if (stockItem == null) {
            throw new IllegalArgumentException("Stock item is required.");
        }
        if (stockItem.getProductId() == null) {
            throw new IllegalArgumentException("Product id is required.");
        }
    }
}
