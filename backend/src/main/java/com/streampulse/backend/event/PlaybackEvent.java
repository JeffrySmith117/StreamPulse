package com.streampulse.backend.event;

import java.time.Instant;

/**
 * Evento enviado pelo player de video de um usuario.
 * Cada evento informa a sessao, o estado (UF) de onde o usuario assiste,
 * o que aconteceu e, para eventos de buffer, quanto tempo o video ficou travado.
 */
public record PlaybackEvent(
        String sessionId,
        String region,
        EventType type,
        long bufferDurationMs,
        Instant timestamp
) {
}