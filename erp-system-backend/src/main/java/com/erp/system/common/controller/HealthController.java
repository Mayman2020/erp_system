package com.erp.system.common.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public String health() {
        return "OK";
    }

    @GetMapping("/health/live")
    public ResponseEntity<Map<String, String>> liveness() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Application is running");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health/ready")
    public ResponseEntity<Map<String, String>> readiness() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Application is ready");
        return ResponseEntity.ok(response);
    }
}
