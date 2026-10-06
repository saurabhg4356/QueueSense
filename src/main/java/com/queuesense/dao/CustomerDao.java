package com.queuesense.dao;

import com.queuesense.exception.DatabaseException;
import com.queuesense.model.Customer;
import com.queuesense.util.ConnectionManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerDao {

    public Optional<Customer> findById(Long customerId) {
        String sql = "SELECT customer_id, user_id, customer_number, full_name, email, phone, date_of_birth, " +
                     "special_assistance_required, created_at, updated_at FROM customers WHERE customer_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCustomer(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding customer by ID: " + customerId, e);
        }
        return Optional.empty();
    }

    public Optional<Customer> findByUserId(Long userId) {
        String sql = "SELECT customer_id, user_id, customer_number, full_name, email, phone, date_of_birth, " +
                     "special_assistance_required, created_at, updated_at FROM customers WHERE user_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCustomer(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding customer by user ID: " + userId, e);
        }
        return Optional.empty();
    }

    public Optional<Customer> findByCustomerNumber(String customerNumber) {
        String sql = "SELECT customer_id, user_id, customer_number, full_name, email, phone, date_of_birth, " +
                     "special_assistance_required, created_at, updated_at FROM customers WHERE customer_number = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCustomer(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding customer by number: " + customerNumber, e);
        }
        return Optional.empty();
    }

    public Optional<Customer> findByPhone(String phone) {
        String sql = "SELECT customer_id, user_id, customer_number, full_name, email, phone, date_of_birth, " +
                     "special_assistance_required, created_at, updated_at FROM customers WHERE phone = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCustomer(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding customer by phone: " + phone, e);
        }
        return Optional.empty();
    }

    public List<Customer> search(String query, int limit, int offset) {
        List<Customer> customers = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT customer_id, user_id, customer_number, full_name, email, phone, date_of_birth, " +
                "special_assistance_required, created_at, updated_at FROM customers WHERE 1=1 ");

        boolean hasQuery = query != null && !query.trim().isEmpty();
        if (hasQuery) {
            sql.append("AND (customer_number LIKE ? OR full_name LIKE ? OR phone LIKE ? OR email LIKE ?) ");
        }
        sql.append("ORDER BY customer_id DESC LIMIT ? OFFSET ?");

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (hasQuery) {
                String term = "%" + query.trim() + "%";
                ps.setString(paramIndex++, term);
                ps.setString(paramIndex++, term);
                ps.setString(paramIndex++, term);
                ps.setString(paramIndex++, term);
            }
            ps.setInt(paramIndex++, limit);
            ps.setInt(paramIndex, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    customers.add(mapResultSetToCustomer(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error searching customers", e);
        }
        return customers;
    }

    public int countSearch(String query) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM customers WHERE 1=1 ");
        boolean hasQuery = query != null && !query.trim().isEmpty();
        if (hasQuery) {
            sql.append("AND (customer_number LIKE ? OR full_name LIKE ? OR phone LIKE ? OR email LIKE ?)");
        }

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (hasQuery) {
                String term = "%" + query.trim() + "%";
                ps.setString(1, term);
                ps.setString(2, term);
                ps.setString(3, term);
                ps.setString(4, term);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting customers", e);
        }
        return 0;
    }

    public Customer create(Customer customer) {
        String sql = "INSERT INTO customers (user_id, customer_number, full_name, email, phone, date_of_birth, special_assistance_required) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (customer.getUserId() != null) {
                ps.setLong(1, customer.getUserId());
            } else {
                ps.setNull(1, Types.BIGINT);
            }
            ps.setString(2, customer.getCustomerNumber());
            ps.setString(3, customer.getFullName());
            ps.setString(4, customer.getEmail());
            ps.setString(5, customer.getPhone());
            if (customer.getDateOfBirth() != null) {
                ps.setDate(6, Date.valueOf(customer.getDateOfBirth()));
            } else {
                ps.setNull(6, Types.DATE);
            }
            ps.setBoolean(7, customer.isSpecialAssistanceRequired());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    customer.setCustomerId(keys.getLong(1));
                }
            }
            return customer;
        } catch (SQLException e) {
            throw new DatabaseException("Error creating customer: " + customer.getFullName(), e);
        }
    }

    public void update(Customer customer) {
        String sql = "UPDATE customers SET full_name = ?, email = ?, phone = ?, date_of_birth = ?, special_assistance_required = ? " +
                     "WHERE customer_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customer.getFullName());
            ps.setString(2, customer.getEmail());
            ps.setString(3, customer.getPhone());
            if (customer.getDateOfBirth() != null) {
                ps.setDate(4, Date.valueOf(customer.getDateOfBirth()));
            } else {
                ps.setNull(4, Types.DATE);
            }
            ps.setBoolean(5, customer.isSpecialAssistanceRequired());
            ps.setLong(6, customer.getCustomerId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Error updating customer ID: " + customer.getCustomerId(), e);
        }
    }

    public String generateNextCustomerNumber() {
        int year = LocalDate.now().getYear();
        String prefix = "CUS-" + year + "-";
        String sql = "SELECT customer_number FROM customers WHERE customer_number LIKE ? ORDER BY customer_id DESC LIMIT 1";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String lastNumber = rs.getString(1);
                    String seqStr = lastNumber.substring(prefix.length());
                    int nextSeq = Integer.parseInt(seqStr) + 1;
                    return String.format("%s%05d", prefix, nextSeq);
                }
            }
        } catch (Exception ignored) {}
        return String.format("%s00001", prefix);
    }

    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setCustomerId(rs.getLong("customer_id"));
        long uid = rs.getLong("user_id");
        if (!rs.wasNull()) {
            c.setUserId(uid);
        }
        c.setCustomerNumber(rs.getString("customer_number"));
        c.setFullName(rs.getString("full_name"));
        c.setEmail(rs.getString("email"));
        c.setPhone(rs.getString("phone"));
        Date dob = rs.getDate("date_of_birth");
        if (dob != null) {
            c.setDateOfBirth(dob.toLocalDate());
        }
        c.setSpecialAssistanceRequired(rs.getBoolean("special_assistance_required"));
        Timestamp ct = rs.getTimestamp("created_at");
        if (ct != null) c.setCreatedAt(ct.toLocalDateTime());
        Timestamp ut = rs.getTimestamp("updated_at");
        if (ut != null) c.setUpdatedAt(ut.toLocalDateTime());
        return c;
    }
}
