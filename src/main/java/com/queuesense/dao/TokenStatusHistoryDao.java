package com.queuesense.dao;

import com.queuesense.enums.TokenStatus;
import com.queuesense.exception.DatabaseException;
import com.queuesense.model.TokenStatusHistory;
import com.queuesense.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TokenStatusHistoryDao {

    public void insertHistory(TokenStatusHistory history, Connection conn) throws SQLException {
        String sql = "INSERT INTO token_status_history (token_id, old_status, new_status, changed_by, remarks, changed_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, history.getTokenId());
            if (history.getOldStatus() != null) {
                ps.setString(2, history.getOldStatus().name());
            } else {
                ps.setNull(2, Types.VARCHAR);
            }
            ps.setString(3, history.getNewStatus().name());
            if (history.getChangedBy() != null) {
                ps.setLong(4, history.getChangedBy());
            } else {
                ps.setNull(4, Types.BIGINT);
            }
            ps.setString(5, history.getRemarks());
            ps.setTimestamp(6, Timestamp.valueOf(history.getChangedAt()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    history.setHistoryId(keys.getLong(1));
                }
            }
        }
    }

    public List<TokenStatusHistory> findByTokenId(Long tokenId) {
        List<TokenStatusHistory> list = new ArrayList<>();
        String sql = "SELECT tsh.history_id, tsh.token_id, tsh.old_status, tsh.new_status, " +
                     "tsh.changed_by, u.full_name AS changed_by_name, tsh.remarks, tsh.changed_at " +
                     "FROM token_status_history tsh " +
                     "LEFT JOIN users u ON tsh.changed_by = u.user_id " +
                     "WHERE tsh.token_id = ? ORDER BY tsh.history_id ASC";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, tokenId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TokenStatusHistory h = new TokenStatusHistory();
                    h.setHistoryId(rs.getLong("history_id"));
                    h.setTokenId(rs.getLong("token_id"));
                    String oldS = rs.getString("old_status");
                    if (oldS != null) h.setOldStatus(TokenStatus.valueOf(oldS));
                    h.setNewStatus(TokenStatus.valueOf(rs.getString("new_status")));
                    long uid = rs.getLong("changed_by");
                    if (!rs.wasNull()) h.setChangedBy(uid);
                    h.setChangedByName(rs.getString("changed_by_name"));
                    h.setRemarks(rs.getString("remarks"));
                    Timestamp ca = rs.getTimestamp("changed_at");
                    if (ca != null) h.setChangedAt(ca.toLocalDateTime());
                    list.add(h);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving status history for token ID: " + tokenId, e);
        }
        return list;
    }
}
