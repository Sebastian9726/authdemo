package com.pruebatecnica.authdemo.repository;

import com.pruebatecnica.authdemo.entity.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LoginLogRepository extends JpaRepository<LoginLog, UUID> {
    
    List<LoginLog> findByUsernameOrderByLoginTimeDesc(String username);
    
    List<LoginLog> findAllByOrderByLoginTimeDesc();
} 