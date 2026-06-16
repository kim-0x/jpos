package com.jpos.sale.repository.implementation.jdbc;

import com.jpos.sale.model.PriceBook;
import com.jpos.sale.repository.PriceBookRepository;
import utils.SqliteConnectionProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class JdbcPriceBookRepository implements PriceBookRepository {

    private final SqliteConnectionProvider connectionProvider;

    public JdbcPriceBookRepository(SqliteConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public void add(PriceBook priceBook) {
        if (priceBook == null) {
            throw new IllegalArgumentException("PriceBook is required.");
        }

        String sql = "INSERT INTO price_books (product_id, cost, margin, sale_price, effective_at)"
                + " VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, priceBook.getProductId().toString());
            ps.setDouble(2, priceBook.getCost());
            ps.setFloat(3,  priceBook.getMargin());
            ps.setDouble(4, priceBook.getSalePrice());
            ps.setLong(5,   priceBook.getEffectiveAt().getTime());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert price book: " + e.getMessage(), e);
        }
    }

    @Override
    public PriceBook[] getAll() {
        String sql = "SELECT product_id, cost, margin, sale_price, effective_at FROM price_books";
        ArrayList<PriceBook> priceBooks = new ArrayList<>();
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                priceBooks.add(mapPriceBook(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load price books: " + e.getMessage(), e);
        }
        return priceBooks.toArray(new PriceBook[0]);
    }

    @Override
    public PriceBook getById(UUID productId) {
        if (productId == null) {
            return null;
        }

        String sql = "SELECT product_id, cost, margin, sale_price, effective_at FROM price_books"
                + " WHERE product_id = ? ORDER BY effective_at DESC LIMIT 1";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapPriceBook(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get price book: " + e.getMessage(), e);
        }
        return null;
    }

    private PriceBook mapPriceBook(ResultSet rs) throws SQLException {
        UUID   productId   = UUID.fromString(rs.getString("product_id"));
        double cost        = rs.getDouble("cost");
        float  margin      = rs.getFloat("margin");
        double salePrice   = rs.getDouble("sale_price");
        Date   effectiveAt = new Date(rs.getLong("effective_at"));
        return new PriceBook(productId, cost, margin, salePrice, effectiveAt);
    }
}
