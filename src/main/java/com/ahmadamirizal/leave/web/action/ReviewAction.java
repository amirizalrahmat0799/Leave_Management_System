package com.ahmadamirizal.leave.web.action;

import java.util.List;

import org.apache.struts2.interceptor.parameter.StrutsParameter;

import com.ahmadamirizal.leave.model.LeaveApplication;
import com.ahmadamirizal.leave.model.LeaveBalance;
import com.ahmadamirizal.leave.model.LeaveStatus;
import com.ahmadamirizal.leave.model.User;
import com.ahmadamirizal.leave.service.LeaveRuleException;
import com.ahmadamirizal.leave.service.LeaveService;
import com.ahmadamirizal.leave.web.BaseAction;

/** Approving and rejecting: HR and admins see every department, approvers their own. */
public class ReviewAction extends BaseAction {

    private String status = "PENDING";
    private Integer id;
    private String decision;
    private String note;
    private List<LeaveApplication> applications;
    private LeaveApplication application;
    private List<LeaveBalance> balances;

    @Override
    public String execute() {
        User me = getCurrentUser();
        if (!isReviewer(me)) return "forbidden";
        LeaveStatus filter = "ALL".equals(status) ? null : parseStatus(status);
        applications = services().applications.forReview(me.getRole().isStaffManager() ? null : me.getDepartmentId(), filter);
        applications.forEach(a -> a.setActionable(a.getStatus() == LeaveStatus.PENDING && LeaveService.canDecide(me, a)));
        return SUCCESS;
    }

    /** One application, with the employee's balance for that year. */
    public String view() {
        User me = getCurrentUser();
        application = services().applications.find(id == null ? -1 : id).orElse(null);
        if (application == null) return "notFound";
        boolean own = application.getUserId().equals(me.getId());
        if (!own && !LeaveService.canDecide(me, application)) return "forbidden";
        application.setActionable(application.getStatus() == LeaveStatus.PENDING && LeaveService.canDecide(me, application));
        User employee = services().users.find(application.getUserId()).orElseThrow();
        balances = services().leave.balances(employee, application.getStartDate().getYear());
        return SUCCESS;
    }

    /** POST, protected by the token interceptor. */
    public String decide() {
        try {
            boolean approve = "approve".equals(decision);
            services().leave.decide(getCurrentUser(), id == null ? -1 : id, approve, note);
            flash(approve ? "Application approved." : "Application rejected.");
        } catch (LeaveRuleException e) {
            flash(e.getMessage());
        }
        return SUCCESS;
    }

    private static boolean isReviewer(User me) {
        return me.getRole().isStaffManager() || me.isApprover();
    }

    private static LeaveStatus parseStatus(String s) {
        try {
            return LeaveStatus.valueOf(s);
        } catch (IllegalArgumentException e) {
            return LeaveStatus.PENDING;
        }
    }

    public String getStatus() { return status; }
    public Integer getId() { return id; }
    public List<LeaveApplication> getApplications() { return applications; }
    public LeaveApplication getApplication() { return application; }
    public List<LeaveBalance> getBalances() { return balances; }
    public LeaveStatus[] getStatuses() { return LeaveStatus.values(); }

    @StrutsParameter
    public void setStatus(String status) { this.status = status; }

    @StrutsParameter
    public void setId(Integer id) { this.id = id; }

    @StrutsParameter
    public void setDecision(String decision) { this.decision = decision; }

    @StrutsParameter
    public void setNote(String note) { this.note = note; }
}
