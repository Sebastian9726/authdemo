package com.pruebatecnica.authdemo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private String error;
    private String message;
    private String code;
    private long timestamp;
    
    public ErrorResponse(String error, String message, String code) {
        this.error = error;
        this.message = message;
        this.code = code;
        this.timestamp = System.currentTimeMillis();
    }
} 