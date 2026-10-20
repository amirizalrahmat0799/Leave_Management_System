package com.ahmadamirizal.leave.web;

import java.time.Clock;
import java.time.ZoneId;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import com.ahmadamirizal.leave.service.Services;

/** Looks up the container-managed connection pool (see META-INF/context.xml) and wires the services. */
@WebListener
public class AppStartup implements ServletContextListener {

    public static final ZoneId ZONE = ZoneId.of("Asia/Kuala_Lumpur");

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            DataSource ds = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/leave");
            Services.init(new Services(ds, Clock.system(ZONE)));
        } catch (NamingException e) {
            throw new IllegalStateException("The jdbc/leave DataSource is not configured", e);
        }
    }
}
