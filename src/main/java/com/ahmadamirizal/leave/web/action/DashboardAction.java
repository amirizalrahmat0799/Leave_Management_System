package com.ahmadamirizal.leave.web.action;

import java.time.LocalDate;
import java.util.List;

import com.ahmadamirizal.leave.model.LeaveApplication;
import com.ahmadamirizal.leave.model.LeaveBalance;
import com.ahmadamirizal.leave.model.LeaveStatus;
import com.ahmadamirizal.leave.model.PublicHoliday;
import com.ahmadamirizal.leave.model.User;
import com.ahmadamirizal.leave.web.BaseAction;

public class DashboardAction extends BaseAction {

    private List<LeaveBalance> balances;
    private int myPending;
    private int toReview;
    private List<LeaveApplication> onLeaveToday;
    private List<PublicHoliday> upcomingHolidays;

    @Override
    public String execute() {
        User me = getCurrentUser();
        LocalDate today = services().leave.today();
        balances = services().leave.balances(me, today.getYear());
        myPending = (int) services().applications.forUser(me.getId()).stream()
                .filter(a -> a.getStatus() == LeaveStatus.PENDING).count();

        Integer scope = reviewScope(me);
        if (isReviewer()) {
            toReview = (int) services().applications.forReview(scope, LeaveStatus.PENDING).stream()
                    .filter(a -> !a.getUserId().equals(me.getId())).count();
        }
        onLeaveToday = services().applications.onLeave(today).stream()
                .filter(a -> scope == null || scope.equals(a.getDepartmentId()))
                .toList();
        upcomingHolidays = services().holidays.forYear(today.getYear()).stream()
                .filter(h -> !h.getDate().isBefore(today))
                .limit(3)
                .toList();
        return SUCCESS;
    }

    /** HR and admins see everyone; approvers their department; others their department too (who's away today). */
    static Integer reviewScope(User me) {
        return me.getRole().isStaffManager() ? null : me.getDepartmentId();
    }

    public boolean isReviewer() {
        User me = getCurrentUser();
        return me.getRole().isStaffManager() || me.isApprover();
    }

    public List<LeaveBalance> getBalances() { return balances; }
    public int getMyPending() { return myPending; }
    public int getToReview() { return toReview; }
    public List<LeaveApplication> getOnLeaveToday() { return onLeaveToday; }
    public List<PublicHoliday> getUpcomingHolidays() { return upcomingHolidays; }
    public boolean isNobodyOnLeave() { return onLeaveToday.isEmpty(); }
    public boolean isNoUpcomingHolidays() { return upcomingHolidays.isEmpty(); }
}
