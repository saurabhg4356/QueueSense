package com.queuesense.dao;

import com.queuesense.enums.CounterStatus;
import com.queuesense.exception.DatabaseException;
import com.queuesense.model.Counter;
import com.queuesense.util.ConnectionManager;

import java.sql.*;
import java.util.*;

public class CounterDao {

    public Optional<Counter> findById(Long counterId) {
        String sql = "SELECT c.counter_id, c.counter_number, c.counter_name, c.location, c.status, " +
                     "c.assigned_agent_id, u.full_name as agent_name, c.created_at, c.updated_at " +
                     "FROM counters c LEFT JOIN users u ON c.assigned_agent_id = u.user_id " +
                     "WHERE c.counter_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, counterId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Counter counter = mapResultSetToCounter(rs);
                    counter.setSupportedServiceIds(findSupportedServiceIds(conn, counter.getCounterId()));
                    return Optional.of(counter);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding counter by ID: " + counterId, e);
        }
        return Optional.empty();
    }

    public Optional<Counter> findByNumber(int counterNumber) {
        String sql = "SELECT c.counter_id, c.counter_number, c.counter_name, c.location, c.status, " +
                     "c.assigned_agent_id, u.full_name as agent_name, c.created_at, c.updated_at " +
                     "FROM counters c LEFT JOIN users u ON c.assigned_agent_id = u.user_id " +
                     "WHERE c.counter_number = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, counterNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Counter counter = mapResultSetToCounter(rs);
                    counter.setSupportedServiceIds(findSupportedServiceIds(conn, counter.getCounterId()));
                    return Optional.of(counter);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding counter by number: " + counterNumber, e);
        }
        return Optional.empty();
    }

    public Optional<Counter> findByAssignedAgentId(Long agentId) {
        String sql = "SELECT c.counter_id, c.counter_number, c.counter_name, c.location, c.status, " +
                     "c.assigned_agent_id, u.full_name as agent_name, c.created_at, c.updated_at " +
                     "FROM counters c LEFT JOIN users u ON c.assigned_agent_id = u.user_id " +
                     "WHERE c.assigned_agent_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, agentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Counter counter = mapResultSetToCounter(rs);
                    counter.setSupportedServiceIds(findSupportedServiceIds(conn, counter.getCounterId()));
                    return Optional.of(counter);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding counter by agent ID: " + agentId, e);
        }
        return Optional.empty();
    }

    public List<Counter> findAll() {
        List<Counter> counters = new ArrayList<>();
        String sql = "SELECT c.counter_id, c.counter_number, c.counter_name, c.location, c.status, " +
                     "c.assigned_agent_id, u.full_name as agent_name, c.created_at, c.updated_at " +
                     "FROM counters c LEFT JOIN users u ON c.assigned_agent_id = u.user_id " +
                     "ORDER BY c.counter_number ASC";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                counters.add(mapResultSetToCounter(rs));
            }
            // Populate compatibility mappings
            Map<Long, Set<Long>> compatibilityMap = loadAllCounterServices(conn);
            for (Counter c : counters) {
                c.setSupportedServiceIds(compatibilityMap.getOrDefault(c.getCounterId(), new HashSet<>()));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving counters", e);
        }
        return counters;
    }

    public List<Counter> findCompatibleCountersForService(Long serviceId) {
        List<Counter> compatible = new ArrayList<>();
        String sql = "SELECT c.counter_id, c.counter_number, c.counter_name, c.location, c.status, " +
                     "c.assigned_agent_id, u.full_name as agent_name, c.created_at, c.updated_at " +
                     "FROM counters c " +
                     "JOIN counter_services cs ON c.counter_id = cs.counter_id " +
                     "LEFT JOIN users u ON c.assigned_agent_id = u.user_id " +
                     "WHERE cs.service_id = ? AND cs.enabled = TRUE " +
                     "ORDER BY c.counter_number ASC";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, serviceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    compatible.add(mapResultSetToCounter(rs));
                }
            }
            Map<Long, Set<Long>> map = loadAllCounterServices(conn);
            for (Counter c : compatible) {
                c.setSupportedServiceIds(map.getOrDefault(c.getCounterId(), new HashSet<>()));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving compatible counters for service ID: " + serviceId, e);
        }
        return compatible;
    }

    public Counter create(Counter counter) {
        String sql = "INSERT INTO counters (counter_number, counter_name, location, status, assigned_agent_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, counter.getCounterNumber());
            ps.setString(2, counter.getCounterName());
            ps.setString(3, counter.getLocation());
            ps.setString(4, counter.getStatus().name());
            if (counter.getAssignedAgentId() != null) {
                ps.setLong(5, counter.getAssignedAgentId());
            } else {
                ps.setNull(5, Types.BIGINT);
            }
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    counter.setCounterId(keys.getLong(1));
                }
            }
            return counter;
        } catch (SQLException e) {
            throw new DatabaseException("Error creating counter: " + counter.getCounterName(), e);
        }
    }

    public void update(Counter counter) {
        String sql = "UPDATE counters SET counter_number = ?, counter_name = ?, location = ?, status = ?, assigned_agent_id = ? " +
                     "WHERE counter_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, counter.getCounterNumber());
            ps.setString(2, counter.getCounterName());
            ps.setString(3, counter.getLocation());
            ps.setString(4, counter.getStatus().name());
            if (counter.getAssignedAgentId() != null) {
                ps.setLong(5, counter.getAssignedAgentId());
            } else {
                ps.setNull(5, Types.BIGINT);
            }
            ps.setLong(6, counter.getCounterId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Error updating counter ID: " + counter.getCounterId(), e);
        }
    }

    public void updateStatus(Long counterId, CounterStatus status) {
        String sql = "UPDATE counters SET status = ? WHERE counter_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setLong(2, counterId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Error updating status for counter ID: " + counterId, e);
        }
    }

    public void updateCounterServices(Long counterId, Set<Long> serviceIds) {
        String deleteSql = "DELETE FROM counter_services WHERE counter_id = ?";
        String insertSql = "INSERT INTO counter_services (counter_id, service_id, enabled) VALUES (?, ?, TRUE)";

        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement delPs = conn.prepareStatement(deleteSql)) {
                delPs.setLong(1, counterId);
                delPs.executeUpdate();
            }

            if (serviceIds != null && !serviceIds.isEmpty()) {
                try (PreparedStatement insPs = conn.prepareStatement(insertSql)) {
                    for (Long sid : serviceIds) {
                        insPs.setLong(1, counterId);
                        insPs.setLong(2, sid);
                        insPs.addBatch();
                    }
                    insPs.executeBatch();
                }
            }

            conn.commit();
        } catch (SQLException e) {
            ConnectionManager.rollbackQuietly(conn);
            throw new DatabaseException("Error updating services for counter ID: " + counterId, e);
        } finally {
            ConnectionManager.closeQuietly(conn);
        }
    }

    private Set<Long> findSupportedServiceIds(Connection conn, Long counterId) throws SQLException {
        Set<Long> set = new HashSet<>();
        String sql = "SELECT service_id FROM counter_services WHERE counter_id = ? AND enabled = TRUE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, counterId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    set.add(rs.getLong("service_id"));
                }
            }
        }
        return set;
    }

    private Map<Long, Set<Long>> loadAllCounterServices(Connection conn) throws SQLException {
        Map<Long, Set<Long>> map = new HashMap<>();
        String sql = "SELECT counter_id, service_id FROM counter_services WHERE enabled = TRUE";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                long cid = rs.getLong("counter_id");
                long sid = rs.getLong("service_id");
                map.computeIfAbsent(cid, k -> new HashSet<>()).add(sid);
            }
        }
        return map;
    }

    private Counter mapResultSetToCounter(ResultSet rs) throws SQLException {
        Counter c = new Counter();
        c.setCounterId(rs.getLong("counter_id"));
        c.setCounterNumber(rs.getInt("counter_number"));
        c.setCounterName(rs.getString("counter_name"));
        c.setLocation(rs.getString("location"));
        c.setStatus(CounterStatus.valueOf(rs.getString("status")));
        long aid = rs.getLong("assigned_agent_id");
        if (!rs.wasNull()) {
            c.setAssignedAgentId(aid);
        }
        c.setAssignedAgentName(rs.getString("agent_name"));
        Timestamp ct = rs.getTimestamp("created_at");
        if (ct != null) c.setCreatedAt(ct.toLocalDateTime());
        Timestamp ut = rs.getTimestamp("updated_at");
        if (ut != null) c.setUpdatedAt(ut.toLocalDateTime());
        return c;
    }
}
