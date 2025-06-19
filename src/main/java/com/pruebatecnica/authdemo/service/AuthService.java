package com.pruebatecnica.authdemo.service;

import com.pruebatecnica.authdemo.client.DummyJsonClient;
import com.pruebatecnica.authdemo.dto.LoginRequest;
import com.pruebatecnica.authdemo.dto.LoginResponse;
import com.pruebatecnica.authdemo.dto.UserResponse;
import com.pruebatecnica.authdemo.dto.UsersListResponse;
import com.pruebatecnica.authdemo.entity.LoginLog;
import com.pruebatecnica.authdemo.repository.LoginLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final DummyJsonClient dummyJsonClient;
    private final LoginLogRepository loginLogRepository;
    
    /**
     * Authenticate user against DummyJSON API and save login log
     */
    @Transactional
    public LoginResponse authenticateUser(LoginRequest loginRequest) {
        try {
            log.info("Attempting to authenticate user: {}", loginRequest.getUsername());
            
            // Call DummyJSON login endpoint
            LoginResponse loginResponse = dummyJsonClient.login(loginRequest);
            
            log.info("Authentication successful for user: {}", loginRequest.getUsername());
            
            // Save login log to database
            LoginLog loginLog = new LoginLog(
                loginResponse.getUsername(),
                loginResponse.getAccessToken(),
                loginResponse.getRefreshToken()
            );
            
            log.info("Saving login log for user: {}", loginResponse.getUsername());
            LoginLog savedLog = loginLogRepository.save(loginLog);
            log.info("Login log saved successfully with ID: {}", savedLog.getId());
            
            return loginResponse;
            
        } catch (Exception e) {
            log.error("Authentication failed for user: {}. Error: {}", 
                loginRequest.getUsername(), e.getMessage(), e);
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }
    }
    
    /**
     * Get current user information using access token
     */
    public UserResponse getCurrentUser(String accessToken) {
        try {
            log.info("Getting current user information");
            
            String authHeader = "Bearer " + accessToken;
            UserResponse userResponse = dummyJsonClient.getCurrentUser(authHeader);
            
            log.info("Successfully retrieved user information for: {}", userResponse.getUsername());
            return userResponse;
            
        } catch (Exception e) {
            log.error("Failed to get current user. Error: {}", e.getMessage());
            throw new RuntimeException("Failed to get user information: " + e.getMessage());
        }
    }
    
    /**
     * Get all available users from DummyJSON for testing
     */
    public UsersListResponse getAllUsers() {
        try {
            log.info("Getting all users from DummyJSON");
            
            UsersListResponse usersResponse = dummyJsonClient.getAllUsers();
            
            log.info("Successfully retrieved {} users", usersResponse.getUsers().size());
            return usersResponse;
            
        } catch (Exception e) {
            log.error("Failed to get users. Error: {}", e.getMessage());
            throw new RuntimeException("Failed to get users: " + e.getMessage());
        }
    }
    
    /**
     * Get login history for a specific user
     */
    public List<LoginLog> getLoginHistory(String username) {
        log.info("Getting login history for user: {}", username);
        return loginLogRepository.findByUsernameOrderByLoginTimeDesc(username);
    }
    
    /**
     * Get all login logs
     */
    public List<LoginLog> getAllLoginLogs() {
        log.info("Getting all login logs");
        return loginLogRepository.findAllByOrderByLoginTimeDesc();
    }
    
    /**
     * Save a test login log - for testing database connectivity
     */
    @Transactional
    public LoginLog saveTestLoginLog(LoginLog loginLog) {
        log.info("Saving test login log for user: {}", loginLog.getUsername());
        return loginLogRepository.save(loginLog);
    }
} 