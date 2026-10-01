package com.streampulse.backend.detection;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Guarda no Redis a fotografia mais recente de cada estado,
 * num hash: chave "streampulse:region-status", campo = UF, valor = JSON.
 */
@Component
public class RegionStatusStore {

    private static final String KEY = "streampulse:region-status";

    private final StringRedisTemplate redis;
    private final JsonMapper jsonMapper;

    public RegionStatusStore(StringRedisTemplate redis, JsonMapper jsonMapper) {
        this.redis = redis;
        this.jsonMapper = jsonMapper;
    }

    public void saveAll(List<RegionMetrics> metrics) {
        if (metrics.isEmpty()) {
            return;
        }
        Map<String, String> entries = new HashMap<>();
        for (RegionMetrics m : metrics) {
            entries.put(m.region(), jsonMapper.writeValueAsString(m));
        }
        redis.opsForHash().putAll(KEY, entries);
    }

    public List<RegionMetrics> findAll() {
        return redis.opsForHash().entries(KEY).values().stream()
                .map(value -> jsonMapper.readValue((String) value, RegionMetrics.class))
                .sorted(Comparator.comparing(RegionMetrics::region))
                .toList();
    }
}