package com.pruebatecnica.authdemo.service;

import com.pruebatecnica.authdemo.client.DummyJsonClient;
import com.pruebatecnica.authdemo.dto.LoginRequest;
import com.pruebatecnica.authdemo.dto.LoginResponse;
import com.pruebatecnica.authdemo.dto.UserResponse;
import com.pruebatecnica.authdemo.dto.UsersListResponse;
import com.pruebatecnica.authdemo.entity.LoginLog;
import com.pruebatecnica.authdemo.repository.LoginLogRepository;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private DummyJsonClient dummyJsonClient;

    @Mock
    private LoginLogRepository loginLogRepository;

    @InjectMocks
    private AuthService authService;

    private LoginRequest loginRequest;
    private LoginResponse loginResponse;
    private UserResponse userResponse;
    private LoginLog loginLog;

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

        loginLog = new LoginLog("emilys", "test-access-token", "test-refresh-token");
        loginLog.setId(UUID.randomUUID());
        loginLog.setLoginTime(LocalDateTime.now());
    }

    @Test
    void authenticateUser_Success() {
        // Arrange
        when(dummyJsonClient.login(loginRequest)).thenReturn(loginResponse);
        when(loginLogRepository.save(any(LoginLog.class))).thenReturn(loginLog);

        // Act
        LoginResponse result = authService.authenticateUser(loginRequest);

        // Assert
        assertNotNull(result);
        assertEquals("emilys", result.getUsername());
        assertEquals("emily.johnson@x.dummyjson.com", result.getEmail());
        assertEquals("Emily", result.getFirstName());
        assertEquals("Johnson", result.getLastName());
        assertNotNull(result.getAccessToken());
        assertNotNull(result.getRefreshToken());
        assertTrue(result.getAccessToken().startsWith("eyJ"));
        assertTrue(result.getRefreshToken().startsWith("eyJ"));

        // Verify interactions
        verify(dummyJsonClient, times(1)).login(loginRequest);
        verify(loginLogRepository, times(1)).save(any(LoginLog.class));
    }

    @Test
    void authenticateUser_InvalidCredentials() {
        // Arrange
        when(dummyJsonClient.login(loginRequest))
                .thenThrow(new RuntimeException("Invalid credentials"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.authenticateUser(loginRequest);
        });

        assertTrue(exception.getMessage().contains("Authentication failed"));
        verify(dummyJsonClient, times(1)).login(loginRequest);
        verify(loginLogRepository, never()).save(any(LoginLog.class));
    }

    @Test
    void authenticateUser_FeignClientError() {
        // Arrange
        FeignException feignException = mock(FeignException.class);
        when(feignException.getMessage()).thenReturn("401 Unauthorized");
        when(dummyJsonClient.login(loginRequest)).thenThrow(feignException);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.authenticateUser(loginRequest);
        });

        assertTrue(exception.getMessage().contains("Authentication failed"));
        verify(dummyJsonClient, times(1)).login(loginRequest);
        verify(loginLogRepository, never()).save(any(LoginLog.class));
    }

    @Test
    void authenticateUser_DatabaseError() {
        // Arrange
        when(dummyJsonClient.login(loginRequest)).thenReturn(loginResponse);
        when(loginLogRepository.save(any(LoginLog.class)))
                .thenThrow(new RuntimeException("Database connection error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.authenticateUser(loginRequest);
        });

        assertTrue(exception.getMessage().contains("Authentication failed"));
        verify(dummyJsonClient, times(1)).login(loginRequest);
        verify(loginLogRepository, times(1)).save(any(LoginLog.class));
    }

    @Test
    void authenticateUser_NullResponse() {
        // Arrange
        when(dummyJsonClient.login(loginRequest)).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.authenticateUser(loginRequest);
        });

        assertTrue(exception.getMessage().contains("Authentication failed"));
        verify(dummyJsonClient, times(1)).login(loginRequest);
        verify(loginLogRepository, never()).save(any(LoginLog.class));
    }

    @Test
    void getCurrentUser_Success() {
        // Arrange
        String accessToken = "test-token";
        String authHeader = "Bearer " + accessToken;
        when(dummyJsonClient.getCurrentUser(authHeader)).thenReturn(userResponse);

        // Act
        UserResponse result = authService.getCurrentUser(accessToken);

        // Assert
        assertNotNull(result);
        assertEquals("emilys", result.getUsername());
        assertEquals("emily.johnson@x.dummyjson.com", result.getEmail());
        assertEquals("Emily", result.getFirstName());
        assertEquals("Johnson", result.getLastName());
        assertEquals("+81 965-431-3024", result.getPhone());
        assertEquals("1996-5-30", result.getBirthDate());

        verify(dummyJsonClient, times(1)).getCurrentUser(authHeader);
    }

    @Test
    void getCurrentUser_InvalidToken() {
        // Arrange
        String accessToken = "invalid-token";
        String authHeader = "Bearer " + accessToken;
        when(dummyJsonClient.getCurrentUser(authHeader))
                .thenThrow(new RuntimeException("Invalid token"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.getCurrentUser(accessToken);
        });

        assertTrue(exception.getMessage().contains("Failed to get user information"));
        verify(dummyJsonClient, times(1)).getCurrentUser(authHeader);
    }

    @Test
    void getCurrentUser_ExpiredToken() {
        // Arrange
        String accessToken = "expired-token";
        String authHeader = "Bearer " + accessToken;
        FeignException feignException = mock(FeignException.class);
        when(feignException.getMessage()).thenReturn("401 Token expired");
        when(dummyJsonClient.getCurrentUser(authHeader)).thenThrow(feignException);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.getCurrentUser(accessToken);
        });

        assertTrue(exception.getMessage().contains("Failed to get user information"));
        verify(dummyJsonClient, times(1)).getCurrentUser(authHeader);
    }

    @Test
    void getCurrentUser_EmptyToken() {
        // Arrange
        String accessToken = "";
        String authHeader = "Bearer " + accessToken;
        when(dummyJsonClient.getCurrentUser(authHeader))
                .thenThrow(new RuntimeException("Empty token"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.getCurrentUser(accessToken);
        });

        assertTrue(exception.getMessage().contains("Failed to get user information"));
        verify(dummyJsonClient, times(1)).getCurrentUser(authHeader);
    }

    @Test
    void getAllUsers_Success() {
        // Arrange
        UsersListResponse usersListResponse = new UsersListResponse();
        List<UserResponse> users = new ArrayList<>();
        users.add(userResponse);
        
        UserResponse secondUser = new UserResponse();
        secondUser.setId(2L);
        secondUser.setUsername("michaelw");
        secondUser.setEmail("michael.williams@x.dummyjson.com");
        secondUser.setFirstName("Michael");
        secondUser.setLastName("Williams");
        users.add(secondUser);
        
        usersListResponse.setUsers(users);
        usersListResponse.setTotal(2);
        usersListResponse.setSkip(0);
        usersListResponse.setLimit(30);

        when(dummyJsonClient.getAllUsers()).thenReturn(usersListResponse);

        // Act
        UsersListResponse result = authService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getUsers().size());
        assertEquals(2, result.getTotal());
        assertEquals("emilys", result.getUsers().get(0).getUsername());
        assertEquals("michaelw", result.getUsers().get(1).getUsername());

        verify(dummyJsonClient, times(1)).getAllUsers();
    }

    @Test
    void getAllUsers_ServiceUnavailable() {
        // Arrange
        when(dummyJsonClient.getAllUsers())
                .thenThrow(new RuntimeException("Service unavailable"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.getAllUsers();
        });

        assertTrue(exception.getMessage().contains("Failed to get users"));
        verify(dummyJsonClient, times(1)).getAllUsers();
    }

    @Test
    void getLoginHistory_Success() {
        // Arrange
        String username = "emilys";
        List<LoginLog> mockLogs = new ArrayList<>();
        
        LoginLog log1 = new LoginLog(username, "token1", "refresh1");
        log1.setId(UUID.randomUUID());
        log1.setLoginTime(LocalDateTime.now());
        
        LoginLog log2 = new LoginLog(username, "token2", "refresh2");
        log2.setId(UUID.randomUUID());
        log2.setLoginTime(LocalDateTime.now().minusHours(1));
        
        mockLogs.add(log1);
        mockLogs.add(log2);

        when(loginLogRepository.findByUsernameOrderByLoginTimeDesc(username)).thenReturn(mockLogs);

        // Act
        List<LoginLog> result = authService.getLoginHistory(username);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(username, result.get(0).getUsername());
        assertEquals(username, result.get(1).getUsername());
        assertTrue(result.get(0).getLoginTime().isAfter(result.get(1).getLoginTime()));

        verify(loginLogRepository, times(1)).findByUsernameOrderByLoginTimeDesc(username);
    }

    @Test
    void getAllLoginLogs_Success() {
        // Arrange
        List<LoginLog> mockLogs = new ArrayList<>();
        
        LoginLog log1 = new LoginLog("user1", "token1", "refresh1");
        log1.setId(UUID.randomUUID());
        log1.setLoginTime(LocalDateTime.now());
        
        LoginLog log2 = new LoginLog("user2", "token2", "refresh2");
        log2.setId(UUID.randomUUID());
        log2.setLoginTime(LocalDateTime.now().minusMinutes(30));
        
        LoginLog log3 = new LoginLog("user1", "token3", "refresh3");
        log3.setId(UUID.randomUUID());
        log3.setLoginTime(LocalDateTime.now().minusHours(2));
        
        mockLogs.add(log1);
        mockLogs.add(log2);
        mockLogs.add(log3);

        when(loginLogRepository.findAllByOrderByLoginTimeDesc()).thenReturn(mockLogs);

        // Act
        List<LoginLog> result = authService.getAllLoginLogs();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("user1", result.get(0).getUsername());
        assertEquals("user2", result.get(1).getUsername());
        assertEquals("user1", result.get(2).getUsername());

        verify(loginLogRepository, times(1)).findAllByOrderByLoginTimeDesc();
    }

    @Test
    void getAllLoginLogs_DatabaseError() {
        // Arrange
        RuntimeException dbException = new RuntimeException("Database connection failed");
        when(loginLogRepository.findAllByOrderByLoginTimeDesc()).thenThrow(dbException);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.getAllLoginLogs();
        });

        assertEquals("Database connection failed", exception.getMessage());
        verify(loginLogRepository, times(1)).findAllByOrderByLoginTimeDesc();
    }

    @Test
    void saveTestLoginLog_Success() {
        // Arrange
        LoginLog testLog = new LoginLog("test-user", "test-token", "test-refresh");
        when(loginLogRepository.save(testLog)).thenReturn(testLog);

        // Act
        assertDoesNotThrow(() -> authService.saveTestLoginLog(testLog));

        // Assert
        verify(loginLogRepository, times(1)).save(testLog);
    }
} 