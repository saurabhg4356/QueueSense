package com.queuesense.service;

import com.queuesense.dao.AnalyticsDao;
import com.queuesense.dao.CounterDao;
import com.queuesense.dao.TokenDao;
import com.queuesense.dto.ChartDataDto;
import com.queuesense.dto.CounterWorkloadDto;
import com.queuesense.dto.DashboardStatsDto;
import com.queuesense.dto.ReportItemDto;
import com.queuesense.engine.CounterBalancingService;
import com.queuesense.model.Counter;
import com.queuesense.model.Token;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardService {

    private final AnalyticsDao analyticsDao;
    private final CounterDao counterDao;
    private final TokenDao tokenDao;
    private final CounterBalancingService balancingService;

    public DashboardService() {
        this.analyticsDao = new AnalyticsDao();
        this.counterDao = new CounterDao();
        this.tokenDao = new TokenDao();
        this.balancingService = new CounterBalancingService();
    }

    public DashboardService(AnalyticsDao analyticsDao, CounterDao counterDao, TokenDao tokenDao, CounterBalancingService balancingService) {
        this.analyticsDao = analyticsDao;
        this.counterDao = counterDao;
        this.tokenDao = tokenDao;
        this.balancingService = balancingService;
    }

    public DashboardStatsDto getStats(String filter, LocalDate customStart, LocalDate customEnd) {
        LocalDate[] range = resolveDateRange(filter, customStart, customEnd);
        return analyticsDao.getDashboardStats(range[0], range[1]);
    }

    public List<CounterWorkloadDto> getCounterWorkloads() {
        List<Counter> counters = counterDao.findAll();
        List<Token> waiting = tokenDao.findWaitingTokens();

        Map<Long, Token> activeServingMap = new HashMap<>();
        for (Counter c : counters) {
            tokenDao.findCurrentServingTokenForCounter(c.getCounterId())
                    .ifPresent(t -> activeServingMap.put(c.getCounterId(), t));
        }

        return balancingService.evaluateCounterWorkloads(counters, waiting, activeServingMap);
    }

    public Map<String, ChartDataDto> getAllChartData(String filter, LocalDate customStart, LocalDate customEnd) {
        LocalDate[] range = resolveDateRange(filter, customStart, customEnd);
        LocalDate start = range[0];
        LocalDate end = range[1];

        Map<String, ChartDataDto> map = new HashMap<>();
        map.put("servedByHour", analyticsDao.getServedByHour(start, end));
        map.put("waitTimeByHour", analyticsDao.getWaitTimeByHour(start, end));
        map.put("serviceDemand", analyticsDao.getServiceDemand(start, end));
        map.put("tokensByPriority", analyticsDao.getTokensByPriority(start, end));
        map.put("counterUtilization", analyticsDao.getCounterUtilization(start, end));
        map.put("outcomeDistribution", analyticsDao.getOutcomeDistribution(start, end));
        map.put("avgServiceTime", analyticsDao.getAvgServiceTimeByService(start, end));
        map.put("dailyVolume", analyticsDao.getDailyTokenVolume());
        map.put("peakHours", analyticsDao.getHourlyArrivalPattern(start, end));
        map.put("satisfaction", analyticsDao.getSatisfactionDistribution());

        return map;
    }

    public List<ReportItemDto> getDailyReport(String filter, LocalDate customStart, LocalDate customEnd) {
        LocalDate[] range = resolveDateRange(filter, customStart, customEnd);
        return analyticsDao.getDailyQueueReport(range[0], range[1]);
    }

    public List<ReportItemDto> getDemandReport(String filter, LocalDate customStart, LocalDate customEnd) {
        LocalDate[] range = resolveDateRange(filter, customStart, customEnd);
        return analyticsDao.getServiceDemandReport(range[0], range[1]);
    }

    public List<ReportItemDto> getUtilizationReport(String filter, LocalDate customStart, LocalDate customEnd) {
        LocalDate[] range = resolveDateRange(filter, customStart, customEnd);
        return analyticsDao.getCounterUtilizationReport(range[0], range[1]);
    }

    public LocalDate[] resolveDateRange(String filter, LocalDate customStart, LocalDate customEnd) {
        LocalDate today = LocalDate.now();
        if ("yesterday".equalsIgnoreCase(filter)) {
            LocalDate yesterday = today.minusDays(1);
            return new LocalDate[]{yesterday, yesterday};
        } else if ("thisWeek".equalsIgnoreCase(filter)) {
            LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            return new LocalDate[]{monday, today};
        } else if ("thisMonth".equalsIgnoreCase(filter)) {
            LocalDate firstDay = today.with(TemporalAdjusters.firstDayOfMonth());
            return new LocalDate[]{firstDay, today};
        } else if ("thisYear".equalsIgnoreCase(filter)) {
            LocalDate firstDayYear = today.with(TemporalAdjusters.firstDayOfYear());
            return new LocalDate[]{firstDayYear, today};
        } else if ("custom".equalsIgnoreCase(filter) && customStart != null && customEnd != null) {
            return new LocalDate[]{customStart, customEnd};
        }
        // Default: today
        return new LocalDate[]{today, today};
    }
}
