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
            // Find check constraints on APP_USERS.STATUS that do not allow PENDING
            List<Map<String, Object>> constraints;
            try {
                // Standard H2 2.x query joining TABLE_CONSTRAINTS and CHECK_CONSTRAINTS
                constraints = jdbcTemplate.queryForList(
                        "SELECT tc.CONSTRAINT_NAME, cc.CHECK_CLAUSE " +
                        "FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc " +
                        "JOIN INFORMATION_SCHEMA.CHECK_CONSTRAINTS cc " +
                        "  ON tc.CONSTRAINT_NAME = cc.CONSTRAINT_NAME " +
                        "WHERE UPPER(tc.TABLE_NAME) = 'APP_USERS' " +
                        "  AND (UPPER(cc.CHECK_CLAUSE) LIKE '%STATUS%ACTIVE%' OR UPPER(cc.CHECK_CLAUSE) LIKE '%\"STATUS\"%') " +
                        "  AND UPPER(cc.CHECK_CLAUSE) NOT LIKE '%PENDING%'"
                );
            } catch (Exception e) {
                // Fallback for H2 configurations without TABLE_CONSTRAINTS join requirement
                constraints = jdbcTemplate.queryForList(
                        "SELECT CONSTRAINT_NAME, CHECK_CLAUSE FROM INFORMATION_SCHEMA.CHECK_CONSTRAINTS " +
                        "WHERE (UPPER(CHECK_CLAUSE) LIKE '%STATUS%ACTIVE%' OR UPPER(CHECK_CLAUSE) LIKE '%\"STATUS\"%') " +
                        "  AND UPPER(CHECK_CLAUSE) NOT LIKE '%PENDING%'"
                );
            }

            for (Map<String, Object> constraint : constraints) {
                String name = (String) constraint.get("CONSTRAINT_NAME");
                log.info("Migrating outdated check constraint '{}' on APP_USERS.STATUS to include PENDING and REJECTED states", name);
                try {
                    jdbcTemplate.execute("ALTER TABLE APP_USERS DROP CONSTRAINT " + name);
                    log.info("Dropped outdated constraint '{}'", name);
                } catch (Exception dropEx) {
                    log.warn("Could not drop constraint '{}': {}", name, dropEx.getMessage());
                }
            }

            // Ensure the updated constraint CHECK_APP_USERS_STATUS is added
            try {
                jdbcTemplate.execute("ALTER TABLE APP_USERS ADD CONSTRAINT CHECK_APP_USERS_STATUS CHECK (\"STATUS\" IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'PENDING', 'REJECTED'))");
                log.info("Successfully added constraint CHECK_APP_USERS_STATUS to APP_USERS");
            } catch (Exception addEx) {
                // Constraint may already exist if already added previously
                log.debug("CHECK_APP_USERS_STATUS constraint already present or skipped: {}", addEx.getMessage());
            }

            // Ensure room_id in INSPECTIONS and VIOLATIONS allows NULL for building-level inspections
            try {
                jdbcTemplate.execute("ALTER TABLE INSPECTIONS ALTER COLUMN ROOM_ID DROP NOT NULL");
                log.info("Successfully updated INSPECTIONS.ROOM_ID to allow NULL for building-level inspections");
            } catch (Exception ex) {
                log.debug("INSPECTIONS.ROOM_ID nullability migration skipped or already applied: {}", ex.getMessage());
            }

            try {
                jdbcTemplate.execute("ALTER TABLE VIOLATIONS ALTER COLUMN ROOM_ID DROP NOT NULL");
                log.info("Successfully updated VIOLATIONS.ROOM_ID to allow NULL for building-level inspections");
            } catch (Exception ex) {
                log.debug("VIOLATIONS.ROOM_ID nullability migration skipped or already applied: {}", ex.getMessage());
            }

            // Backfill building_id for any legacy inspections and violations that only had room_id
            try {
                jdbcTemplate.execute(
                        "UPDATE INSPECTIONS SET BUILDING_ID = (SELECT BUILDING_ID FROM ROOMS WHERE ROOMS.ID = INSPECTIONS.ROOM_ID) " +
                        "WHERE BUILDING_ID IS NULL AND ROOM_ID IS NOT NULL"
                );
                log.info("Backfilled BUILDING_ID on legacy INSPECTIONS records");
            } catch (Exception ex) {
                log.debug("INSPECTIONS BUILDING_ID backfill skipped: {}", ex.getMessage());
            }

            try {
                jdbcTemplate.execute(
                        "UPDATE VIOLATIONS SET BUILDING_ID = (SELECT BUILDING_ID FROM ROOMS WHERE ROOMS.ID = VIOLATIONS.ROOM_ID) " +
                        "WHERE BUILDING_ID IS NULL AND ROOM_ID IS NOT NULL"
                );
                log.info("Backfilled BUILDING_ID on legacy VIOLATIONS records");
            } catch (Exception ex) {
                log.debug("VIOLATIONS BUILDING_ID backfill skipped: {}", ex.getMessage());
            }
        } catch (Exception e) {
            log.warn("Database check constraint migration check skipped: {}", e.getMessage());
        }
    }
}
