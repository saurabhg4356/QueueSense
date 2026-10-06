package com.queuesense.dao;

import com.queuesense.enums.UserRole;
import com.queuesense.exception.DatabaseException;
import com.queuesense.model.User;
import com.queuesense.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao {

    public Optional<User> findByUsername(String username) {
        String sql = "SELECT user_id, username, password_hash, role, full_name, email, active, created_at, updated_at " +
                     "FROM users WHERE username = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding user by username: " + username, e);
        }
        return Optional.empty();
    }

    public Optional<User> findById(Long userId) {
        String sql = "SELECT user_id, username, password_hash, role, full_name, email, active, created_at, updated_at " +
                     "FROM users WHERE user_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding user by ID: " + userId, e);
        }
        return Optional.empty();
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, username, password_hash, role, full_name, email, active, created_at, updated_at " +
                     "FROM users ORDER BY user_id ASC";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving all users", e);
        }
        return users;
    }

    public User create(User user) {
        String sql = "INSERT INTO users (username, password_hash, role, full_name, email, active) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getRole().name());
            ps.setString(4, user.getFullName());
            ps.setString(5, user.getEmail());
            ps.setBoolean(6, user.isActive());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setUserId(keys.getLong(1));
                }
            }
            return user;
        } catch (SQLException e) {
            throw new DatabaseException("Error creating user: " + user.getUsername(), e);
        }
    }

    public void update(User user) {
        String sql = "UPDATE users SET role = ?, full_name = ?, email = ?, active = ? WHERE user_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getRole().name());
            ps.setString(2, user.getFullName());
            ps.setString(3, user.getEmail());
            ps.setBoolean(4, user.isActive());
            ps.setLong(5, user.getUserId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Error updating user ID: " + user.getUserId(), e);
        }
    }

    public void updatePassword(Long userId, String passwordHash) {
        String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setLong(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Error updating password for user ID: " + userId, e);
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getLong("user_id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRole(UserRole.valueOf(rs.getString("role")));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setActive(rs.getBoolean("active"));
        Timestamp ct = rs.getTimestamp("created_at");
        if (ct != null) u.setCreatedAt(ct.toLocalDateTime());
        Timestamp ut = rs.getTimestamp("updated_at");
        if (ut != null) u.setUpdatedAt(ut.toLocalDateTime());
        return u;
    }
}
