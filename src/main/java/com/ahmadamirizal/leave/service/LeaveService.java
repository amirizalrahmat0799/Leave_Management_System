package com.ahmadamirizal.leave.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ahmadamirizal.leave.dao.HolidayDao;
import com.ahmadamirizal.leave.dao.LeaveApplicationDao;
import com.ahmadamirizal.leave.dao.LeaveTypeDao;
import com.ahmadamirizal.leave.model.LeaveApplication;
import com.ahmadamirizal.leave.model.LeaveBalance;
import com.ahmadamirizal.leave.model.LeaveStatus;
import com.ahmadamirizal.leave.model.LeaveType;
import com.ahmadamirizal.leave.model.User;

/** The leave rules: how many days an application costs, who may apply and decide, and what's left. */
public class LeaveService {

    /** How far back an application may start (sick leave is often applied for afterwards). */
    public static final int MAX_BACKDATE_DAYS = 30;
    private static final BigDecimal HALF = new BigDecimal("0.5");

    private final LeaveApplicationDao applications;
    private final LeaveTypeDao types;
    private final HolidayDao holidays;
    private final Clock clock;

    public LeaveService(LeaveApplicationDao applications, LeaveTypeDao types, HolidayDao holidays, Clock clock) {
        this.applications = applications;
        this.types = types;
        this.holidays = holidays;
        this.clock = clock;
    }

    public LocalDate today() {
        return LocalDate.now(clock);
    }

