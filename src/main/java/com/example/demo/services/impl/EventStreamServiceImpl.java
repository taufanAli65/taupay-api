package com.example.demo.services.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.demo.services.EventStreamService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EventStreamServiceImpl implements EventStreamService {
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    @Override
    public SseEmitter subscribe(String channel, long timeoutMillis) {
        SseEmitter emitter = new SseEmitter(timeoutMillis);
        emitters.put(channel, emitter);
        emitter.onCompletion(() -> emitters.remove(channel));
        emitter.onTimeout(() -> emitters.remove(channel));
        emitter.onError(error -> emitters.remove(channel));

        try {
            emitter.send(SseEmitter.event().name("connected").data("listening"));
        } catch (Exception ex) {
            emitters.remove(channel);
            throw new IllegalStateException("Failed to open SSE channel", ex);
        }

        return emitter;
    }

    @Override
    public void publish(String channel, String eventName, Object data) {
        SseEmitter emitter = emitters.remove(channel);
        if (emitter == null) {
            log.info("SSE event for channel {}: {}", channel, data);
            return;
        }

        try {
            emitter.send(SseEmitter.event().name(eventName).data(data));
            emitter.complete();
        } catch (Exception ex) {
            emitter.completeWithError(ex);
            log.warn("Failed to send SSE event for channel {}", channel, ex);
        }
    }
}
