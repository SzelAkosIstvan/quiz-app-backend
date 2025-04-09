package com.vduzzle.QuizApp.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String jdbcUrl;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;
    @Value("${spring.datasource.hikari.maximum-pool-size}")
    private int maximumPoolSize;
    @Value("${spring.datasource.hikari.minimum-idle}")
    private int minimumIdle;
    @Value("${spring.datasource.hikari.idle-timeout}")
    private long idleTimeout;
    @Value("${spring.datasource.hikari.max-lifetime}")
    private long maxLifetime;
    @Value("${spring.datasource.hikari.connection-timeout}")
    private long connectionTimeout;
    @Value("${spring.datasource.hikari.pool-name}")
    private String poolName;

    private static HikariDataSource dataSource;

    @Bean
    public HikariDataSource getDataSource() {
        if(dataSource == null) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(username);
            config.setPassword(password);

            config.setMaximumPoolSize(maximumPoolSize);
            config.setMinimumIdle(minimumIdle);
            config.setIdleTimeout(idleTimeout);
            config.setMaxLifetime(maxLifetime);
            config.setConnectionTimeout(connectionTimeout);
            config.setPoolName(poolName);

            dataSource = new HikariDataSource(config);
        }
        return dataSource;
    }

    public static HikariDataSource getStaticDataSource() {
        return dataSource;
    }
}