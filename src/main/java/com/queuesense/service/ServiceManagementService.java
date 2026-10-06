package com.queuesense.service;

import com.queuesense.dao.ServiceDao;
import com.queuesense.exception.DatabaseException;
import com.queuesense.model.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class ServiceManagementService {

    private static final Logger logger = LoggerFactory.getLogger(ServiceManagementService.class);
    private final ServiceDao serviceDao;

    public ServiceManagementService() {
        this.serviceDao = new ServiceDao();
    }

    public ServiceManagementService(ServiceDao serviceDao) {
        this.serviceDao = serviceDao;
    }

    public List<Service> getAllServices() {
        return serviceDao.findAll();
    }

    public List<Service> getActiveServices() {
        return serviceDao.findAllActive();
    }

    public Optional<Service> getServiceById(Long serviceId) {
        return serviceDao.findById(serviceId);
    }

    public Service createService(Service service) {
        if (service.getServiceCode() == null || service.getServiceCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Service code is required.");
        }
        if (service.getServiceName() == null || service.getServiceName().trim().isEmpty()) {
            throw new IllegalArgumentException("Service name is required.");
        }
        if (serviceDao.findByCode(service.getServiceCode().trim().toUpperCase()).isPresent()) {
            throw new DatabaseException("Service code '" + service.getServiceCode() + "' already exists.");
        }
        Service created = serviceDao.create(service);
        logger.info("Created new service: {} ({})", created.getServiceName(), created.getServiceCode());
        return created;
    }

    public void updateService(Service service) {
        if (service.getServiceId() == null) {
            throw new IllegalArgumentException("Service ID is required for update.");
        }
        serviceDao.update(service);
        logger.info("Updated service ID: {}", service.getServiceId());
    }
}
