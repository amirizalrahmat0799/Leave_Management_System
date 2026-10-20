package com.ahmadamirizal.leave.web.action;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.apache.struts2.interceptor.parameter.StrutsParameter;

import com.ahmadamirizal.leave.model.LeaveBalance;
import com.ahmadamirizal.leave.service.LeaveRuleException;
import com.ahmadamirizal.leave.web.BaseAction;

public class ApplyLeaveAction extends BaseAction {

    private String leaveTypeId;   // a String, so an empty choice is a rule message rather than a conversion error
    private String startDate;
    private String endDate;
    private boolean halfDay;
    private String reason;
    private List<LeaveBalance> balances;

    /** GET: the form. */
    public String show() {
        loadBalances();
        return INPUT;
    }

    /** POST: validate with the leave rules and save. */
    @Override
    public String execute() {
        try {
            LocalDate start = parse(startDate, "first day");
            LocalDate end = endDate == null || endDate.isBlank() ? null : parse(endDate, "last day");
            var a = services().leave.apply(getCurrentUser(), parseId(leaveTypeId), start, end, halfDay, reason);
            flash("Leave applied for " + a.getDays().stripTrailingZeros().toPlainString() + " day(s). Waiting for approval.");
            return SUCCESS;
        } catch (LeaveRuleException e) {
            addActionError(e.getMessage());
            loadBalances();
            return INPUT;
        }
    }

    private void loadBalances() {
        balances = services().leave.balances(getCurrentUser(), services().leave.today().getYear());
    }

    private static Integer parseId(String value) {
        try {
            return value == null || value.isBlank() ? null : Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static LocalDate parse(String value, String what) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            throw new LeaveRuleException("The " + what + " is not a valid date.");
        }
    }

    public List<LeaveBalance> getBalances() {
        if (balances == null) loadBalances();
        return balances;
    }
    public String getLeaveTypeId() { return leaveTypeId; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public boolean isHalfDay() { return halfDay; }
    public String getReason() { return reason; }

    @StrutsParameter
    public void setLeaveTypeId(String leaveTypeId) { this.leaveTypeId = leaveTypeId; }

    @StrutsParameter
    public void setStartDate(String startDate) { this.startDate = startDate; }

    @StrutsParameter
    public void setEndDate(String endDate) { this.endDate = endDate; }

    @StrutsParameter
    public void setHalfDay(boolean halfDay) { this.halfDay = halfDay; }

    @StrutsParameter
    public void setReason(String reason) { this.reason = reason; }
}
