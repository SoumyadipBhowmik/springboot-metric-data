package com.poc.telemetry.open.opentelemetry.controller;

import com.poc.telemetry.open.opentelemetry.service.TelemetryService;
import io.opentelemetry.api.common.Attributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private final TelemetryService telemetryService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public HealthController(TelemetryService telemetryService, KafkaTemplate<String, String> kafkaTemplate) {
        this.telemetryService = telemetryService;
        this.kafkaTemplate = kafkaTemplate;
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
    @PostMapping("/send")
    public String sendMessage(@RequestParam String message) {
        Attributes attributes = Attributes.builder()
                .put("topic", "test-topic")
                .put("message", message)
                .build();

        telemetryService.traceOperation(
                "send_message",
                "KAFKA",
                () -> {
                    kafkaTemplate.send("test-topic", message);
                },
                attributes
        );

        return "Message sent to Kafka";
    }
}
