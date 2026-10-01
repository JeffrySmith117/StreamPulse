package com.streampulse.backend.detection;

import com.streampulse.backend.event.PlaybackEvent;

/**
 * Janela deslizante de N segundos dividida em baldes de 1 segundo.
 * Cada balde guarda os contadores daquele segundo; baldes mais antigos que a janela
 * sao ignorados na soma e reaproveitados quando o ponteiro da volta.
 */
public class SlidingWindow {

    private final int sizeSeconds;
    private final long[] bucketSecond;
    private final long[] total;
    private final long[] buffers;
    private final long[] errors;
    private final long[] bufferMs;

    public SlidingWindow(int sizeSeconds) {
        this.sizeSeconds = sizeSeconds;
        this.bucketSecond = new long[sizeSeconds];
        this.total = new long[sizeSeconds];
        this.buffers = new long[sizeSeconds];
        this.errors = new long[sizeSeconds];
        this.bufferMs = new long[sizeSeconds];
    }

    public synchronized void record(long epochSecond, PlaybackEvent event) {
        int i = Math.floorMod(epochSecond, sizeSeconds);
        if (bucketSecond[i] != epochSecond) {
            bucketSecond[i] = epochSecond;
            total[i] = 0;
            buffers[i] = 0;
            errors[i] = 0;
            bufferMs[i] = 0;
        }
        total[i]++;
        switch (event.type()) {
            case BUFFER_END -> {
                buffers[i]++;
                bufferMs[i] += event.bufferDurationMs();
            }
            case ERROR -> errors[i]++;
            default -> {
            }
        }
    }

    public synchronized Totals totals(long nowEpochSecond) {
        long t = 0, b = 0, e = 0, ms = 0;
        for (int i = 0; i < sizeSeconds; i++) {
            if (nowEpochSecond - bucketSecond[i] < sizeSeconds) {
                t += total[i];
                b += buffers[i];
                e += errors[i];
                ms += bufferMs[i];
            }
        }
        return new Totals(t, b, e, ms);
    }

    public record Totals(long total, long buffers, long errors, long bufferMs) {
    }
}