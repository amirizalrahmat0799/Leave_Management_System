package com.ahmadamirizal.leave.web.action;

import java.util.List;

import org.apache.struts2.interceptor.parameter.StrutsParameter;

import com.ahmadamirizal.leave.model.Department;
import com.ahmadamirizal.leave.model.Designation;
import com.ahmadamirizal.leave.service.LeaveRuleException;
import com.ahmadamirizal.leave.web.BaseAction;

/** Departments and designations. struts.xml maps each to this class with a "kind" parameter. */
public class LookupAction extends BaseAction {

    private String kind = "department";
    private Integer id;
    private String name;
    private String description;

    @Override
    public String execute() {
        return SUCCESS;
    }

    public String save() {
        try {
            if (name == null || name.isBlank() || name.length() > 100) throw new LeaveRuleException("Enter a name (up to 100 characters).");
            String desc = description == null || description.isBlank() ? null : description.trim();
            if (isDepartment()) {
                services().lookups.saveDepartment(new Department(id, name.trim(), desc));
            } else {
                services().lookups.saveDesignation(new Designation(id, name.trim(), desc));
            }
            flash("Saved " + name.trim() + ".");
        } catch (LeaveRuleException e) {
            flash(e.getMessage());
        } catch (RuntimeException e) {
            flash("Could not save: is the name already used?");
        }
        return SUCCESS;
    }

    public String delete() {
        int target = id == null ? -1 : id;
        boolean inUse = isDepartment() ? services().lookups.departmentInUse(target) : services().lookups.designationInUse(target);
        if (inUse) {
            flash("It's still assigned to employees, so it can't be deleted.");
        } else if (isDepartment()) {
            services().lookups.deleteDepartment(target);
            flash("Deleted.");
        } else {
            services().lookups.deleteDesignation(target);
            flash("Deleted.");
        }
        return SUCCESS;
    }

    public boolean isDepartment() { return !"designation".equals(kind); }
    public String getKind() { return isDepartment() ? "department" : "designation"; }
    public String getTitle() { return isDepartment() ? "Departments" : "Designations"; }
    public List<?> getItems() { return isDepartment() ? services().lookups.departments() : services().lookups.designations(); }

    public void setKind(String kind) { this.kind = kind; }   // from struts.xml <param>, not from the request

    @StrutsParameter public void setId(Integer id) { this.id = id; }
    @StrutsParameter public void setName(String name) { this.name = name; }
    @StrutsParameter public void setDescription(String description) { this.description = description; }
}
