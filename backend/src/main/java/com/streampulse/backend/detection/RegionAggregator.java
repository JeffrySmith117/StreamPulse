package com.streampulse.backend.detection;

import com.streampulse.backend.event.PlaybackEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mantem uma janela deslizante por estado (UF) e acumula os eventos recebidos.
 */
@Component
public class RegionAggregator {

    private final int windowSeconds;
    private final Map<String, SlidingWindow> windows = new ConcurrentHashMap<>();

    public RegionAggregator(@Value("${streampulse.detection.window-seconds:30}") int windowSeconds) {
        this.windowSeconds = windowSeconds;
    }

    public void record(PlaybackEvent event) {
        long now = Instant.now().getEpochSecond();
        windows.computeIfAbsent(event.region(), r -> new SlidingWindow(windowSeconds))
                .record(now, event);
    }

    public Map<String, SlidingWindow.Totals> currentTotals() {
        long now = Instant.now().getEpochSecond();
        Map<String, SlidingWindow.Totals> result = new HashMap<>();
        windows.forEach((region, window) -> result.put(region, window.totals(now)));
        return result;
    }
}