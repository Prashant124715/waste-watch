package com.wastewatch.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * WasteWatch Runtime Database Migration
 *
 * Ensures that existing database tables with enum check constraints created
 * prior to the addition of PENDING and REJECTED volunteer registration states
 * are seamlessly updated without dropping tables or altering existing user records.
 */
@Component
@Order(1)
public class DatabaseMigration implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseMigration.class);

    private final JdbcTemplate jdbcTemplate;

    public DatabaseMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            // Check for outdated check constraint on APP_USERS.STATUS that does not allow PENDING
            List<Map<String, Object>> constraints = jdbcTemplate.queryForList(
                    "SELECT CONSTRAINT_NAME, CHECK_CLAUSE FROM INFORMATION_SCHEMA.CHECK_CONSTRAINTS " +
                    "WHERE TABLE_NAME = 'APP_USERS' AND CHECK_CLAUSE LIKE '%STATUS%ACTIVE%' AND CHECK_CLAUSE NOT LIKE '%PENDING%'"
            );

            for (Map<String, Object> constraint : constraints) {
                String name = (String) constraint.get("CONSTRAINT_NAME");
                log.info("Migrating outdated check constraint '{}' on APP_USERS.STATUS to include PENDING and REJECTED states", name);
                jdbcTemplate.execute("ALTER TABLE APP_USERS DROP CONSTRAINT " + name);
                jdbcTemplate.execute("ALTER TABLE APP_USERS ADD CONSTRAINT CHECK_APP_USERS_STATUS CHECK (\"STATUS\" IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'PENDING', 'REJECTED'))");
                log.info("Successfully migrated constraint '{}' to CHECK_APP_USERS_STATUS", name);
            }
        } catch (Exception e) {
            log.debug("Database check constraint migration check skipped: {}", e.getMessage());
        }
    }
}
