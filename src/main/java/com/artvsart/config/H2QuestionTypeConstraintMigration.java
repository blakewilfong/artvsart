package com.artvsart.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class H2QuestionTypeConstraintMigration
        implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(
            H2QuestionTypeConstraintMigration.class
    );

    private static final String CHECK_CONSTRAINT_QUERY = """
            SELECT tc.CONSTRAINT_NAME, cc.CHECK_CLAUSE
            FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc
            JOIN INFORMATION_SCHEMA.CHECK_CONSTRAINTS cc
              ON cc.CONSTRAINT_CATALOG = tc.CONSTRAINT_CATALOG
             AND cc.CONSTRAINT_SCHEMA = tc.CONSTRAINT_SCHEMA
             AND cc.CONSTRAINT_NAME = tc.CONSTRAINT_NAME
            WHERE tc.TABLE_SCHEMA = CURRENT_SCHEMA
              AND tc.TABLE_NAME = 'ARTWORK_QUESTIONS'
              AND tc.CONSTRAINT_TYPE = 'CHECK'
            """;

    private final JdbcTemplate jdbcTemplate;

    public H2QuestionTypeConstraintMigration(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        migrate();
    }

    void migrate() {
        List<CheckConstraint> constraints = jdbcTemplate.query(
                CHECK_CONSTRAINT_QUERY,
                (resultSet, rowNumber) -> new CheckConstraint(
                        resultSet.getString("CONSTRAINT_NAME"),
                        resultSet.getString("CHECK_CLAUSE")
                )
        );

        constraints.stream()
                .filter(this::limitsQuestionTypeValues)
                .forEach(this::drop);
    }

    private boolean limitsQuestionTypeValues(
            CheckConstraint constraint
    ) {
        return constraint.clause() != null
                && constraint.clause()
                .toUpperCase(Locale.ROOT)
                .contains("QUESTION_TYPE");
    }

    private void drop(CheckConstraint constraint) {
        String escapedName = constraint.name()
                .replace("\"", "\"\"");

        jdbcTemplate.execute(
                "ALTER TABLE artwork_questions DROP CONSTRAINT \""
                        + escapedName
                        + "\""
        );

        LOGGER.info(
                "Removed legacy artwork question type constraint {}",
                constraint.name()
        );
    }

    private record CheckConstraint(
            String name,
            String clause
    ) {
    }
}
