package com.queuesense.dao;

import com.queuesense.exception.DatabaseException;
import com.queuesense.model.AuditLog;
import com.queuesense.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDao {

    public void insertLog(AuditLog log) {
        String sql = "INSERT INTO audit_logs (user_id, action, entity_type, entity_id, details, ip_address, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (log.getUserId() != null) {
                ps.setLong(1, log.getUserId());
            } else {
                ps.setNull(1, Types.BIGINT);
            }
            ps.setString(2, log.getAction());
            ps.setString(3, log.getEntityType());
            ps.setString(4, log.getEntityId());
            ps.setString(5, log.getDetails());
            ps.setString(6, log.getIpAddress());
            ps.setTimestamp(7, Timestamp.valueOf(log.getCreatedAt()));
            ps.executeUpdate();
        } catch (SQLException e) {
            // Audit logging failure should not crash main transaction but be logged
            System.err.println("Audit log error: " + e.getMessage());
        }
    }

    public void insertLog(AuditLog log, Connection conn) throws SQLException {
        String sql = "INSERT INTO audit_logs (user_id, action, entity_type, entity_id, details, ip_address, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (log.getUserId() != null) {
                ps.setLong(1, log.getUserId());
            } else {
                ps.setNull(1, Types.BIGINT);
            }
            ps.setString(2, log.getAction());
            ps.setString(3, log.getEntityType());
            ps.setString(4, log.getEntityId());
            ps.setString(5, log.getDetails());
            ps.setString(6, log.getIpAddress());
            ps.setTimestamp(7, Timestamp.valueOf(log.getCreatedAt()));
            ps.executeUpdate();
        }
    }

    public List<AuditLog> findAll(int limit, int offset) {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT al.log_id, al.user_id, u.username, al.action, al.entity_type, al.entity_id, " +
                     "al.details, al.ip_address, al.created_at " +
                     "FROM audit_logs al " +
                     "LEFT JOIN users u ON al.user_id = u.user_id " +
                     "ORDER BY al.log_id DESC LIMIT ? OFFSET ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AuditLog a = new AuditLog();
                    a.setLogId(rs.getLong("log_id"));
                    long uid = rs.getLong("user_id");
                    if (!rs.wasNull()) a.setUserId(uid);
                    a.setUsername(rs.getString("username"));
                    a.setAction(rs.getString("action"));
                    a.setEntityType(rs.getString("entity_type"));
                    a.setEntityId(rs.getString("entity_id"));
                    a.setDetails(rs.getString("details"));
                    a.setIpAddress(rs.getString("ip_address"));
                    Timestamp ca = rs.getTimestamp("created_at");
                    if (ca != null) a.setCreatedAt(ca.toLocalDateTime());
                    list.add(a);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving audit logs", e);
        }
        return list;
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM audit_logs";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting audit logs", e);
        }
        return 0;
    }
}
