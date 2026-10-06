package com.queuesense.service;

import com.queuesense.dao.AuditLogDao;
import com.queuesense.dao.UserDao;
import com.queuesense.exception.UnauthorizedAccessException;
import com.queuesense.model.AuditLog;
import com.queuesense.model.User;
import com.queuesense.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserDao userDao;
    private final AuditLogDao auditLogDao;

    public AuthService() {
        this.userDao = new UserDao();
        this.auditLogDao = new AuditLogDao();
    }

    public AuthService(UserDao userDao, AuditLogDao auditLogDao) {
        this.userDao = userDao;
        this.auditLogDao = auditLogDao;
    }

    public User authenticate(String username, String plainPassword, String ipAddress) {
        if (username == null || plainPassword == null || username.trim().isEmpty()) {
            throw new UnauthorizedAccessException("Username and password are required.");
        }

        Optional<User> userOpt = userDao.findByUsername(username.trim());
        if (userOpt.isEmpty()) {
            logger.warn("Authentication failed: User '{}' not found", username);
            throw new UnauthorizedAccessException("Invalid username or password.");
        }

        User user = userOpt.get();
        if (!user.isActive()) {
            logger.warn("Authentication failed: User '{}' is deactivated", username);
            throw new UnauthorizedAccessException("Account is disabled. Please contact an administrator.");
        }

        if (!PasswordUtil.verifyPassword(plainPassword, user.getPasswordHash())) {
            logger.warn("Authentication failed: Incorrect password for '{}'", username);
            throw new UnauthorizedAccessException("Invalid username or password.");
        }

        logger.info("User '{}' [{}] successfully authenticated", user.getUsername(), user.getRole());
        auditLogDao.insertLog(new AuditLog(user.getUserId(), "LOGIN_SUCCESS", "USER",
                String.valueOf(user.getUserId()), "Successful login from " + ipAddress, ipAddress));

        return user;
    }

    public void logLogout(User user, String ipAddress) {
        if (user != null) {
            auditLogDao.insertLog(new AuditLog(user.getUserId(), "LOGOUT", "USER",
                    String.valueOf(user.getUserId()), "User logged out", ipAddress));
        }
    }
}
