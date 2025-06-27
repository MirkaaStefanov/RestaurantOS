package com.example.RestaurantOS.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync // This annotation enables Spring's asynchronous method execution capability
public class AsyncConfig {

    /**
     * Defines a primary ThreadPoolTaskExecutor bean.
     * This bean will be used by default for any asynchronous operations
     * that do not explicitly specify a TaskExecutor.
     *
     * @return A configured ThreadPoolTaskExecutor instance.
     */
    @Bean(name = "taskExecutor") // Give it a specific name
    @Primary // Mark this as the primary TaskExecutor
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // Core pool size: The number of threads to keep in the pool, even if they are idle.
        executor.setCorePoolSize(5);
        // Maximum pool size: The maximum number of threads that can be created.
        executor.setMaxPoolSize(10);
        // Queue capacity: The size of the queue for holding tasks before they are executed.
        executor.setQueueCapacity(25);
        // Thread name prefix: A prefix for the names of threads created by this executor.
        executor.setThreadNamePrefix("RestaurantApp-Task-");
        // Initialize the executor: This sets up the thread pool.
        executor.initialize();
        return executor;
    }

    // If you have other specific TaskExecutors (e.g., for WebSockets),
    // you can define them without @Primary or with a different name.
    // Spring will then know to use 'taskExecutor' as the default.
}

