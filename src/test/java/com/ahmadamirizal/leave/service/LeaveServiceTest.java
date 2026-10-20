package com.ahmadamirizal.leave.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ahmadamirizal.leave.TestDatabase;
import com.ahmadamirizal.leave.model.LeaveApplication;
import com.ahmadamirizal.leave.model.LeaveBalance;
import com.ahmadamirizal.leave.model.LeaveStatus;
import com.ahmadamirizal.leave.model.User;

/** The leave rules, against the real schema and sample data (see db/sample-data.sql). */
class LeaveServiceTest {

    private static final int ANNUAL = 1, SICK = 2, EMERGENCY = 3, UNPAID = 4;

    private Services s;
    private User aina, daniel, hafiz, priya;   // HR; IT approver; IT engineer; Finance executive

    @BeforeEach
    void setUp() {
        s = TestDatabase.services();
        aina = s.users.findByUsername("aina").orElseThrow();
        daniel = s.users.findByUsername("daniel").orElseThrow();
        hafiz = s.users.findByUsername("hafiz").orElseThrow();
        priya = s.users.findByUsername("priya").orElseThrow();
    }

    private static LocalDate d(int month, int day) {
        return LocalDate.of(2026, month, day);
    }

    @Test
    void balancesUseOverridesAndCountApprovedAndPending() {
        LeaveBalance annual = s.leave.balance(hafiz, ANNUAL, 2026);
        assertEquals(0, new BigDecimal("16").compareTo(annual.getEntitled()));   // his override, not the default 14
        assertEquals(0, new BigDecimal("4").compareTo(annual.getApproved()));
        assertEquals(0, new BigDecimal("12").compareTo(annual.getAvailable()));

        LeaveBalance emergency = s.leave.balance(hafiz, EMERGENCY, 2026);
        assertEquals(0, new BigDecimal("0.5").compareTo(emergency.getPending()));
        assertEquals(0, new BigDecimal("2.5").compareTo(emergency.getAvailable()));

        assertTrue(s.leave.balance(hafiz, UNPAID, 2026).isUnlimited());
    }

    @Test
    void applyingCountsWorkingDaysOnly() {
        // Thursday 29 October to Tuesday 3 November: Thu, Fri, Mon, Tue
        LeaveApplication a = s.leave.apply(priya, ANNUAL, d(10, 29), d(11, 3), false, "Visiting family");
        assertEquals(0, new BigDecimal("4").compareTo(a.getDays()));
        assertEquals(LeaveStatus.PENDING, s.applications.find(a.getId()).orElseThrow().getStatus());
    }

    @Test
    void publicHolidaysAreNotCharged() {
        // The week of Malaysia Day (Wednesday 16 September): Monday to Friday costs 4 days, not 5
        assertEquals(0, new BigDecimal("4").compareTo(s.leave.daysFor(d(9, 14), d(9, 18), false)));
        LeaveRuleException e = assertThrows(LeaveRuleException.class,
                () -> s.leave.apply(priya, ANNUAL, d(12, 25), d(12, 25), false, "Christmas Day, by mistake"));
        assertTrue(e.getMessage().contains("public holidays"), e.getMessage());
    }

    @Test
    void halfDayCostsHalf() {
        LeaveApplication a = s.leave.apply(daniel, ANNUAL, d(10, 23), null, true, "Dentist");
        assertEquals(0, new BigDecimal("0.5").compareTo(a.getDays()));
        assertEquals(a.getStartDate(), a.getEndDate());
    }

    @Test
    void rejectsWhenBalanceIsTooLow() {
        // Priya has 14 days of annual leave with 4 pending; three weeks needs 15
        LeaveRuleException e = assertThrows(LeaveRuleException.class,
                () -> s.leave.apply(priya, ANNUAL, d(11, 9), d(11, 27), false, "Long holiday"));
        assertTrue(e.getMessage().contains("Not enough Annual Leave"), e.getMessage());
    }

    @Test
    void unpaidLeaveHasNoLimit() {
        LeaveApplication a = s.leave.apply(priya, UNPAID, d(11, 2), d(11, 30), false, "Sabbatical");
        assertEquals(0, new BigDecimal("21").compareTo(a.getDays()));
    }

