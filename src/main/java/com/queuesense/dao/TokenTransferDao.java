package com.queuesense.dao;

import com.queuesense.enums.TransferReason;
import com.queuesense.exception.DatabaseException;
import com.queuesense.model.TokenTransfer;
import com.queuesense.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TokenTransferDao {

    public void insertTransfer(TokenTransfer transfer, Connection conn) throws SQLException {
        String sql = "INSERT INTO token_transfers (token_id, from_counter_id, to_counter_id, reason, " +
                     "transferred_by, transferred_at, remarks) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, transfer.getTokenId());
            ps.setLong(2, transfer.getFromCounterId());
            ps.setLong(3, transfer.getToCounterId());
            ps.setString(4, transfer.getReason().name());
            ps.setLong(5, transfer.getTransferredBy());
            ps.setTimestamp(6, Timestamp.valueOf(transfer.getTransferredAt()));
            ps.setString(7, transfer.getRemarks());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    transfer.setTransferId(keys.getLong(1));
                }
            }
        }
    }

    public List<TokenTransfer> findByTokenId(Long tokenId) {
        List<TokenTransfer> list = new ArrayList<>();
        String sql = "SELECT tt.transfer_id, tt.token_id, tt.from_counter_id, c1.counter_number AS from_number, " +
                     "tt.to_counter_id, c2.counter_number AS to_number, tt.reason, tt.transferred_by, " +
                     "u.full_name AS transferred_by_name, tt.transferred_at, tt.remarks " +
                     "FROM token_transfers tt " +
                     "JOIN counters c1 ON tt.from_counter_id = c1.counter_id " +
                     "JOIN counters c2 ON tt.to_counter_id = c2.counter_id " +
                     "JOIN users u ON tt.transferred_by = u.user_id " +
                     "WHERE tt.token_id = ? ORDER BY tt.transfer_id ASC";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, tokenId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TokenTransfer t = new TokenTransfer();
                    t.setTransferId(rs.getLong("transfer_id"));
                    t.setTokenId(rs.getLong("token_id"));
                    t.setFromCounterId(rs.getLong("from_counter_id"));
                    t.setFromCounterNumber(rs.getInt("from_number"));
                    t.setToCounterId(rs.getLong("to_counter_id"));
                    t.setToCounterNumber(rs.getInt("to_number"));
                    t.setReason(TransferReason.valueOf(rs.getString("reason")));
                    t.setTransferredBy(rs.getLong("transferred_by"));
                    t.setTransferredByName(rs.getString("transferred_by_name"));
                    Timestamp ta = rs.getTimestamp("transferred_at");
                    if (ta != null) t.setTransferredAt(ta.toLocalDateTime());
                    t.setRemarks(rs.getString("remarks"));
                    list.add(t);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving transfers for token ID: " + tokenId, e);
        }
        return list;
    }
}
