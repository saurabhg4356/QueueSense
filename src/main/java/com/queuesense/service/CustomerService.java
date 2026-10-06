package com.queuesense.service;

import com.queuesense.dao.CustomerDao;
import com.queuesense.exception.CustomerNotFoundException;
import com.queuesense.model.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);
    private final CustomerDao customerDao;

    public CustomerService() {
        this.customerDao = new CustomerDao();
    }

    public CustomerService(CustomerDao customerDao) {
        this.customerDao = customerDao;
    }

    public Customer registerCustomer(Customer customer) {
        if (customer.getFullName() == null || customer.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer full name is required.");
        }
        if (customer.getPhone() == null || customer.getPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer contact phone is required.");
        }

        // Generate human-friendly unique customer number: CUS-YYYY-XXXXX
        if (customer.getCustomerNumber() == null || customer.getCustomerNumber().trim().isEmpty()) {
            customer.setCustomerNumber(customerDao.generateNextCustomerNumber());
        }

        Customer created = customerDao.create(customer);
        logger.info("Registered new customer: {} ({})", created.getFullName(), created.getCustomerNumber());
        return created;
    }

    public Customer getCustomerById(Long customerId) {
        return customerDao.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
    }

    public Optional<Customer> findByUserId(Long userId) {
        return customerDao.findByUserId(userId);
    }

    public Optional<Customer> findByPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return Optional.empty();
        return customerDao.findByPhone(phone.trim());
    }

    public List<Customer> searchCustomers(String query, int page, int pageSize) {
        int offset = Math.max(0, (page - 1) * pageSize);
        return customerDao.search(query, pageSize, offset);
    }

    public int countCustomers(String query) {
        return customerDao.countSearch(query);
    }

    public void updateCustomer(Customer customer) {
        if (customer.getCustomerId() == null) {
            throw new IllegalArgumentException("Customer ID is required for update.");
        }
        customerDao.update(customer);
        logger.info("Updated customer profile ID: {}", customer.getCustomerId());
    }
}
