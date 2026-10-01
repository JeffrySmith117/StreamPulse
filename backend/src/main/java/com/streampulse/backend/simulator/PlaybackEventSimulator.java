package com.streampulse.backend.simulator;

import com.streampulse.backend.config.KafkaTopicConfig;
import com.streampulse.backend.event.EventType;
import com.streampulse.backend.event.PlaybackEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Simula milhares de players de video espalhados pelo Brasil enviando eventos ao Kafka.
 * Regioes "degradadas" passam a travar com muito mais frequencia, para demonstrar a deteccao.
 */
@Component
@ConditionalOnProperty(name = "streampulse.simulator.enabled", havingValue = "true")
public class PlaybackEventSimulator {

    public static final List<String> REGIONS = List.of(
            "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO",
            "MA", "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI",
            "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO"
    );

    private static final double NORMAL_BUFFER_RATE = 0.02;
    private static final double DEGRADED_BUFFER_RATE = 0.35;
    private static final double DEGRADED_ERROR_RATE = 0.05;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonMapper jsonMapper;
    private final int eventsPerTick;
    private final Set<String> degradedRegions = ConcurrentHashMap.newKeySet();

    public PlaybackEventSimulator(KafkaTemplate<String, String> kafkaTemplate,
                                  JsonMapper jsonMapper,
                                  @Value("${streampulse.simulator.events-per-tick:200}") int eventsPerTick) {
        this.kafkaTemplate = kafkaTemplate;
        this.jsonMapper = jsonMapper;
        this.eventsPerTick = eventsPerTick;
    }

    @Scheduled(fixedRateString = "${streampulse.simulator.interval-ms:1000}")
    public void publishBatch() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < eventsPerTick; i++) {
            String region = REGIONS.get(random.nextInt(REGIONS.size()));
            PlaybackEvent event = randomEvent(region, random);
            kafkaTemplate.send(KafkaTopicConfig.PLAYBACK_EVENTS_TOPIC, region, jsonMapper.writeValueAsString(event));
        }
    }

    private PlaybackEvent randomEvent(String region, ThreadLocalRandom random) {
        boolean degraded = degradedRegions.contains(region);
        String sessionId = "session-" + random.nextInt(100_000);
        double roll = random.nextDouble();

        if (degraded && roll < DEGRADED_ERROR_RATE) {
            return new PlaybackEvent(sessionId, region, EventType.ERROR, 0, Instant.now());
        }

        double bufferRate = degraded ? DEGRADED_BUFFER_RATE : NORMAL_BUFFER_RATE;
        if (roll < bufferRate) {
            long bufferMs = degraded
                    ? random.nextLong(2_000, 15_000)
                    : random.nextLong(200, 2_000);
            return new PlaybackEvent(sessionId, region, EventType.BUFFER_END, bufferMs, Instant.now());
        }

        return new PlaybackEvent(sessionId, region, EventType.PLAY, 0, Instant.now());
    }

    public void degrade(String region) {
        degradedRegions.add(region);
    }

    public void restore(String region) {
        degradedRegions.remove(region);
    }

    public Set<String> getDegradedRegions() {
        return Set.copyOf(degradedRegions);
    }
}