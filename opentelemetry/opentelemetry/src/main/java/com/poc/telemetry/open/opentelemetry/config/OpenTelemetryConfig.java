package com.poc.telemetry.open.opentelemetry.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.semconv.ResourceAttributes;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenTelemetryConfig {

    @Bean
    public OpenTelemetry openTelemetry() {
        // Define the resource with service name
        Resource resource = Resource.getDefault()
                .merge(Resource.create(Attributes.of(
                        ResourceAttributes.SERVICE_NAME, "tememetry-service"
                )));

        // Configure Tracer Provider
        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(io.opentelemetry.sdk.trace.export.BatchSpanProcessor.builder(
                                OtlpGrpcSpanExporter.builder()
                                        .setEndpoint("http://localhost:4317") // Replace with your OTLP endpoint
                                        .build())
                        .build())
                .setResource(resource)
                .build();

        // Configure Meter Provider
        SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder()
                .registerMetricReader(PeriodicMetricReader.builder(
                                OtlpGrpcMetricExporter.builder()
                                        .setEndpoint("http://localhost:4317") // Replace with your OTLP endpoint
                                        .build())
                        .build())
                .setResource(resource)
                .build();

        // Build OpenTelemetry SDK
        OpenTelemetrySdk openTelemetrySdk = OpenTelemetrySdk.builder()
                .setTracerProvider(sdkTracerProvider)
                .setMeterProvider(sdkMeterProvider)
                .setPropagators(ContextPropagators.create(
                        W3CTraceContextPropagator.getInstance()))
                .buildAndRegisterGlobal();

        return openTelemetrySdk;
    }
}
