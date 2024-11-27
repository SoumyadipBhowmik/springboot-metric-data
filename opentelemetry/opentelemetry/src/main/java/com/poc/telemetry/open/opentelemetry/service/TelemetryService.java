package com.poc.telemetry.open.opentelemetry.service;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.DoubleHistogram;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Service
public class TelemetryService {
    private final OpenTelemetry openTelemetry;
    private final Tracer tracer;
    private final Meter meter;
    private final ConcurrentHashMap<String, LongCounter> counters;
    private final ConcurrentHashMap<String, DoubleHistogram> histograms;

    public TelemetryService(OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
        this.tracer = openTelemetry.getTracer("your-service-name");
        this.meter = openTelemetry.getMeter("your-service-name");
        this.counters = new ConcurrentHashMap<>();
        this.histograms = new ConcurrentHashMap<>();
    }

    // Generic method to trace any operation with return value
    public <T> T traceOperation(String operationName,
                                String serviceType,
                                Supplier<T> operation,
                                Attributes customAttributes) {
        Span span = createSpan(operationName, serviceType, customAttributes);
        try (Scope scope = span.makeCurrent()) {
            T result = operation.get();
            span.setAttribute("operation.success", true);
            return result;
        } catch (Exception e) {
            span.setAttribute("operation.success", false);
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    // Generic method to trace void operations
    public void traceOperation(String operationName,
                               String serviceType,
                               Runnable operation,
                               Attributes customAttributes) {
        Span span = createSpan(operationName, serviceType, customAttributes);
        try (Scope scope = span.makeCurrent()) {
            operation.run();
            span.setAttribute("operation.success", true);
        } catch (Exception e) {
            span.setAttribute("operation.success", false);
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    // Create counter if not exists and increment
    public void incrementCounter(String name, String serviceType, Attributes attributes) {
        LongCounter counter = counters.computeIfAbsent(name,
                k -> meter.counterBuilder(k)
                        .setDescription("Counter for " + name)
                        .build());
        counter.add(1, attributes);
    }

    // Record timing for operations
    public void recordTiming(String name, String serviceType, double timeInMs, Attributes attributes) {
        DoubleHistogram histogram = histograms.computeIfAbsent(name,
                k -> meter.histogramBuilder(k)
                        .setDescription("Timing histogram for " + name)
                        .setUnit("ms")
                        .build());
        histogram.record(timeInMs, attributes);
    }

    private Span createSpan(String operationName, String serviceType, Attributes attributes) {
        return tracer.spanBuilder(operationName)
                .setAttribute("service.type", serviceType)
                .setAttribute("service.name", serviceType)
                .setAllAttributes(attributes)
                .startSpan();
    }
}

