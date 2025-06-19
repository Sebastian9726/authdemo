package com.pruebatecnica.authdemo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pruebatecnica.authdemo.dto.ErrorResponse;
import com.pruebatecnica.authdemo.dto.LoginRequest;
import com.pruebatecnica.authdemo.dto.LoginResponse;
import com.pruebatecnica.authdemo.dto.UserResponse;
import com.pruebatecnica.authdemo.dto.UsersListResponse;
import com.pruebatecnica.authdemo.entity.LoginLog;
import com.pruebatecnica.authdemo.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private LoginRequest loginRequest;
    private LoginResponse loginResponse;
    private UserResponse userResponse;
    private List<LoginLog> loginLogs;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest("emilys", "emilyspass");

        loginResponse = new LoginResponse();
        loginResponse.setId(1L);
        loginResponse.setUsername("emilys");
        loginResponse.setEmail("emily.johnson@x.dummyjson.com");
        loginResponse.setFirstName("Emily");
        loginResponse.setLastName("Johnson");
        loginResponse.setGender("female");
        loginResponse.setImage("https://dummyjson.com/icon/emilys/128");
        loginResponse.setAccessToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token");
        loginResponse.setRefreshToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.refresh.token");

        userResponse = new UserResponse();
        userResponse.setId(1L);
        userResponse.setUsername("emilys");
        userResponse.setEmail("emily.johnson@x.dummyjson.com");
        userResponse.setFirstName("Emily");
        userResponse.setLastName("Johnson");
        userResponse.setGender("female");
        userResponse.setImage("https://dummyjson.com/icon/emilys/128");
        userResponse.setPhone("+81 965-431-3024");
        userResponse.setBirthDate("1996-5-30");

        loginLogs = new ArrayList<>();
        LoginLog log = new LoginLog("emilys", "test-token", "test-refresh");
        log.setId(UUID.randomUUID());
        log.setLoginTime(LocalDateTime.now());
        loginLogs.add(log);
    }

    @Test
    void login_Success() throws Exception {
        // Arrange
        when(authService.authenticateUser(any(LoginRequest.class))).thenReturn(loginResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("emilys")))
                .andExpect(jsonPath("$.email", is("emily.johnson@x.dummyjson.com")))
                .andExpect(jsonPath("$.firstName", is("Emily")))
                .andExpect(jsonPath("$.lastName", is("Johnson")))
                .andExpect(jsonPath("$.accessToken", org.hamcrest.Matchers.startsWith("eyJ")))
                .andExpect(jsonPath("$.refreshToken", org.hamcrest.Matchers.startsWith("eyJ")));
    }

    @Test
    void login_InvalidCredentials() throws Exception {
        // Arrange
        when(authService.authenticateUser(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Authentication failed: Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_InvalidJson() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCurrentUser_Success_WithCookie() throws Exception {
        // Arrange
        when(authService.getCurrentUser(anyString())).thenReturn(userResponse);

        // Act & Assert
        mockMvc.perform(get("/api/auth/me")
                .cookie(new jakarta.servlet.http.Cookie("accessToken", "test-token")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username", is("emilys")))
                .andExpect(jsonPath("$.email", is("emily.johnson@x.dummyjson.com")))
                .andExpect(jsonPath("$.firstName", is("Emily")))
                .andExpect(jsonPath("$.phone", is("+81 965-431-3024")))
                .andExpect(jsonPath("$.birthDate", is("1996-5-30")));
    }

    @Test
    void getCurrentUser_NoToken_ReturnsCustomError() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("UNAUTHORIZED")))
                .andExpect(jsonPath("$.message", is("Token de acceso requerido. Por favor, proporciona un token válido.")))
                .andExpect(jsonPath("$.code", is("AUTH_001")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void getCurrentUser_InvalidToken_ReturnsCustomError() throws Exception {
        // Arrange
        when(authService.getCurrentUser(anyString()))
                .thenThrow(new RuntimeException("Invalid token"));

        // Act & Assert
        mockMvc.perform(get("/api/auth/me")
                .cookie(new jakarta.servlet.http.Cookie("accessToken", "invalid-token")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.message", containsString("Error al obtener la información del usuario")))
                .andExpect(jsonPath("$.code", is("AUTH_002")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void getAllUsers_Success() throws Exception {
        // Arrange
        UsersListResponse usersResponse = new UsersListResponse();
        List<UserResponse> users = new ArrayList<>();
        users.add(userResponse);
        usersResponse.setUsers(users);
        usersResponse.setTotal(1);
        usersResponse.setSkip(0);
        usersResponse.setLimit(30);

        when(authService.getAllUsers()).thenReturn(usersResponse);

        // Act & Assert
        mockMvc.perform(get("/api/auth/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.users", hasSize(1)))
                .andExpect(jsonPath("$.total", is(1)))
                .andExpect(jsonPath("$.users[0].username", is("emilys")));
    }

    @Test
    void getAllUsers_ServiceError() throws Exception {
        // Arrange
        when(authService.getAllUsers())
                .thenThrow(new RuntimeException("Service unavailable"));

        // Act & Assert
        mockMvc.perform(get("/api/auth/users"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getLoginHistory_Success() throws Exception {
        // Arrange
        when(authService.getLoginHistory("emilys")).thenReturn(loginLogs);

        // Act & Assert
        mockMvc.perform(get("/api/auth/login-history/emilys"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username", is("emilys")));
    }

    @Test
    void getLoginHistory_EmptyResult() throws Exception {
        // Arrange
        when(authService.getLoginHistory("nonexistent")).thenReturn(new ArrayList<>());

        // Act & Assert
        mockMvc.perform(get("/api/auth/login-history/nonexistent"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getLoginHistory_ServiceError() throws Exception {
        // Arrange
        when(authService.getLoginHistory(anyString()))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(get("/api/auth/login-history/emilys"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllLoginLogs_Success() throws Exception {
        // Arrange
        when(authService.getAllLoginLogs()).thenReturn(loginLogs);

        // Act & Assert
        mockMvc.perform(get("/api/auth/login-logs"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username", is("emilys")));
    }

    @Test
    void getAllLoginLogs_ServiceError() throws Exception {
        // Arrange
        when(authService.getAllLoginLogs())
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        mockMvc.perform(get("/api/auth/login-logs"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void corsHeadersPresent() throws Exception {
        // Act & Assert
        mockMvc.perform(options("/api/auth/users")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET"))
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }
} 