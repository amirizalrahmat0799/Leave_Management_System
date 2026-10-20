package com.ahmadamirizal.leave.web.action;

import java.math.BigDecimal;
import java.util.List;

import org.apache.struts2.interceptor.parameter.StrutsParameter;

import com.ahmadamirizal.leave.model.LeaveType;
import com.ahmadamirizal.leave.service.LeaveRuleException;
import com.ahmadamirizal.leave.web.BaseAction;

public class LeaveTypeAction extends BaseAction {

    private Integer id;
    private String code;
    private String name;
    private String description;
    private String defaultDays;
    private boolean active = true;

    @Override
    public String execute() {
        return SUCCESS;
    }

    public String save() {
        try {
            if (code == null || !code.trim().matches("[A-Za-z0-9]{1,10}")) throw new LeaveRuleException("Codes are 1 to 10 letters or digits.");
            if (name == null || name.isBlank()) throw new LeaveRuleException("Enter a name.");
            LeaveType t = new LeaveType();
            t.setId(id);
            t.setCode(code.trim().toUpperCase());
            t.setName(name.trim());
            t.setDescription(description == null || description.isBlank() ? null : description.trim());
            t.setActive(active);
            if (defaultDays != null && !defaultDays.isBlank()) {
                try {
                    t.setDefaultDays(new BigDecimal(defaultDays.trim()));
                } catch (NumberFormatException e) {
                    throw new LeaveRuleException("Default days must be a number, or blank for unlimited.");
                }
            }
            services().leaveTypes.save(t);
            flash("Saved " + t.getName() + ".");
        } catch (LeaveRuleException e) {
            flash(e.getMessage());
        } catch (RuntimeException e) {
            flash("Could not save: is the code already used?");
        }
        return SUCCESS;
    }

    public List<LeaveType> getLeaveTypes() { return services().leaveTypes.all(); }

    @StrutsParameter public void setId(Integer id) { this.id = id; }
    @StrutsParameter public void setCode(String code) { this.code = code; }
    @StrutsParameter public void setName(String name) { this.name = name; }
    @StrutsParameter public void setDescription(String description) { this.description = description; }
    @StrutsParameter public void setDefaultDays(String defaultDays) { this.defaultDays = defaultDays; }
    @StrutsParameter public void setActive(boolean active) { this.active = active; }
}
