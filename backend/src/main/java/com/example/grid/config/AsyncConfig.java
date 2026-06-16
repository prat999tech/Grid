package com.example.grid.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Defines the thread pool used for asynchronous work.
 *
 * <p>When a click arrives over the WebSocket, we hand the database work to this
 * pool instead of doing it on the receiving thread. That keeps the messaging
 * layer responsive even under a burst of clicks from many users. This is the
 * "use multithreading for asynchronous work" requirement.
 *
 * <p>Methods annotated with {@code @Async("claimExecutor")} run here.
 */
@Configuration
public class AsyncConfig {

    @Bean(name = "claimExecutor")
    public Executor claimExecutor(
            @Value("${grid.async.core-pool-size}") int corePoolSize,
            @Value("${grid.async.max-pool-size}") int maxPoolSize,
            @Value("${grid.async.queue-capacity}") int queueCapacity) {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);   // threads kept alive
        executor.setMaxPoolSize(maxPoolSize);      // threads created under load
        executor.setQueueCapacity(queueCapacity);  // backlog before new threads spawn
        executor.setThreadNamePrefix("claim-worker-");
        executor.initialize();
        return executor;
    }
}