    @Test
    void rejectsOverlapsWeekendsBackdatingAndCrossingYears() {
        assertThrows(LeaveRuleException.class,   // overlaps Hafiz's pending half day on 2 November
                () -> s.leave.apply(hafiz, ANNUAL, d(11, 2), d(11, 3), false, "Trip"));
        assertThrows(LeaveRuleException.class,   // Saturday and Sunday only
                () -> s.leave.apply(hafiz, ANNUAL, d(10, 24), d(10, 25), false, "Weekend"));
        assertThrows(LeaveRuleException.class,   // more than 30 days ago
                () -> s.leave.apply(hafiz, SICK, d(9, 1), d(9, 1), false, "Flu"));
        assertThrows(LeaveRuleException.class,
                () -> s.leave.apply(hafiz, ANNUAL, d(12, 30), LocalDate.of(2027, 1, 4), false, "New year"));
        assertThrows(LeaveRuleException.class,
                () -> s.leave.apply(hafiz, ANNUAL, d(11, 10), d(11, 9), false, "Backwards"));
        assertThrows(LeaveRuleException.class,
                () -> s.leave.apply(hafiz, ANNUAL, d(11, 10), d(11, 10), false, " "));
    }

    @Test
    void sickLeaveCanBeAppliedForAfterwards() {
        LeaveApplication a = s.leave.apply(hafiz, SICK, d(10, 12), d(10, 13), false, "Flu, MC attached");
        assertEquals(0, new BigDecimal("2").compareTo(a.getDays()));
    }

    @Test
    void whoMayDecide() {
        LeaveApplication hafizHalfDay = pendingOf(hafiz);   // IT department
        LeaveApplication priyaBreak = pendingOf(priya);     // Finance department

        assertTrue(LeaveService.canDecide(daniel, hafizHalfDay));     // approver, same department
        assertFalse(LeaveService.canDecide(daniel, priyaBreak));      // approver, other department
        assertTrue(LeaveService.canDecide(aina, priyaBreak));         // HR decides for everyone
        assertFalse(LeaveService.canDecide(hafiz, hafizHalfDay));     // nobody decides their own
        assertFalse(LeaveService.canDecide(priya, hafizHalfDay));     // plain employees can't decide
    }

    @Test
    void approveAndRejectOnlyOnce() {
        LeaveApplication a = pendingOf(hafiz);
        s.leave.decide(daniel, a.getId(), true, null);
        LeaveApplication after = s.applications.find(a.getId()).orElseThrow();
        assertEquals(LeaveStatus.APPROVED, after.getStatus());
        assertEquals(daniel.getId(), after.getDecidedBy());

        assertThrows(LeaveRuleException.class, () -> s.leave.decide(aina, a.getId(), false, "Too late"));
        assertThrows(LeaveRuleException.class, () -> s.leave.decide(daniel, pendingOf(priya).getId(), true, null));
        assertThrows(LeaveRuleException.class,   // rejecting needs a reason
                () -> s.leave.decide(aina, pendingOf(priya).getId(), false, ""));
    }

    @Test
    void cancellingOwnPendingAndFutureApprovedLeave() {
        LeaveApplication pending = pendingOf(hafiz);
        assertThrows(LeaveRuleException.class, () -> s.leave.cancel(priya, pending.getId()));   // not hers
        s.leave.cancel(hafiz, pending.getId());
        assertEquals(LeaveStatus.CANCELLED, s.applications.find(pending.getId()).orElseThrow().getStatus());

        // Hafiz's September holiday has already happened, so it stays
        LeaveApplication past = s.applications.forUser(hafiz.getId()).stream()
                .filter(x -> x.getStartDate().equals(d(9, 14))).findFirst().orElseThrow();
        assertThrows(LeaveRuleException.class, () -> s.leave.cancel(hafiz, past.getId()));

        // Approved leave that hasn't started can be withdrawn, and the days come back
        LeaveApplication future = s.leave.apply(priya, EMERGENCY, d(10, 27), d(10, 27), false, "Moving house");
        s.leave.decide(aina, future.getId(), true, null);
        BigDecimal before = s.leave.balance(priya, EMERGENCY, 2026).getAvailable();
        s.leave.cancel(priya, future.getId());
        assertEquals(0, before.add(BigDecimal.ONE).compareTo(s.leave.balance(priya, EMERGENCY, 2026).getAvailable()));
    }

    private LeaveApplication pendingOf(User u) {
        return s.applications.forUser(u.getId()).stream()
                .filter(x -> x.getStatus() == LeaveStatus.PENDING).findFirst().orElseThrow();
    }
}
