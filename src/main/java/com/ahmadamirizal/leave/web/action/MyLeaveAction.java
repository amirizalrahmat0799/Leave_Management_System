package com.ahmadamirizal.leave.web.action;

import java.util.List;

import org.apache.struts2.interceptor.parameter.StrutsParameter;

import com.ahmadamirizal.leave.model.LeaveApplication;
import com.ahmadamirizal.leave.model.LeaveBalance;
import com.ahmadamirizal.leave.model.LeaveStatus;
import com.ahmadamirizal.leave.service.LeaveRuleException;
import com.ahmadamirizal.leave.web.BaseAction;

/** The signed-in employee's own leave: balances, history, and cancelling. */
public class MyLeaveAction extends BaseAction {

    private Integer id;
    private int year;
    private List<LeaveBalance> balances;
    private List<LeaveApplication> applications;

    @Override
    public String execute() {
        if (year == 0) year = services().leave.today().getYear();
        balances = services().leave.balances(getCurrentUser(), year);
        applications = services().applications.forUser(getCurrentUser().getId()).stream()
                .filter(a -> a.getStartDate().getYear() == year)
                .toList();
        applications.forEach(a -> a.setActionable(canCancel(a)));
        return SUCCESS;
    }

    /** POST, protected by the token interceptor. */
    public String cancel() {
        try {
            services().leave.cancel(getCurrentUser(), id == null ? -1 : id);
            flash("Your application was cancelled.");
        } catch (LeaveRuleException e) {
            flash(e.getMessage());
        }
        return SUCCESS;
    }

    private boolean canCancel(LeaveApplication a) {
        return a.getStatus() == LeaveStatus.PENDING
                || (a.getStatus() == LeaveStatus.APPROVED && a.getStartDate().isAfter(services().leave.today()));
    }

    public int getYear() { return year; }

    @StrutsParameter
    public void setYear(int year) { this.year = year; }

    @StrutsParameter
    public void setId(Integer id) { this.id = id; }

    public List<LeaveBalance> getBalances() { return balances; }
    public List<LeaveApplication> getApplications() { return applications; }
}
