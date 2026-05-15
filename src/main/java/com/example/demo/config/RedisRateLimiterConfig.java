package com.example.demo.config;

import java.time.Duration;
import java.util.function.Supplier;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;

@Configuration
public class RedisRateLimiterConfig {
    @Value("${app.rate-limit.capacity:15}")
    private long capacity;

    @Value("${app.rate-limit.window:1m}")
    private Duration window;

    @Bean
    public RedisClient redisClient(RedisProperties redisProperties) {
        RedisURI.Builder redisUriBuilder = RedisURI.builder()
                .withHost(redisProperties.getHost())
                .withPort(redisProperties.getPort())
                .withSsl(redisProperties.getSsl() != null && redisProperties.getSsl().isEnabled());

        if (redisProperties.getTimeout() != null) {
            redisUriBuilder.withTimeout(redisProperties.getTimeout());
        }

        if (redisProperties.getPassword() != null) {
            redisUriBuilder.withPassword(redisProperties.getPassword());
        }

        return RedisClient.create(redisUriBuilder.build());
    }

    @Bean
    public StatefulRedisConnection<String, byte[]> redisRateLimitConnection(RedisClient redisClient) {
        return redisClient.connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE));
    }

    @Bean
    public ProxyManager<String> lettuceBasedProxyManager(StatefulRedisConnection<String, byte[]> redisConnection) {
        return LettuceBasedProxyManager.builderFor(redisConnection)
                .build();
    }

    @Bean
    public Supplier<BucketConfiguration> bucketConfiguration() {
        return () -> BucketConfiguration.builder()
                .addLimit(Bandwidth.simple(capacity, window))
                .build();
    }
}
