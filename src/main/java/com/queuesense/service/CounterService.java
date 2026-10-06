package com.queuesense.service;

import com.queuesense.dao.CounterDao;
import com.queuesense.enums.CounterStatus;
import com.queuesense.exception.CounterNotFoundException;
import com.queuesense.model.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class CounterService {

    private static final Logger logger = LoggerFactory.getLogger(CounterService.class);
    private final CounterDao counterDao;

    public CounterService() {
        this.counterDao = new CounterDao();
    }

    public CounterService(CounterDao counterDao) {
        this.counterDao = counterDao;
    }

    public List<Counter> getAllCounters() {
        return counterDao.findAll();
    }

    public Counter getCounterById(Long counterId) {
        return counterDao.findById(counterId)
                .orElseThrow(() -> new CounterNotFoundException("Counter ID " + counterId + " was not found."));
    }

    public Optional<Counter> getCounterByAgentId(Long agentId) {
        return counterDao.findByAssignedAgentId(agentId);
    }

    public List<Counter> getCompatibleCountersForService(Long serviceId) {
        return counterDao.findCompatibleCountersForService(serviceId);
    }

    public Counter createCounter(Counter counter, Set<Long> supportedServiceIds) {
        if (counter.getCounterName() == null || counter.getCounterName().trim().isEmpty()) {
            throw new IllegalArgumentException("Counter name is required.");
        }
        Counter created = counterDao.create(counter);
        if (supportedServiceIds != null && !supportedServiceIds.isEmpty()) {
            counterDao.updateCounterServices(created.getCounterId(), supportedServiceIds);
            created.setSupportedServiceIds(supportedServiceIds);
        }
        logger.info("Created new counter: {} (#{})", created.getCounterName(), created.getCounterNumber());
        return created;
    }

    public void updateCounter(Counter counter, Set<Long> supportedServiceIds) {
        counterDao.update(counter);
        if (supportedServiceIds != null) {
            counterDao.updateCounterServices(counter.getCounterId(), supportedServiceIds);
        }
        logger.info("Updated counter ID: {}", counter.getCounterId());
    }

    public void updateCounterStatus(Long counterId, CounterStatus status) {
        counterDao.updateStatus(counterId, status);
        logger.info("Updated counter ID {} status to {}", counterId, status);
    }
}
