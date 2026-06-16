package com.jpos.inventory.repository.implementation.jdbc;

import com.jpos.inventory.model.Product;
import com.jpos.inventory.model.ProductCategory;
import com.jpos.inventory.model.ProductQuery;
import com.jpos.inventory.repository.ProductRepository;
import utils.SqliteConnectionProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

public class JdbcProductRepository implements ProductRepository {

    private final SqliteConnectionProvider connectionProvider;

    public JdbcProductRepository(SqliteConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public Product getProductBy(ProductQuery productQuery) {
        if (productQuery == null) {
            return null;
        }

        String sql = "SELECT id, barcode, name, product_category FROM products WHERE id = ? OR barcode = ? LIMIT 1";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String idParam      = productQuery.getProductId() != null ? productQuery.getProductId().toString() : null;
            String barcodeParam = productQuery.getBarcode();
            ps.setString(1, idParam);
            ps.setString(2, barcodeParam);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapProduct(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get product: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public Product[] getProducts() {
        String sql = "SELECT id, barcode, name, product_category FROM products";
        ArrayList<Product> products = new ArrayList<>();
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                products.add(mapProduct(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load products: " + e.getMessage(), e);
        }
        return products.toArray(new Product[0]);
    }

    @Override
    public void saveProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product is required.");
        }

        Product existing = findExisting(product);
        if (existing != null) {
            if (product.getId() == null) {
                product.setId(existing.getId());
            }
            updateProduct(product);
        } else {
            if (product.getId() == null) {
                product.setId(UUID.randomUUID());
            }
            insertProduct(product);
        }
    }

    private void insertProduct(Product product) {
        String sql = "INSERT INTO products (id, barcode, name, product_category) VALUES (?, ?, ?, ?)";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getId().toString());
            ps.setString(2, product.getBarcode());
            ps.setString(3, product.getName());
            ps.setString(4, product.getCategory().getValue());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert product: " + e.getMessage(), e);
        }
    }

    private void updateProduct(Product product) {
        String sql = "UPDATE products SET barcode = ?, name = ?, product_category = ? WHERE id = ?";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getBarcode());
            ps.setString(2, product.getName());
            ps.setString(3, product.getCategory().getValue());
            ps.setString(4, product.getId().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update product: " + e.getMessage(), e);
        }
    }

    private Product findExisting(Product product) {
        if (product == null) {
            return null;
        }

        String idParam      = product.getId() != null ? product.getId().toString() : null;
        String barcodeParam = product.getBarcode();

        String sql = "SELECT id, barcode, name, product_category FROM products WHERE id = ? OR barcode = ? LIMIT 1";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idParam);
            ps.setString(2, barcodeParam);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapProduct(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find product: " + e.getMessage(), e);
        }
        return null;
    }

    private Product mapProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(UUID.fromString(rs.getString("id")));
        product.setBarcode(rs.getString("barcode"));
        product.setName(rs.getString("name"));
        product.setCategory(ProductCategory.fromString(rs.getString("product_category")));
        return product;
    }
}
