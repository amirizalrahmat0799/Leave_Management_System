package com.ahmadamirizal.leave;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;

import com.ahmadamirizal.leave.service.Services;

/** A fresh in-memory database with the real schema and sample data, and a clock fixed at 20 October 2026. */
public final class TestDatabase {

    public static final LocalDate TODAY = LocalDate.of(2026, 10, 20);   // a Tuesday
    private static final AtomicInteger COUNTER = new AtomicInteger();

    private TestDatabase() {}

    public static Services services() {
        return new Services(dataSource(), Clock.fixed(TODAY.atStartOfDay(ZoneId.of("Asia/Kuala_Lumpur")).toInstant(),
                ZoneId.of("Asia/Kuala_Lumpur")));
    }

    public static DataSource dataSource() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:leave" + COUNTER.incrementAndGet() + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        try (Connection c = ds.getConnection(); Statement s = c.createStatement()) {
            s.execute("RUNSCRIPT FROM 'classpath:/db/schema.sql'");
            s.execute("RUNSCRIPT FROM 'classpath:/db/sample-data.sql'");
            // The sample data inserts explicit ids; move the counters past them (MySQL does this by itself).
            for (String table : new String[] {"departments", "designations", "leave_types", "users", "leave_applications"}) {
                s.execute("ALTER TABLE " + table + " ALTER COLUMN id RESTART WITH 100");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not create the test database", e);
        }
        return ds;
    }
}
