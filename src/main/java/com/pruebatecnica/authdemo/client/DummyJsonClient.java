package com.pruebatecnica.authdemo.client;

import com.pruebatecnica.authdemo.dto.LoginRequest;
import com.pruebatecnica.authdemo.dto.LoginResponse;
import com.pruebatecnica.authdemo.dto.UserResponse;
import com.pruebatecnica.authdemo.dto.UsersListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "dummyjson-client", url = "https://dummyjson.com")
public interface DummyJsonClient {
    
    @PostMapping("/auth/login")
    LoginResponse login(@RequestBody LoginRequest loginRequest);
    
    @GetMapping("/auth/me")
    UserResponse getCurrentUser(@RequestHeader("Authorization") String authorization);
    
    @GetMapping("/users")
    UsersListResponse getAllUsers();
} 