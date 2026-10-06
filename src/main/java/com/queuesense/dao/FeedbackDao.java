package com.queuesense.dao;

import com.queuesense.exception.DatabaseException;
import com.queuesense.model.Feedback;
import com.queuesense.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FeedbackDao {

    public Feedback insert(Feedback feedback) {
        String sql = "INSERT INTO feedback (token_id, customer_id, rating, comment) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, feedback.getTokenId());
            ps.setLong(2, feedback.getCustomerId());
            ps.setInt(3, feedback.getRating());
            ps.setString(4, feedback.getComment());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    feedback.setFeedbackId(keys.getLong(1));
                }
            }
            return feedback;
        } catch (SQLException e) {
            throw new DatabaseException("Error submitting feedback for token ID: " + feedback.getTokenId(), e);
        }
    }

    public Optional<Feedback> findByTokenId(Long tokenId) {
        String sql = "SELECT f.feedback_id, f.token_id, t.token_number, f.customer_id, " +
                     "c.full_name AS customer_name, f.rating, f.comment, f.created_at " +
                     "FROM feedback f " +
                     "JOIN tokens t ON f.token_id = t.token_id " +
                     "JOIN customers c ON f.customer_id = c.customer_id " +
                     "WHERE f.token_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, tokenId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToFeedback(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding feedback by token ID: " + tokenId, e);
        }
        return Optional.empty();
    }

    public List<Feedback> findAll(int limit, int offset) {
        List<Feedback> list = new ArrayList<>();
        String sql = "SELECT f.feedback_id, f.token_id, t.token_number, f.customer_id, " +
                     "c.full_name AS customer_name, f.rating, f.comment, f.created_at " +
                     "FROM feedback f " +
                     "JOIN tokens t ON f.token_id = t.token_id " +
                     "JOIN customers c ON f.customer_id = c.customer_id " +
                     "ORDER BY f.feedback_id DESC LIMIT ? OFFSET ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToFeedback(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving feedback list", e);
        }
        return list;
    }

    public double getAverageRating() {
        String sql = "SELECT AVG(rating) FROM feedback";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                double avg = rs.getDouble(1);
                return rs.wasNull() ? 0.0 : avg;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error calculating average rating", e);
        }
        return 0.0;
    }

    private Feedback mapResultSetToFeedback(ResultSet rs) throws SQLException {
        Feedback f = new Feedback();
        f.setFeedbackId(rs.getLong("feedback_id"));
        f.setTokenId(rs.getLong("token_id"));
        f.setTokenNumber(rs.getString("token_number"));
        f.setCustomerId(rs.getLong("customer_id"));
        f.setCustomerName(rs.getString("customer_name"));
        f.setRating(rs.getInt("rating"));
        f.setComment(rs.getString("comment"));
        Timestamp ct = rs.getTimestamp("created_at");
        if (ct != null) f.setCreatedAt(ct.toLocalDateTime());
        return f;
    }
}
