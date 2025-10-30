package com.trustline.trustline.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base class for integration tests with containerized dependencies.
 * Provides shared configuration for PostgreSQL container.
 *
 * This uses Testcontainers to provide isolated, reproducible test environments with:
 * - PostgreSQL for database operations
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(BaseIT.TestContainersConfig.class)
public abstract class BaseIT {

    /**
     * Testcontainers configuration using Spring Boot's @ServiceConnection.
     * Automatically configures DataSource connection factory.
     */
    @TestConfiguration(proxyBeanMethods = false)
    public static class TestContainersConfig {

        @Bean
        @ServiceConnection
        public PostgreSQLContainer<?> postgreSQLContainer() {
            return new PostgreSQLContainer<>("postgres:17-alpine")
                    .withDatabaseName("trustline")
                    .withUsername("test")
                    .withPassword("test");
        }
    }
}
