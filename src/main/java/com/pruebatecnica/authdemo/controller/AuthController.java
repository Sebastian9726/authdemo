package com.pruebatecnica.authdemo.controller;

import com.pruebatecnica.authdemo.dto.ErrorResponse;
import com.pruebatecnica.authdemo.dto.LoginRequest;
import com.pruebatecnica.authdemo.dto.LoginResponse;
import com.pruebatecnica.authdemo.dto.UserResponse;
import com.pruebatecnica.authdemo.dto.UsersListResponse;
import com.pruebatecnica.authdemo.entity.LoginLog;
import com.pruebatecnica.authdemo.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    /**
     * Login endpoint - authenticates user against DummyJSON and saves login log
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        log.info("Login request received for user: {}", loginRequest.getUsername());

        try {
            LoginResponse response = authService.authenticateUser(loginRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Login failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get current user information using access token
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(
            @CookieValue(value = "accessToken", required = false) String cookieToken) {
        log.info("Get current user request received");

        try {
            String token = null;

            // If not found, try cookie
            if (cookieToken != null) {
                token = cookieToken;
                log.info("Using token from accessToken cookie");
            }

            if (token == null) {
                log.error("No token provided in Authorization header or accessToken cookie");
                ErrorResponse errorResponse = new ErrorResponse(
                        "UNAUTHORIZED",
                        "Token de acceso requerido. Por favor, proporciona un token válido.",
                        "AUTH_001");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            UserResponse response = authService.getCurrentUser(token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Get current user failed: {}", e.getMessage());
            ErrorResponse errorResponse = new ErrorResponse(
                    "BAD_REQUEST",
                    "Error al obtener la información del usuario: " + e.getMessage(),
                    "AUTH_002");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get all available users from DummyJSON for testing
     */
    @GetMapping("/users")
    public ResponseEntity<UsersListResponse> getAllUsers() {
        log.info("Get all users request received");

        try {
            UsersListResponse response = authService.getAllUsers();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Get all users failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get login history for a specific user
     */
    @GetMapping("/login-history/{username}")
    public ResponseEntity<List<LoginLog>> getLoginHistory(@PathVariable("username") String username) {
        log.info("Get login history request received for user: {}", username);

        try {
            List<LoginLog> response = authService.getLoginHistory(username);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Get login history failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get all login logs
     */
    @GetMapping("/login-logs")
    public ResponseEntity<List<LoginLog>> getAllLoginLogs() {
        log.info("Get all login logs request received");

        try {
            List<LoginLog> response = authService.getAllLoginLogs();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Get all login logs failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        }
    }
}