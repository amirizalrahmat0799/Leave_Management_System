package com.ahmadamirizal.leave.service;

import java.time.Clock;
import javax.sql.DataSource;

import com.ahmadamirizal.leave.dao.HolidayDao;
import com.ahmadamirizal.leave.dao.LeaveApplicationDao;
import com.ahmadamirizal.leave.dao.LeaveTypeDao;
import com.ahmadamirizal.leave.dao.LookupDao;
import com.ahmadamirizal.leave.dao.ReportDao;
import com.ahmadamirizal.leave.dao.UserDao;

/**
 * The application's objects, wired by hand at startup (no dependency-injection framework, in keeping with the era).
 * {@link com.ahmadamirizal.leave.web.AppStartup} creates one from Tomcat's JNDI DataSource; tests create their own.
 */
public final class Services {

    private static volatile Services instance;

    public final UserDao users;
    public final LookupDao lookups;
    public final LeaveTypeDao leaveTypes;
    public final HolidayDao holidays;
    public final LeaveApplicationDao applications;
    public final ReportDao reports;
    public final AuthService auth;
    public final LeaveService leave;
    public final LoginThrottle loginThrottle;

    public Services(DataSource dataSource, Clock clock) {
        users = new UserDao(dataSource);
        lookups = new LookupDao(dataSource);
        leaveTypes = new LeaveTypeDao(dataSource);
        holidays = new HolidayDao(dataSource);
        applications = new LeaveApplicationDao(dataSource);
        reports = new ReportDao(dataSource);
        auth = new AuthService(users, clock);
        leave = new LeaveService(applications, leaveTypes, holidays, clock);
        loginThrottle = new LoginThrottle(clock);
    }

    public static Services get() {
        Services s = instance;
        if (s == null) throw new IllegalStateException("Services not initialised");
        return s;
    }

    public static void init(Services services) {
        instance = services;
    }
}
