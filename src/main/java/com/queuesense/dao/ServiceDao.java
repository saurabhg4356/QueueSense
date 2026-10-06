package com.queuesense.dao;

import com.queuesense.exception.DatabaseException;
import com.queuesense.model.Service;
import com.queuesense.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ServiceDao {

    public Optional<Service> findById(Long serviceId) {
        String sql = "SELECT service_id, service_code, service_name, description, average_service_minutes, " +
                     "priority_weight, active, created_at, updated_at FROM services WHERE service_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, serviceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToService(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding service by ID: " + serviceId, e);
        }
        return Optional.empty();
    }

    public Optional<Service> findByCode(String serviceCode) {
        String sql = "SELECT service_id, service_code, service_name, description, average_service_minutes, " +
                     "priority_weight, active, created_at, updated_at FROM services WHERE service_code = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, serviceCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToService(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding service by code: " + serviceCode, e);
        }
        return Optional.empty();
    }

    public List<Service> findAll() {
        List<Service> services = new ArrayList<>();
        String sql = "SELECT service_id, service_code, service_name, description, average_service_minutes, " +
                     "priority_weight, active, created_at, updated_at FROM services ORDER BY service_name ASC";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                services.add(mapResultSetToService(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving all services", e);
        }
        return services;
    }

    public List<Service> findAllActive() {
        List<Service> services = new ArrayList<>();
        String sql = "SELECT service_id, service_code, service_name, description, average_service_minutes, " +
                     "priority_weight, active, created_at, updated_at FROM services WHERE active = TRUE ORDER BY service_name ASC";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                services.add(mapResultSetToService(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving active services", e);
        }
        return services;
    }

    public Service create(Service service) {
        String sql = "INSERT INTO services (service_code, service_name, description, average_service_minutes, priority_weight, active) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, service.getServiceCode().toUpperCase().trim());
            ps.setString(2, service.getServiceName().trim());
            ps.setString(3, service.getDescription());
            ps.setInt(4, service.getAverageServiceMinutes());
            ps.setInt(5, service.getPriorityWeight());
            ps.setBoolean(6, service.isActive());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    service.setServiceId(keys.getLong(1));
                }
            }
            return service;
        } catch (SQLException e) {
            throw new DatabaseException("Error creating service: " + service.getServiceName(), e);
        }
    }

    public void update(Service service) {
        String sql = "UPDATE services SET service_code = ?, service_name = ?, description = ?, " +
                     "average_service_minutes = ?, priority_weight = ?, active = ? WHERE service_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, service.getServiceCode().toUpperCase().trim());
            ps.setString(2, service.getServiceName().trim());
            ps.setString(3, service.getDescription());
            ps.setInt(4, service.getAverageServiceMinutes());
            ps.setInt(5, service.getPriorityWeight());
            ps.setBoolean(6, service.isActive());
            ps.setLong(7, service.getServiceId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Error updating service ID: " + service.getServiceId(), e);
        }
    }

    private Service mapResultSetToService(ResultSet rs) throws SQLException {
        Service s = new Service();
        s.setServiceId(rs.getLong("service_id"));
        s.setServiceCode(rs.getString("service_code"));
        s.setServiceName(rs.getString("service_name"));
        s.setDescription(rs.getString("description"));
        s.setAverageServiceMinutes(rs.getInt("average_service_minutes"));
        s.setPriorityWeight(rs.getInt("priority_weight"));
        s.setActive(rs.getBoolean("active"));
        Timestamp ct = rs.getTimestamp("created_at");
        if (ct != null) s.setCreatedAt(ct.toLocalDateTime());
        Timestamp ut = rs.getTimestamp("updated_at");
        if (ut != null) s.setUpdatedAt(ut.toLocalDateTime());
        return s;
    }
}
