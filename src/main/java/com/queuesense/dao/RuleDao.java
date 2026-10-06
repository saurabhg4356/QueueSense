package com.queuesense.dao;

import com.queuesense.enums.PriorityType;
import com.queuesense.exception.DatabaseException;
import com.queuesense.model.AgingRule;
import com.queuesense.model.PriorityRule;
import com.queuesense.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RuleDao {

    public List<PriorityRule> findAllPriorityRules() {
        List<PriorityRule> list = new ArrayList<>();
        String sql = "SELECT id, priority_type, base_weight, description, active FROM priority_rules ORDER BY base_weight DESC";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PriorityRule r = new PriorityRule();
                r.setId(rs.getLong("id"));
                r.setPriorityType(PriorityType.valueOf(rs.getString("priority_type")));
                r.setBaseWeight(rs.getInt("base_weight"));
                r.setDescription(rs.getString("description"));
                r.setActive(rs.getBoolean("active"));
                list.add(r);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving priority rules", e);
        }
        return list;
    }

    public Optional<PriorityRule> findPriorityRuleByType(PriorityType type) {
        String sql = "SELECT id, priority_type, base_weight, description, active FROM priority_rules WHERE priority_type = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PriorityRule r = new PriorityRule();
                    r.setId(rs.getLong("id"));
                    r.setPriorityType(PriorityType.valueOf(rs.getString("priority_type")));
                    r.setBaseWeight(rs.getInt("base_weight"));
                    r.setDescription(rs.getString("description"));
                    r.setActive(rs.getBoolean("active"));
                    return Optional.of(r);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding priority rule by type: " + type, e);
        }
        return Optional.empty();
    }

    public void updatePriorityRule(PriorityRule rule) {
        String sql = "UPDATE priority_rules SET base_weight = ?, description = ?, active = ? WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, rule.getBaseWeight());
            ps.setString(2, rule.getDescription());
            ps.setBoolean(3, rule.isActive());
            ps.setLong(4, rule.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Error updating priority rule ID: " + rule.getId(), e);
        }
    }

    public AgingRule getActiveAgingRule() {
        String sql = "SELECT id, minutes_interval, bonus_points, maximum_bonus, starvation_threshold_minutes, " +
                     "special_assistance_bonus, active FROM aging_rules WHERE active = TRUE ORDER BY id ASC LIMIT 1";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                AgingRule r = new AgingRule();
                r.setId(rs.getLong("id"));
                r.setMinutesInterval(rs.getInt("minutes_interval"));
                r.setBonusPoints(rs.getInt("bonus_points"));
                r.setMaximumBonus(rs.getInt("maximum_bonus"));
                r.setStarvationThresholdMinutes(rs.getInt("starvation_threshold_minutes"));
                r.setSpecialAssistanceBonus(rs.getInt("special_assistance_bonus"));
                r.setActive(rs.getBoolean("active"));
                return r;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving active aging rule", e);
        }
        // Fallback default rule if table is empty
        return new AgingRule();
    }

    public void updateAgingRule(AgingRule rule) {
        String sql = "UPDATE aging_rules SET minutes_interval = ?, bonus_points = ?, maximum_bonus = ?, " +
                     "starvation_threshold_minutes = ?, special_assistance_bonus = ? WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, rule.getMinutesInterval());
            ps.setInt(2, rule.getBonusPoints());
            ps.setInt(3, rule.getMaximumBonus());
            ps.setInt(4, rule.getStarvationThresholdMinutes());
            ps.setInt(5, rule.getSpecialAssistanceBonus());
            ps.setLong(6, rule.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Error updating aging rule ID: " + rule.getId(), e);
        }
    }
}
