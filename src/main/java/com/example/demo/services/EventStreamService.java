package com.example.demo.services;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface EventStreamService {
    SseEmitter subscribe(String channel, long timeoutMillis);

    void publish(String channel, String eventName, Object data);
}
