package com.queuesense.dao;

import com.queuesense.enums.PriorityType;
import com.queuesense.enums.TokenStatus;
import com.queuesense.exception.DatabaseException;
import com.queuesense.model.Token;
import com.queuesense.util.ConnectionManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TokenDao {

    private static final String BASE_SELECT =
            "SELECT t.token_id, t.token_number, t.customer_id, c.full_name AS customer_name, " +
            "c.customer_number, c.special_assistance_required, t.service_id, s.service_name, " +
            "s.service_code, s.average_service_minutes, s.priority_weight AS service_priority_weight, " +
            "t.priority_type, t.priority_score, t.status, t.counter_id, cnt.counter_number, " +
            "cnt.counter_name, t.issued_at, t.called_at, t.service_started_at, t.service_completed_at, " +
            "t.cancelled_at, t.cancelled_by, t.cancellation_reason, t.no_show_at, " +
            "t.estimated_wait_minutes, t.actual_wait_minutes, t.created_at, t.updated_at " +
            "FROM tokens t " +
            "JOIN customers c ON t.customer_id = c.customer_id " +
            "JOIN services s ON t.service_id = s.service_id " +
            "LEFT JOIN counters cnt ON t.counter_id = cnt.counter_id ";

    public Optional<Token> findById(Long tokenId) {
        String sql = BASE_SELECT + "WHERE t.token_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, tokenId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToToken(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding token by ID: " + tokenId, e);
        }
        return Optional.empty();
    }

    public Optional<Token> findById(Long tokenId, Connection conn) throws SQLException {
        String sql = BASE_SELECT + "WHERE t.token_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, tokenId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToToken(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Token> findByTokenNumber(String tokenNumber) {
        String sql = BASE_SELECT + "WHERE t.token_number = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tokenNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToToken(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding token by number: " + tokenNumber, e);
        }
        return Optional.empty();
    }

    public List<Token> findWaitingTokens() {
        List<Token> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE t.status = 'WAITING' ORDER BY t.issued_at ASC";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToToken(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving waiting tokens", e);
        }
        return list;
    }

    public List<Token> findWaitingTokensByService(Long serviceId) {
        List<Token> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE t.status = 'WAITING' AND t.service_id = ? ORDER BY t.issued_at ASC";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, serviceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToToken(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving waiting tokens for service: " + serviceId, e);
        }
        return list;
    }

    public Optional<Token> findCurrentServingTokenForCounter(Long counterId) {
        String sql = BASE_SELECT + "WHERE t.counter_id = ? AND t.status IN ('CALLED', 'SERVING', 'PAUSED') ORDER BY t.called_at DESC LIMIT 1";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, counterId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToToken(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding serving token for counter: " + counterId, e);
        }
        return Optional.empty();
    }

    public List<Token> findActiveTokensByCustomerId(Long customerId) {
        List<Token> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE t.customer_id = ? AND t.status IN ('WAITING', 'CALLED', 'SERVING', 'PAUSED') ORDER BY t.token_id DESC";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToToken(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding active tokens for customer ID: " + customerId, e);
        }
        return list;
    }

    public List<Token> findHistoryByCustomerId(Long customerId) {
        List<Token> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE t.customer_id = ? ORDER BY t.token_id DESC LIMIT 50";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToToken(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding history tokens for customer ID: " + customerId, e);
        }
        return list;
    }

    public Token create(Token token, Connection conn) throws SQLException {
        String sql = "INSERT INTO tokens (token_number, customer_id, service_id, priority_type, priority_score, " +
                     "status, estimated_wait_minutes, issued_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, token.getTokenNumber());
            ps.setLong(2, token.getCustomerId());
            ps.setLong(3, token.getServiceId());
            ps.setString(4, token.getPriorityType().name());
            ps.setDouble(5, token.getPriorityScore());
            ps.setString(6, token.getStatus().name());
            ps.setInt(7, token.getEstimatedWaitMinutes());
            ps.setTimestamp(8, Timestamp.valueOf(token.getIssuedAt()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    token.setTokenId(keys.getLong(1));
                }
            }
        }
        return token;
    }

    /**
     * Atomically calls a waiting token and binds it to a counter.
     * Prevents double-call race conditions: returns true ONLY if this transaction won the lock.
     */
    public boolean callTokenAtomically(Long tokenId, Long counterId, Connection conn) throws SQLException {
        String sql = "UPDATE tokens SET status = 'CALLED', counter_id = ?, called_at = NOW() " +
                     "WHERE token_id = ? AND status = 'WAITING'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, counterId);
            ps.setLong(2, tokenId);
            int rowsUpdated = ps.executeUpdate();
            return rowsUpdated == 1;
        }
    }

    public void updateStatus(Long tokenId, TokenStatus newStatus, Connection conn) throws SQLException {
        String sql;
        if (newStatus == TokenStatus.SERVING) {
            sql = "UPDATE tokens SET status = ?, service_started_at = IFNULL(service_started_at, NOW()) WHERE token_id = ?";
        } else if (newStatus == TokenStatus.COMPLETED) {
            sql = "UPDATE tokens SET status = ?, service_completed_at = NOW(), " +
                  "actual_wait_minutes = TIMESTAMPDIFF(MINUTE, issued_at, service_started_at) WHERE token_id = ?";
        } else if (newStatus == TokenStatus.NO_SHOW) {
            sql = "UPDATE tokens SET status = ?, no_show_at = NOW() WHERE token_id = ?";
        } else {
            sql = "UPDATE tokens SET status = ? WHERE token_id = ?";
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus.name());
            ps.setLong(2, tokenId);
            ps.executeUpdate();
        }
    }

    public void cancelToken(Long tokenId, Long userId, String reason, Connection conn) throws SQLException {
        String sql = "UPDATE tokens SET status = 'CANCELLED', cancelled_at = NOW(), cancelled_by = ?, " +
                     "cancellation_reason = ? WHERE token_id = ? AND status IN ('WAITING', 'CALLED')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (userId != null) {
                ps.setLong(1, userId);
            } else {
                ps.setNull(1, Types.BIGINT);
            }
            ps.setString(2, reason);
            ps.setLong(3, tokenId);
            ps.executeUpdate();
        }
    }

    public void updateAssignedCounter(Long tokenId, Long counterId, Connection conn) throws SQLException {
        String sql = "UPDATE tokens SET counter_id = ? WHERE token_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (counterId != null) {
                ps.setLong(1, counterId);
            } else {
                ps.setNull(1, Types.BIGINT);
            }
            ps.setLong(2, tokenId);
            ps.executeUpdate();
        }
    }

    public void updatePriorityScore(Long tokenId, double newScore, Connection conn) throws SQLException {
        String sql = "UPDATE tokens SET priority_score = ? WHERE token_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, newScore);
            ps.setLong(2, tokenId);
            ps.executeUpdate();
        }
    }

    public void updateEstimatedWait(Long tokenId, int waitMinutes, Connection conn) throws SQLException {
        String sql = "UPDATE tokens SET estimated_wait_minutes = ? WHERE token_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, waitMinutes);
            ps.setLong(2, tokenId);
            ps.executeUpdate();
        }
    }

    public List<Token> searchTokens(String query, TokenStatus status, PriorityType priority,
                                    Long serviceId, Long counterId, LocalDate startDate,
                                    LocalDate endDate, int limit, int offset) {
        List<Token> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT).append("WHERE 1=1 ");

        boolean hasQuery = query != null && !query.trim().isEmpty();
        if (hasQuery) {
            sql.append("AND (t.token_number LIKE ? OR c.customer_number LIKE ? OR c.full_name LIKE ?) ");
        }
        if (status != null) {
            sql.append("AND t.status = ? ");
        }
        if (priority != null) {
            sql.append("AND t.priority_type = ? ");
        }
        if (serviceId != null) {
            sql.append("AND t.service_id = ? ");
        }
        if (counterId != null) {
            sql.append("AND t.counter_id = ? ");
        }
        if (startDate != null) {
            sql.append("AND t.issued_at >= ? ");
        }
        if (endDate != null) {
            sql.append("AND t.issued_at <= ? ");
        }

        sql.append("ORDER BY t.token_id DESC LIMIT ? OFFSET ?");

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (hasQuery) {
                String term = "%" + query.trim() + "%";
                ps.setString(idx++, term);
                ps.setString(idx++, term);
                ps.setString(idx++, term);
            }
            if (status != null) ps.setString(idx++, status.name());
            if (priority != null) ps.setString(idx++, priority.name());
            if (serviceId != null) ps.setLong(idx++, serviceId);
            if (counterId != null) ps.setLong(idx++, counterId);
            if (startDate != null) ps.setTimestamp(idx++, Timestamp.valueOf(startDate.atStartOfDay()));
            if (endDate != null) ps.setTimestamp(idx++, Timestamp.valueOf(endDate.atTime(23, 59, 59)));
            ps.setInt(idx++, limit);
            ps.setInt(idx, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToToken(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error searching tokens", e);
        }
        return list;
    }

    public int countSearchTokens(String query, TokenStatus status, PriorityType priority,
                                  Long serviceId, Long counterId, LocalDate startDate, LocalDate endDate) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM tokens t " +
                "JOIN customers c ON t.customer_id = c.customer_id " +
                "JOIN services s ON t.service_id = s.service_id " +
                "LEFT JOIN counters cnt ON t.counter_id = cnt.counter_id WHERE 1=1 ");

        boolean hasQuery = query != null && !query.trim().isEmpty();
        if (hasQuery) {
            sql.append("AND (t.token_number LIKE ? OR c.customer_number LIKE ? OR c.full_name LIKE ?) ");
        }
        if (status != null) sql.append("AND t.status = ? ");
        if (priority != null) sql.append("AND t.priority_type = ? ");
        if (serviceId != null) sql.append("AND t.service_id = ? ");
        if (counterId != null) sql.append("AND t.counter_id = ? ");
        if (startDate != null) sql.append("AND t.issued_at >= ? ");
        if (endDate != null) sql.append("AND t.issued_at <= ? ");

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (hasQuery) {
                String term = "%" + query.trim() + "%";
                ps.setString(idx++, term);
                ps.setString(idx++, term);
                ps.setString(idx++, term);
            }
            if (status != null) ps.setString(idx++, status.name());
            if (priority != null) ps.setString(idx++, priority.name());
            if (serviceId != null) ps.setLong(idx++, serviceId);
            if (counterId != null) ps.setLong(idx++, counterId);
            if (startDate != null) ps.setTimestamp(idx++, Timestamp.valueOf(startDate.atStartOfDay()));
            if (endDate != null) ps.setTimestamp(idx++, Timestamp.valueOf(endDate.atTime(23, 59, 59)));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting tokens", e);
        }
        return 0;
    }

    public String generateNextTokenNumber(String serviceCode) {
        char prefixChar = (serviceCode != null && !serviceCode.isEmpty()) ? serviceCode.charAt(0) : 'Q';
        String prefix = String.valueOf(prefixChar);
        String sql = "SELECT token_number FROM tokens WHERE token_number LIKE ? ORDER BY token_id DESC LIMIT 1";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String lastNumber = rs.getString(1);
                    try {
                        int num = Integer.parseInt(lastNumber.substring(1));
                        return String.format("%s%03d", prefix, num + 1);
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (Exception ignored) {}
        return prefix + "101";
    }

    private Token mapResultSetToToken(ResultSet rs) throws SQLException {
        Token t = new Token();
        t.setTokenId(rs.getLong("token_id"));
        t.setTokenNumber(rs.getString("token_number"));
        t.setCustomerId(rs.getLong("customer_id"));
        t.setCustomerName(rs.getString("customer_name"));
        t.setCustomerNumber(rs.getString("customer_number"));
        t.setSpecialAssistanceRequired(rs.getBoolean("special_assistance_required"));

        t.setServiceId(rs.getLong("service_id"));
        t.setServiceName(rs.getString("service_name"));
        t.setServiceCode(rs.getString("service_code"));
        t.setServiceAverageMinutes(rs.getInt("average_service_minutes"));
        t.setServicePriorityWeight(rs.getInt("service_priority_weight"));

        t.setPriorityType(PriorityType.valueOf(rs.getString("priority_type")));
        t.setPriorityScore(rs.getDouble("priority_score"));
        t.setStatus(TokenStatus.valueOf(rs.getString("status")));

        long cid = rs.getLong("counter_id");
        if (!rs.wasNull()) {
            t.setCounterId(cid);
            t.setCounterNumber(rs.getInt("counter_number"));
            t.setCounterName(rs.getString("counter_name"));
        }

        Timestamp issued = rs.getTimestamp("issued_at");
        if (issued != null) t.setIssuedAt(issued.toLocalDateTime());

        Timestamp called = rs.getTimestamp("called_at");
        if (called != null) t.setCalledAt(called.toLocalDateTime());

        Timestamp started = rs.getTimestamp("service_started_at");
        if (started != null) t.setServiceStartedAt(started.toLocalDateTime());

        Timestamp comp = rs.getTimestamp("service_completed_at");
        if (comp != null) t.setServiceCompletedAt(comp.toLocalDateTime());

        Timestamp canc = rs.getTimestamp("cancelled_at");
        if (canc != null) t.setCancelledAt(canc.toLocalDateTime());

        long cb = rs.getLong("cancelled_by");
        if (!rs.wasNull()) t.setCancelledBy(cb);
        t.setCancellationReason(rs.getString("cancellation_reason"));

        Timestamp ns = rs.getTimestamp("no_show_at");
        if (ns != null) t.setNoShowAt(ns.toLocalDateTime());

        t.setEstimatedWaitMinutes(rs.getInt("estimated_wait_minutes"));
        int aw = rs.getInt("actual_wait_minutes");
        if (!rs.wasNull()) t.setActualWaitMinutes(aw);

        Timestamp ct = rs.getTimestamp("created_at");
        if (ct != null) t.setCreatedAt(ct.toLocalDateTime());

        Timestamp ut = rs.getTimestamp("updated_at");
        if (ut != null) t.setUpdatedAt(ut.toLocalDateTime());

        return t;
    }
}
