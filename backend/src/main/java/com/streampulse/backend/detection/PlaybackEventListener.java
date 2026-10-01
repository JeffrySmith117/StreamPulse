package com.streampulse.backend.detection;

import com.streampulse.backend.config.KafkaTopicConfig;
import com.streampulse.backend.event.PlaybackEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class PlaybackEventListener {

    private final RegionAggregator aggregator;
    private final JsonMapper jsonMapper;

    public PlaybackEventListener(RegionAggregator aggregator, JsonMapper jsonMapper) {
        this.aggregator = aggregator;
        this.jsonMapper = jsonMapper;
    }

    @KafkaListener(topics = KafkaTopicConfig.PLAYBACK_EVENTS_TOPIC)
    public void onEvent(String payload) {
        PlaybackEvent event = jsonMapper.readValue(payload, PlaybackEvent.class);
        aggregator.record(event);
    }
}