    /** Working days an application from start to end would use (0.5 for a half day). */
    public BigDecimal daysFor(LocalDate start, LocalDate end, boolean halfDay) {
        Set<LocalDate> off = holidays.between(start, end);
        if (halfDay) {
            return WorkingDays.isWorkingDay(start, off) ? HALF : BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(WorkingDays.count(start, end, off));
    }

    public List<LeaveBalance> balances(User user, int year) {
        Map<Integer, BigDecimal> overrides = types.entitlementOverrides(user.getId());
        LocalDate from = LocalDate.of(year, 1, 1), to = LocalDate.of(year, 12, 31);
        List<LeaveBalance> out = new ArrayList<>();
        for (LeaveType t : types.active()) {
            BigDecimal entitled = overrides.containsKey(t.getId()) ? overrides.get(t.getId()) : t.getDefaultDays();
            out.add(new LeaveBalance(t, entitled,
                    applications.sumDays(user.getId(), t.getId(), LeaveStatus.APPROVED, from, to),
                    applications.sumDays(user.getId(), t.getId(), LeaveStatus.PENDING, from, to)));
        }
        return out;
    }

    public LeaveBalance balance(User user, int leaveTypeId, int year) {
        return balances(user, year).stream()
                .filter(b -> b.getType().getId() == leaveTypeId)
                .findFirst()
                .orElseThrow(() -> new LeaveRuleException("That leave type is not available."));
    }

    /** Validates and saves a new application. Throws LeaveRuleException with a readable reason if it's not allowed. */
    public LeaveApplication apply(User user, Integer leaveTypeId, LocalDate start, LocalDate end, boolean halfDay,
                                  String reason) {
        if (leaveTypeId == null) throw new LeaveRuleException("Choose a leave type.");
        if (start == null) throw new LeaveRuleException("Choose the first day of your leave.");
        if (halfDay || end == null) end = start;
        if (end.isBefore(start)) throw new LeaveRuleException("The last day can't be before the first day.");
        if (start.getYear() != end.getYear()) {
            throw new LeaveRuleException("Leave can't cross into a new year. Apply for each year separately.");
        }
        if (start.isBefore(today().minusDays(MAX_BACKDATE_DAYS))) {
            throw new LeaveRuleException("Leave can be applied for at most " + MAX_BACKDATE_DAYS + " days after it starts.");
        }
        if (reason == null || reason.isBlank()) throw new LeaveRuleException("Give a reason for your leave.");
        if (reason.length() > 500) throw new LeaveRuleException("Keep the reason under 500 characters.");

        LeaveType type = types.find(leaveTypeId)
                .filter(LeaveType::isActive)
                .orElseThrow(() -> new LeaveRuleException("That leave type is not available."));
        BigDecimal days = daysFor(start, end, halfDay);
        if (days.signum() == 0) {
            throw new LeaveRuleException("Those dates fall on weekends or public holidays, so no leave is needed.");
        }
        if (applications.overlaps(user.getId(), start, end)) {
            throw new LeaveRuleException("You already have leave applied for some of these days.");
        }
        LeaveBalance balance = balance(user, type.getId(), start.getYear());
        if (!balance.isUnlimited() && balance.getAvailable().compareTo(days) < 0) {
            throw new LeaveRuleException("Not enough " + type.getName() + ": this needs " + days.stripTrailingZeros().toPlainString()
                    + " day(s) and you have " + balance.getAvailable().stripTrailingZeros().toPlainString() + " left.");
        }

        LeaveApplication a = new LeaveApplication();
        a.setUserId(user.getId());
        a.setLeaveTypeId(type.getId());
        a.setStartDate(start);
        a.setEndDate(end);
        a.setHalfDay(halfDay);
        a.setDays(days);
        a.setReason(reason.trim());
        a.setStatus(LeaveStatus.PENDING);
        applications.create(a);
        return a;
    }

    /**
     * Who may approve or reject: HR and admins for everyone; approvers for their own department.
     * Nobody decides on their own application.
     */
    public static boolean canDecide(User reviewer, LeaveApplication a) {
        if (reviewer.getId().equals(a.getUserId())) return false;
        if (reviewer.getRole().isStaffManager()) return true;
        return reviewer.isApprover() && reviewer.getDepartmentId() != null
                && reviewer.getDepartmentId().equals(a.getDepartmentId());
    }

    public void decide(User reviewer, int applicationId, boolean approve, String note) {
        LeaveApplication a = applications.find(applicationId)
                .orElseThrow(() -> new LeaveRuleException("That application doesn't exist."));
        if (!canDecide(reviewer, a)) throw new LeaveRuleException("You can't decide on this application.");
        if (a.getStatus() != LeaveStatus.PENDING) {
            throw new LeaveRuleException("This application is already " + a.getStatus().name().toLowerCase() + ".");
        }
        if (!approve && (note == null || note.isBlank())) {
            throw new LeaveRuleException("Say why the application is rejected.");
        }
        String trimmed = note == null || note.isBlank() ? null : note.trim();
        if (trimmed != null && trimmed.length() > 500) throw new LeaveRuleException("Keep the note under 500 characters.");
        boolean moved = applications.transition(applicationId, LeaveStatus.PENDING,
                approve ? LeaveStatus.APPROVED : LeaveStatus.REJECTED, reviewer.getId(), trimmed);
        if (!moved) throw new LeaveRuleException("Someone else decided on this application first.");
    }

    /** Employees can withdraw pending applications, and approved ones that haven't started yet. */
    public void cancel(User user, int applicationId) {
        LeaveApplication a = applications.find(applicationId)
                .orElseThrow(() -> new LeaveRuleException("That application doesn't exist."));
        if (!a.getUserId().equals(user.getId())) throw new LeaveRuleException("You can only cancel your own applications.");
        boolean allowed = a.getStatus() == LeaveStatus.PENDING
                || (a.getStatus() == LeaveStatus.APPROVED && a.getStartDate().isAfter(today()));
        if (!allowed) throw new LeaveRuleException("This application can no longer be cancelled.");
        if (!applications.transition(applicationId, a.getStatus(), LeaveStatus.CANCELLED, user.getId(), "Cancelled by the employee")) {
            throw new LeaveRuleException("This application changed in the meantime. Reload and try again.");
        }
    }
}
