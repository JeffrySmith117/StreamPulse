package com.streampulse.backend.detection;

import com.streampulse.backend.config.WebSocketConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

/**
 * A cada intervalo, transforma os contadores das janelas em metricas,
 * classifica cada estado (OK / WARNING / CRITICAL), grava no Redis
 * e envia a lista atualizada aos navegadores conectados via WebSocket.
 */
@Component
public class RegionStatusPublisher {

    private final RegionAggregator aggregator;
    private final RegionStatusStore store;
    private final SimpMessagingTemplate messaging;
    private final double warningRebufferRate;
    private final double criticalRebufferRate;
    private final double criticalErrorRate;
    private final long minEvents;

    public RegionStatusPublisher(RegionAggregator aggregator,
                                 RegionStatusStore store,
                                 SimpMessagingTemplate messaging,
                                 @Value("${streampulse.detection.warning-rebuffer-rate:0.06}") double warningRebufferRate,
                                 @Value("${streampulse.detection.critical-rebuffer-rate:0.15}") double criticalRebufferRate,
                                 @Value("${streampulse.detection.critical-error-rate:0.03}") double criticalErrorRate,
                                 @Value("${streampulse.detection.min-events:20}") long minEvents) {
        this.aggregator = aggregator;
        this.store = store;
        this.messaging = messaging;
        this.warningRebufferRate = warningRebufferRate;
        this.criticalRebufferRate = criticalRebufferRate;
        this.criticalErrorRate = criticalErrorRate;
        this.minEvents = minEvents;
    }

    @Scheduled(fixedRateString = "${streampulse.detection.publish-interval-ms:1000}")
    public void publish() {
        Instant now = Instant.now();
        List<RegionMetrics> metrics = aggregator.currentTotals().entrySet().stream()
                .map(entry -> toMetrics(entry.getKey(), entry.getValue(), now))
                .sorted(Comparator.comparing(RegionMetrics::region))
                .toList();
        store.saveAll(metrics);
        messaging.convertAndSend(WebSocketConfig.REGIONS_TOPIC, metrics);
    }

    private RegionMetrics toMetrics(String region, SlidingWindow.Totals t, Instant now) {
        double rebufferRate = t.total() == 0 ? 0 : (double) t.buffers() / t.total();
        double errorRate = t.total() == 0 ? 0 : (double) t.errors() / t.total();
        long avgBufferMs = t.buffers() == 0 ? 0 : t.bufferMs() / t.buffers();
        RegionStatus status = classify(t.total(), rebufferRate, errorRate);
        return new RegionMetrics(region, t.total(), t.buffers(), t.errors(),
                rebufferRate, avgBufferMs, errorRate, status, now);
    }

    private RegionStatus classify(long total, double rebufferRate, double errorRate) {
        if (total < minEvents) {
            return RegionStatus.OK;
        }
        if (rebufferRate >= criticalRebufferRate || errorRate >= criticalErrorRate) {
            return RegionStatus.CRITICAL;
        }
        if (rebufferRate >= warningRebufferRate) {
            return RegionStatus.WARNING;
        }
        return RegionStatus.OK;
    }
}