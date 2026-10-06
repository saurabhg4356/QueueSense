package com.queuesense.dao;

import com.queuesense.enums.PauseReason;
import com.queuesense.exception.DatabaseException;
import com.queuesense.model.ServicePause;
import com.queuesense.model.ServiceSession;
import com.queuesense.util.ConnectionManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ServiceSessionDao {

    public ServiceSession startSession(ServiceSession session, Connection conn) throws SQLException {
        String sql = "INSERT INTO service_sessions (token_id, counter_id, started_at) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, session.getTokenId());
            ps.setLong(2, session.getCounterId());
            ps.setTimestamp(3, Timestamp.valueOf(session.getStartedAt()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    session.setSessionId(keys.getLong(1));
                }
            }
        }
        return session;
    }

    public Optional<ServiceSession> findActiveSessionByTokenId(Long tokenId) {
        String sql = "SELECT session_id, token_id, counter_id, started_at, paused_at, resumed_at, " +
                     "completed_at, actual_duration_seconds, remarks FROM service_sessions " +
                     "WHERE token_id = ? AND completed_at IS NULL ORDER BY session_id DESC LIMIT 1";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, tokenId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSession(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding active service session for token ID: " + tokenId, e);
        }
        return Optional.empty();
    }

    public Optional<ServiceSession> findActiveSessionByCounterId(Long counterId) {
        String sql = "SELECT session_id, token_id, counter_id, started_at, paused_at, resumed_at, " +
                     "completed_at, actual_duration_seconds, remarks FROM service_sessions " +
                     "WHERE counter_id = ? AND completed_at IS NULL ORDER BY session_id DESC LIMIT 1";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, counterId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSession(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding active service session for counter ID: " + counterId, e);
        }
        return Optional.empty();
    }

    public void pauseSession(Long sessionId, PauseReason reason, String remarks, Connection conn) throws SQLException {
        String updateSessionSql = "UPDATE service_sessions SET paused_at = NOW() WHERE session_id = ?";
        String insertPauseSql = "INSERT INTO service_pauses (session_id, pause_reason, paused_at, remarks) VALUES (?, ?, NOW(), ?)";

        try (PreparedStatement ps1 = conn.prepareStatement(updateSessionSql);
             PreparedStatement ps2 = conn.prepareStatement(insertPauseSql)) {
            ps1.setLong(1, sessionId);
            ps1.executeUpdate();

            ps2.setLong(1, sessionId);
            ps2.setString(2, reason.name());
            ps2.setString(3, remarks);
            ps2.executeUpdate();
        }
    }

    public void resumeSession(Long sessionId, Connection conn) throws SQLException {
        // Calculate pause duration and update last pause record
        String updatePauseSql = "UPDATE service_pauses SET resumed_at = NOW(), " +
                                "duration_seconds = TIMESTAMPDIFF(SECOND, paused_at, NOW()) " +
                                "WHERE session_id = ? AND resumed_at IS NULL ORDER BY pause_id DESC LIMIT 1";
        String updateSessionSql = "UPDATE service_sessions SET resumed_at = NOW(), paused_at = NULL WHERE session_id = ?";

        try (PreparedStatement ps1 = conn.prepareStatement(updatePauseSql);
             PreparedStatement ps2 = conn.prepareStatement(updateSessionSql)) {
            ps1.setLong(1, sessionId);
            ps1.executeUpdate();

            ps2.setLong(1, sessionId);
            ps2.executeUpdate();
        }
    }

    public void completeSession(Long sessionId, String remarks, Connection conn) throws SQLException {
        // If there was an open pause, close it first
        String closePauseSql = "UPDATE service_pauses SET resumed_at = NOW(), " +
                               "duration_seconds = TIMESTAMPDIFF(SECOND, paused_at, NOW()) " +
                               "WHERE session_id = ? AND resumed_at IS NULL";
        // Calculate total gross duration minus pauses
        String completeSql =
                "UPDATE service_sessions " +
                "SET completed_at = NOW(), " +
                "    remarks = ?, " +
                "    actual_duration_seconds = GREATEST(0, TIMESTAMPDIFF(SECOND, started_at, NOW()) - " +
                "        IFNULL((SELECT SUM(duration_seconds) FROM service_pauses WHERE session_id = ?), 0)) " +
                "WHERE session_id = ?";

        try (PreparedStatement ps1 = conn.prepareStatement(closePauseSql);
             PreparedStatement ps2 = conn.prepareStatement(completeSql)) {
            ps1.setLong(1, sessionId);
            ps1.executeUpdate();

            ps2.setString(1, remarks);
            ps2.setLong(2, sessionId);
            ps2.setLong(3, sessionId);
            ps2.executeUpdate();
        }
    }

    public List<ServicePause> findPausesBySessionId(Long sessionId) {
        List<ServicePause> list = new ArrayList<>();
        String sql = "SELECT pause_id, session_id, pause_reason, paused_at, resumed_at, duration_seconds, remarks " +
                     "FROM service_pauses WHERE session_id = ? ORDER BY pause_id ASC";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ServicePause p = new ServicePause();
                    p.setPauseId(rs.getLong("pause_id"));
                    p.setSessionId(rs.getLong("session_id"));
                    p.setPauseReason(PauseReason.valueOf(rs.getString("pause_reason")));
                    Timestamp pa = rs.getTimestamp("paused_at");
                    if (pa != null) p.setPausedAt(pa.toLocalDateTime());
                    Timestamp ra = rs.getTimestamp("resumed_at");
                    if (ra != null) p.setResumedAt(ra.toLocalDateTime());
                    p.setDurationSeconds(rs.getInt("duration_seconds"));
                    p.setRemarks(rs.getString("remarks"));
                    list.add(p);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving pauses for session ID: " + sessionId, e);
        }
        return list;
    }

    public double getHistoricalAverageServiceMinutes(Long serviceId) {
        String sql = "SELECT AVG(ss.actual_duration_seconds) / 60.0 AS avg_minutes " +
                     "FROM service_sessions ss " +
                     "JOIN tokens t ON ss.token_id = t.token_id " +
                     "WHERE t.service_id = ? AND ss.completed_at IS NOT NULL AND ss.actual_duration_seconds > 0";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, serviceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double val = rs.getDouble("avg_minutes");
                    return rs.wasNull() ? 0.0 : val;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error calculating historical avg service minutes for service ID: " + serviceId, e);
        }
        return 0.0;
    }

    private ServiceSession mapResultSetToSession(ResultSet rs) throws SQLException {
        ServiceSession s = new ServiceSession();
        s.setSessionId(rs.getLong("session_id"));
        s.setTokenId(rs.getLong("token_id"));
        s.setCounterId(rs.getLong("counter_id"));
        Timestamp sa = rs.getTimestamp("started_at");
        if (sa != null) s.setStartedAt(sa.toLocalDateTime());
        Timestamp pa = rs.getTimestamp("paused_at");
        if (pa != null) s.setPausedAt(pa.toLocalDateTime());
        Timestamp ra = rs.getTimestamp("resumed_at");
        if (ra != null) s.setResumedAt(ra.toLocalDateTime());
        Timestamp ca = rs.getTimestamp("completed_at");
        if (ca != null) s.setCompletedAt(ca.toLocalDateTime());
        s.setActualDurationSeconds(rs.getInt("actual_duration_seconds"));
        s.setRemarks(rs.getString("remarks"));
        return s;
    }
}
