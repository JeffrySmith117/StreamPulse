package com.streampulse.backend.detection;

import java.time.Instant;

/**
 * Fotografia da qualidade de reproducao de um estado na janela de tempo atual.
 */
public record RegionMetrics(
        String region,
        long totalEvents,
        long bufferEvents,
        long errorEvents,
        double rebufferRate,
        long avgBufferMs,
        double errorRate,
        RegionStatus status,
        Instant updatedAt
) {
}