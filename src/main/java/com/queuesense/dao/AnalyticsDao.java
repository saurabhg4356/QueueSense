package com.queuesense.dao;

import com.queuesense.dto.ChartDataDto;
import com.queuesense.dto.DashboardStatsDto;
import com.queuesense.dto.ReportItemDto;
import com.queuesense.exception.DatabaseException;
import com.queuesense.util.ConnectionManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AnalyticsDao {

    public DashboardStatsDto getDashboardStats(LocalDate startDate, LocalDate endDate) {
        DashboardStatsDto stats = new DashboardStatsDto();

        String statsSql =
                "SELECT " +
                "  COUNT(CASE WHEN status = 'WAITING' THEN 1 END) AS waiting_count, " +
                "  COUNT(CASE WHEN status IN ('CALLED', 'SERVING', 'PAUSED') THEN 1 END) AS serving_count, " +
                "  COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) AS completed_count, " +
                "  COUNT(CASE WHEN status = 'CANCELLED' THEN 1 END) AS cancelled_count, " +
                "  COUNT(CASE WHEN status = 'NO_SHOW' THEN 1 END) AS no_show_count, " +
                "  AVG(CASE WHEN status = 'COMPLETED' AND actual_wait_minutes IS NOT NULL THEN actual_wait_minutes END) AS avg_wait, " +
                "  (SELECT AVG(actual_duration_seconds) / 60.0 FROM service_sessions WHERE completed_at IS NOT NULL) AS avg_service " +
                "FROM tokens WHERE issued_at >= ? AND issued_at <= ?";

        String counterSql =
                "SELECT " +
                "  COUNT(*) AS total_counters, " +
                "  COUNT(CASE WHEN status IN ('AVAILABLE', 'BUSY') THEN 1 END) AS active_counters " +
                "FROM counters";

        String ratingSql = "SELECT AVG(rating) FROM feedback";

        String peakHourSql =
                "SELECT HOUR(issued_at) AS hr, COUNT(*) AS cnt " +
                "FROM tokens WHERE issued_at >= ? AND issued_at <= ? " +
                "GROUP BY HOUR(issued_at) ORDER BY cnt DESC LIMIT 1";

        try (Connection conn = ConnectionManager.getConnection()) {
            Timestamp startTs = Timestamp.valueOf(startDate.atStartOfDay());
            Timestamp endTs = Timestamp.valueOf(endDate.atTime(23, 59, 59));

            try (PreparedStatement ps = conn.prepareStatement(statsSql)) {
                ps.setTimestamp(1, startTs);
                ps.setTimestamp(2, endTs);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        stats.setCustomersWaiting(rs.getInt("waiting_count"));
                        stats.setCustomersServing(rs.getInt("serving_count"));
                        stats.setCompletedToday(rs.getInt("completed_count"));
                        stats.setCancelledToday(rs.getInt("cancelled_count"));
                        stats.setNoShowsToday(rs.getInt("no_show_count"));
                        stats.setAverageWaitMinutes(Math.round(rs.getDouble("avg_wait") * 10.0) / 10.0);
                        stats.setAverageServiceMinutes(Math.round(rs.getDouble("avg_service") * 10.0) / 10.0);
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(counterSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.setTotalCounters(rs.getInt("total_counters"));
                    stats.setActiveCounters(rs.getInt("active_counters"));
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(ratingSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double r = rs.getDouble(1);
                    stats.setCustomerSatisfactionRating(rs.wasNull() ? 0.0 : Math.round(r * 10.0) / 10.0);
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(peakHourSql)) {
                ps.setTimestamp(1, startTs);
                ps.setTimestamp(2, endTs);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int hr = rs.getInt("hr");
                        stats.setPeakHour(String.format("%02d:00 - %02d:00 (%d tokens)", hr, hr + 1, rs.getInt("cnt")));
                    } else {
                        stats.setPeakHour("None recorded");
                    }
                }
            }

            // Calculate overall counter utilization based on busy sessions vs operating hours
            double totalBusySeconds = 0;
            String busySql = "SELECT IFNULL(SUM(actual_duration_seconds), 0) FROM service_sessions WHERE started_at >= ? AND started_at <= ?";
            try (PreparedStatement ps = conn.prepareStatement(busySql)) {
                ps.setTimestamp(1, startTs);
                ps.setTimestamp(2, endTs);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        totalBusySeconds = rs.getDouble(1);
                    }
                }
            }
            int totalActive = Math.max(1, stats.getActiveCounters());
            // Benchmark: 8 hours operating day per active counter
            double benchmarkOperatingSeconds = totalActive * 8 * 3600.0;
            double utilPct = Math.min(100.0, (totalBusySeconds / benchmarkOperatingSeconds) * 100.0);
            stats.setOverallUtilizationPercentage(Math.round(utilPct * 10.0) / 10.0);

        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving dashboard statistics", e);
        }

        return stats;
    }

    // -------------------------------------------------------------
    // 10 CHART.JS DATA METHODS (100% REAL DATABASE QUERIES)
    // -------------------------------------------------------------

    // Chart 1: Customers Served by Hour
    public ChartDataDto getServedByHour(LocalDate startDate, LocalDate endDate) {
        ChartDataDto chart = new ChartDataDto("Customers Served");
        String sql = "SELECT HOUR(service_completed_at) AS hr, COUNT(*) AS cnt " +
                     "FROM tokens WHERE status = 'COMPLETED' AND service_completed_at >= ? AND service_completed_at <= ? " +
                     "GROUP BY HOUR(service_completed_at) ORDER BY hr ASC";
        return executeHourlyQuery(sql, startDate, endDate, chart);
    }

    // Chart 2: Average Waiting Time by Hour
    public ChartDataDto getWaitTimeByHour(LocalDate startDate, LocalDate endDate) {
        ChartDataDto chart = new ChartDataDto("Avg Wait Time (Minutes)");
        String sql = "SELECT HOUR(issued_at) AS hr, AVG(actual_wait_minutes) AS avg_wait " +
                     "FROM tokens WHERE actual_wait_minutes IS NOT NULL AND issued_at >= ? AND issued_at <= ? " +
                     "GROUP BY HOUR(issued_at) ORDER BY hr ASC";
        return executeHourlyQuery(sql, startDate, endDate, chart);
    }

    // Chart 3: Service Demand by Category
    public ChartDataDto getServiceDemand(LocalDate startDate, LocalDate endDate) {
        ChartDataDto chart = new ChartDataDto("Demand Share");
        String sql = "SELECT s.service_name, COUNT(t.token_id) AS cnt " +
                     "FROM services s LEFT JOIN tokens t ON s.service_id = t.service_id " +
                     "AND t.issued_at >= ? AND t.issued_at <= ? " +
                     "GROUP BY s.service_id, s.service_name ORDER BY cnt DESC";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(startDate.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(endDate.atTime(23, 59, 59)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    chart.addPoint(rs.getString("service_name"), rs.getInt("cnt"));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching service demand chart data", e);
        }
        return chart;
    }

    // Chart 4: Tokens by Priority
    public ChartDataDto getTokensByPriority(LocalDate startDate, LocalDate endDate) {
        ChartDataDto chart = new ChartDataDto("Priority Breakdown");
        String sql = "SELECT priority_type, COUNT(*) AS cnt FROM tokens " +
                     "WHERE issued_at >= ? AND issued_at <= ? GROUP BY priority_type";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(startDate.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(endDate.atTime(23, 59, 59)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    chart.addPoint(rs.getString("priority_type"), rs.getInt("cnt"));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching priority breakdown chart data", e);
        }
        return chart;
    }

    // Chart 5: Counter Utilization Percentage
    public ChartDataDto getCounterUtilization(LocalDate startDate, LocalDate endDate) {
        ChartDataDto chart = new ChartDataDto("Utilization Rate (%)");
        String sql = "SELECT c.counter_name, IFNULL(SUM(ss.actual_duration_seconds), 0) AS busy_seconds " +
                     "FROM counters c " +
                     "LEFT JOIN service_sessions ss ON c.counter_id = ss.counter_id " +
                     "AND ss.started_at >= ? AND ss.started_at <= ? " +
                     "GROUP BY c.counter_id, c.counter_name ORDER BY c.counter_number ASC";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(startDate.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(endDate.atTime(23, 59, 59)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double busySecs = rs.getDouble("busy_seconds");
                    double benchmark = 8 * 3600.0;
                    double pct = Math.min(100.0, Math.round((busySecs / benchmark) * 1000.0) / 10.0);
                    chart.addPoint(rs.getString("counter_name"), pct);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching counter utilization chart data", e);
        }
        return chart;
    }

    // Chart 6: Outcome Distribution: Completed vs Cancelled vs No-Show
    public ChartDataDto getOutcomeDistribution(LocalDate startDate, LocalDate endDate) {
        ChartDataDto chart = new ChartDataDto("Status Volume");
        String sql = "SELECT " +
                     "  COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) AS completed, " +
                     "  COUNT(CASE WHEN status = 'CANCELLED' THEN 1 END) AS cancelled, " +
                     "  COUNT(CASE WHEN status = 'NO_SHOW' THEN 1 END) AS no_show " +
                     "FROM tokens WHERE issued_at >= ? AND issued_at <= ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(startDate.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(endDate.atTime(23, 59, 59)));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    chart.addPoint("Completed", rs.getInt("completed"));
                    chart.addPoint("Cancelled", rs.getInt("cancelled"));
                    chart.addPoint("No-Show", rs.getInt("no_show"));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching outcome chart data", e);
        }
        return chart;
    }

    // Chart 7: Average Service Time by Service
    public ChartDataDto getAvgServiceTimeByService(LocalDate startDate, LocalDate endDate) {
        ChartDataDto chart = new ChartDataDto("Avg Duration (Minutes)");
        String sql = "SELECT s.service_name, AVG(ss.actual_duration_seconds) / 60.0 AS avg_mins " +
                     "FROM services s " +
                     "JOIN tokens t ON s.service_id = t.service_id " +
                     "JOIN service_sessions ss ON t.token_id = ss.token_id " +
                     "WHERE ss.completed_at IS NOT NULL AND ss.started_at >= ? AND ss.started_at <= ? " +
                     "GROUP BY s.service_id, s.service_name ORDER BY avg_mins DESC";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(startDate.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(endDate.atTime(23, 59, 59)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double mins = Math.round(rs.getDouble("avg_mins") * 10.0) / 10.0;
                    chart.addPoint(rs.getString("service_name"), mins);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching avg service time chart data", e);
        }
        return chart;
    }

    // Chart 8: Daily Token Volume (Last 7 Days)
    public ChartDataDto getDailyTokenVolume() {
        ChartDataDto chart = new ChartDataDto("Daily Volume");
        String sql = "SELECT DATE(issued_at) AS dt, COUNT(*) AS cnt " +
                     "FROM tokens " +
                     "WHERE issued_at >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
                     "GROUP BY DATE(issued_at) ORDER BY dt ASC";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                chart.addPoint(rs.getString("dt"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching daily token volume chart data", e);
        }
        return chart;
    }

    // Chart 9: Hourly Arrival Patterns (Peak Hours)
    public ChartDataDto getHourlyArrivalPattern(LocalDate startDate, LocalDate endDate) {
        ChartDataDto chart = new ChartDataDto("Arrivals by Hour");
        String sql = "SELECT HOUR(issued_at) AS hr, COUNT(*) AS cnt " +
                     "FROM tokens WHERE issued_at >= ? AND issued_at <= ? " +
                     "GROUP BY HOUR(issued_at) ORDER BY hr ASC";
        return executeHourlyQuery(sql, startDate, endDate, chart);
    }

    // Chart 10: Customer Satisfaction Ratings Breakdown (1-5 Stars)
    public ChartDataDto getSatisfactionDistribution() {
        ChartDataDto chart = new ChartDataDto("Star Rating Count");
        String sql = "SELECT rating, COUNT(*) AS cnt FROM feedback GROUP BY rating ORDER BY rating ASC";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                chart.addPoint(rs.getInt("rating") + " Stars", rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching customer satisfaction chart data", e);
        }
        return chart;
    }

    // -------------------------------------------------------------
    // REPORTS DATA METHODS
    // -------------------------------------------------------------

    public List<ReportItemDto> getDailyQueueReport(LocalDate startDate, LocalDate endDate) {
        List<ReportItemDto> list = new ArrayList<>();
        String sql =
                "SELECT DATE(issued_at) AS rep_date, " +
                "       COUNT(*) AS total_issued, " +
                "       COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) AS total_served, " +
                "       COUNT(CASE WHEN status = 'CANCELLED' THEN 1 END) AS total_cancelled, " +
                "       COUNT(CASE WHEN status = 'NO_SHOW' THEN 1 END) AS total_no_show, " +
                "       AVG(actual_wait_minutes) AS avg_wait " +
                "FROM tokens WHERE issued_at >= ? AND issued_at <= ? " +
                "GROUP BY DATE(issued_at) ORDER BY rep_date DESC";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(startDate.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(endDate.atTime(23, 59, 59)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReportItemDto item = new ReportItemDto();
                    item.setDimension1(rs.getString("rep_date"));
                    item.setMetricCount1(rs.getLong("total_issued"));
                    item.setMetricCount2(rs.getLong("total_served"));
                    item.setMetricDouble1(Math.round(rs.getDouble("avg_wait") * 10.0) / 10.0);
                    item.putExtra("cancelled", rs.getLong("total_cancelled"));
                    item.putExtra("noShow", rs.getLong("total_no_show"));
                    list.add(item);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching daily queue report", e);
        }
        return list;
    }

    public List<ReportItemDto> getServiceDemandReport(LocalDate startDate, LocalDate endDate) {
        List<ReportItemDto> list = new ArrayList<>();
        String sql =
                "SELECT s.service_code, s.service_name, COUNT(t.token_id) AS total_tokens, " +
                "       AVG(t.actual_wait_minutes) AS avg_wait, " +
                "       (SELECT AVG(ss.actual_duration_seconds)/60.0 FROM service_sessions ss " +
                "        JOIN tokens t2 ON ss.token_id = t2.token_id WHERE t2.service_id = s.service_id) AS avg_service " +
                "FROM services s LEFT JOIN tokens t ON s.service_id = t.service_id " +
                "AND t.issued_at >= ? AND t.issued_at <= ? " +
                "GROUP BY s.service_id, s.service_code, s.service_name ORDER BY total_tokens DESC";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(startDate.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(endDate.atTime(23, 59, 59)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReportItemDto item = new ReportItemDto();
                    item.setDimension1(rs.getString("service_code"));
                    item.setDimension2(rs.getString("service_name"));
                    item.setMetricCount1(rs.getLong("total_tokens"));
                    item.setMetricDouble1(Math.round(rs.getDouble("avg_wait") * 10.0) / 10.0);
                    item.setMetricDouble2(Math.round(rs.getDouble("avg_service") * 10.0) / 10.0);
                    list.add(item);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching service demand report", e);
        }
        return list;
    }

    public List<ReportItemDto> getCounterUtilizationReport(LocalDate startDate, LocalDate endDate) {
        List<ReportItemDto> list = new ArrayList<>();
        String sql =
                "SELECT c.counter_number, c.counter_name, c.status, " +
                "       COUNT(ss.session_id) AS sessions_count, " +
                "       IFNULL(SUM(ss.actual_duration_seconds), 0) AS total_busy_seconds " +
                "FROM counters c " +
                "LEFT JOIN service_sessions ss ON c.counter_id = ss.counter_id " +
                "AND ss.started_at >= ? AND ss.started_at <= ? " +
                "GROUP BY c.counter_id, c.counter_number, c.counter_name, c.status " +
                "ORDER BY c.counter_number ASC";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(startDate.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(endDate.atTime(23, 59, 59)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReportItemDto item = new ReportItemDto();
                    item.setDimension1("Counter " + rs.getInt("counter_number"));
                    item.setDimension2(rs.getString("counter_name"));
                    item.setDimension3(rs.getString("status"));
                    item.setMetricCount1(rs.getLong("sessions_count"));
                    double busySecs = rs.getDouble("total_busy_seconds");
                    double busyHours = Math.round((busySecs / 3600.0) * 10.0) / 10.0;
                    double utilPct = Math.min(100.0, Math.round(((busySecs / (8 * 3600.0)) * 100.0) * 10.0) / 10.0);
                    item.setMetricDouble1(busyHours);
                    item.setMetricDouble2(utilPct);
                    list.add(item);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching counter utilization report", e);
        }
        return list;
    }

    private ChartDataDto executeHourlyQuery(String sql, LocalDate startDate, LocalDate endDate, ChartDataDto chart) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(startDate.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(endDate.atTime(23, 59, 59)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int hr = rs.getInt(1);
                    chart.addPoint(String.format("%02d:00", hr), Math.round(rs.getDouble(2) * 10.0) / 10.0);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error executing hourly query", e);
        }
        return chart;
    }
}
