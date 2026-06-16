package com.jpos.sale.repository.implementation.jdbc;

import com.jpos.sale.model.SaleHeader;
import com.jpos.sale.repository.SaleHeaderRepository;
import utils.SqliteConnectionProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class JdbcSaleHeaderRepository implements SaleHeaderRepository {

    private final SqliteConnectionProvider connectionProvider;

    public JdbcSaleHeaderRepository(SqliteConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public void add(SaleHeader saleHeader) {
        if (saleHeader == null) {
            throw new IllegalArgumentException("SaleHeader is required.");
        }

        String sql = "INSERT INTO sale_transactions (transaction_id, receipt_number, grand_total, transaction_date)"
                + " VALUES (?, ?, ?, ?)";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, saleHeader.getTransactionId().toString());
            ps.setString(2, saleHeader.getReceiptNumber());
            ps.setDouble(3, saleHeader.getGrandTotal());
            ps.setLong(4,   saleHeader.getTransactionDate().getTime());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert sale transaction: " + e.getMessage(), e);
        }
    }

    @Override
    public SaleHeader[] getAll() {
        String sql = "SELECT transaction_id, receipt_number, grand_total, transaction_date FROM sale_transactions";
        ArrayList<SaleHeader> headers = new ArrayList<>();
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                headers.add(mapSaleHeader(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load sale transactions: " + e.getMessage(), e);
        }
        return headers.toArray(new SaleHeader[0]);
    }

    @Override
    public SaleHeader getById(UUID transactionId) {
        if (transactionId == null) {
            return null;
        }

        String sql = "SELECT transaction_id, receipt_number, grand_total, transaction_date"
                + " FROM sale_transactions WHERE transaction_id = ?";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, transactionId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapSaleHeader(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get sale transaction: " + e.getMessage(), e);
        }
        return null;
    }

    private SaleHeader mapSaleHeader(ResultSet rs) throws SQLException {
        UUID   transactionId   = UUID.fromString(rs.getString("transaction_id"));
        String receiptNumber   = rs.getString("receipt_number");
        double grandTotal      = rs.getDouble("grand_total");
        Date   transactionDate = new Date(rs.getLong("transaction_date"));
        return new SaleHeader(transactionId, receiptNumber, grandTotal, transactionDate);
    }
}
