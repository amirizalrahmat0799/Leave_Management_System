package com.ahmadamirizal.leave.web.action;

import org.apache.struts2.interceptor.parameter.StrutsParameter;

import com.ahmadamirizal.leave.service.LeaveRuleException;
import com.ahmadamirizal.leave.web.BaseAction;

public class PasswordAction extends BaseAction {

    private String current;
    private String replacement;
    private String confirmation;

    public String show() {
        return INPUT;
    }

    @Override
    public String execute() {
        try {
            services().auth.changePassword(getCurrentUser(), current, replacement, confirmation);
            flash("Your password was changed.");
            return SUCCESS;
        } catch (LeaveRuleException e) {
            addActionError(e.getMessage());
            return INPUT;
        } finally {
            current = replacement = confirmation = null;
        }
    }

    public boolean isForced() {
        return getCurrentUser() != null && getCurrentUser().isMustChangePassword();
    }

    @StrutsParameter
    public void setCurrent(String current) { this.current = current; }

    @StrutsParameter
    public void setReplacement(String replacement) { this.replacement = replacement; }

    @StrutsParameter
    public void setConfirmation(String confirmation) { this.confirmation = confirmation; }
}
