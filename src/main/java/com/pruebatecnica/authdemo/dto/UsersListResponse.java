package com.pruebatecnica.authdemo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsersListResponse {
    private List<UserResponse> users;
    private int total;
    private int skip;
    private int limit;
} 