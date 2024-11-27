package com.poc.telemetry.open.opentelemetry.controller;

import com.poc.telemetry.open.opentelemetry.service.TelemetryService;
import io.opentelemetry.api.common.Attributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private final TelemetryService telemetryService;

    @Autowired
    public HealthController(TelemetryService telemetryService) {
        this.telemetryService = telemetryService;
    }

    @GetMapping("/health")
    public String healthCheck() {
        Attributes attributes = Attributes.builder()
                .put("endpoint", "/health")
                .put("method", "GET")
                .build();

        return telemetryService.traceOperation(
                "health_check",
                "REST",
                () -> {
                    // Here you can add any logic you want to trace
                    System.out.println("Application is running");
                    return "Application is running";
                },
                attributes
        );
    }
}
