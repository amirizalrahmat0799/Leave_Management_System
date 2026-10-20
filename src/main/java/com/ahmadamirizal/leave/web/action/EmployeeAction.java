package com.ahmadamirizal.leave.web.action;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.struts2.interceptor.parameter.StrutsParameter;

import com.ahmadamirizal.leave.model.Department;
import com.ahmadamirizal.leave.model.Designation;
import com.ahmadamirizal.leave.model.LeaveType;
import com.ahmadamirizal.leave.model.Role;
import com.ahmadamirizal.leave.model.User;
import com.ahmadamirizal.leave.service.AuthService;
import com.ahmadamirizal.leave.service.LeaveRuleException;
import com.ahmadamirizal.leave.web.BaseAction;

/** HR and admins: the staff list, adding and editing people, leave entitlements, and password resets. */
public class EmployeeAction extends BaseAction {

    private String q;
    private List<User> employees;

    private Integer id;
    private String username;
    private String role = "EMPLOYEE";
    private String employeeNo;
    private String firstName;
    private String lastName;
    private String email;
    private String departmentId;    // Strings: the "none" choice in the form is an empty value
    private String designationId;
    private boolean approver;
    private boolean active = true;
    private String initialPassword;

    private String[] typeIds = new String[0];
    private String[] typeDays = new String[0];
    private Map<Integer, BigDecimal> overrides = Map.of();

    private String temporaryPassword;

    @Override
    public String execute() {
        employees = q == null || q.isBlank() ? services().users.all() : services().users.search(q.trim());
        return SUCCESS;
    }

    /** GET: the add/edit form. */
    public String edit() {
        if (id != null) {
            User u = services().users.find(id).orElse(null);
            if (u == null) return "notFound";
            username = u.getUsername();
            role = u.getRole().name();
            employeeNo = u.getEmployeeNo();
            firstName = u.getFirstName();
            lastName = u.getLastName();
            email = u.getEmail();
            departmentId = u.getDepartmentId() == null ? "" : u.getDepartmentId().toString();
            designationId = u.getDesignationId() == null ? "" : u.getDesignationId().toString();
            approver = u.isApprover();
            active = u.isActive();
            overrides = services().leaveTypes.entitlementOverrides(id);
        }
        return INPUT;
    }

    /** POST: create or update, including the entitlements. */
    public String save() {
        try {
            User me = getCurrentUser();
            Role newRole = Role.valueOf(role);
            User existing = id == null ? null : services().users.find(id).orElseThrow(() -> new LeaveRuleException("That employee doesn't exist."));
            // HR manages employees; only an admin can create or change HR and admin accounts.
            if (me.getRole() != Role.ADMIN && (newRole != Role.EMPLOYEE || (existing != null && existing.getRole() != Role.EMPLOYEE))) {
                throw new LeaveRuleException("Only an administrator can manage HR and administrator accounts.");
            }
            if (existing != null && existing.getId().equals(me.getId()) && (!active || newRole != me.getRole())) {
                throw new LeaveRuleException("You can't deactivate yourself or change your own role.");
            }
            if (isBlank(username) || !username.trim().matches("[A-Za-z0-9._-]{3,50}")) {
                throw new LeaveRuleException("Usernames are 3 to 50 letters, digits, dots, dashes or underscores.");
            }
            if (isBlank(firstName) || isBlank(lastName)) throw new LeaveRuleException("Enter the first and last name.");
            if (services().users.usernameTaken(username.trim(), id)) throw new LeaveRuleException("That username is taken.");

            User u = existing != null ? existing : new User();
            u.setUsername(username.trim());
            u.setRole(newRole);
            u.setEmployeeNo(blankToNull(employeeNo));
            u.setFirstName(firstName.trim());
            u.setLastName(lastName.trim());
            u.setEmail(blankToNull(email));
            u.setDepartmentId(parseId(departmentId));
            u.setDesignationId(parseId(designationId));
            u.setApprover(approver);
            u.setActive(active);
            if (existing == null) {
                AuthService.validateNewPassword(initialPassword, initialPassword);
                u.setPasswordHash(AuthService.hash(initialPassword));
                u.setMustChangePassword(true);
                services().users.create(u);
            } else {
                services().users.update(u);
            }
            saveEntitlements(u.getId());
            flash((existing == null ? "Added " : "Saved ") + u.getFullName() + ".");
            return SUCCESS;
        } catch (LeaveRuleException e) {
            addActionError(e.getMessage());
            if (id != null) overrides = services().leaveTypes.entitlementOverrides(id);
            return INPUT;
        } finally {
            initialPassword = null;
        }
    }

    /** Blank means "use the leave type's default". */
    private void saveEntitlements(int userId) {
        for (int i = 0; i < typeIds.length && i < typeDays.length; i++) {
            int typeId = Integer.parseInt(typeIds[i]);
            String raw = typeDays[i] == null ? "" : typeDays[i].trim();
            BigDecimal days = null;
            if (!raw.isEmpty()) {
                try {
                    days = new BigDecimal(raw);
                } catch (NumberFormatException e) {
                    throw new LeaveRuleException("Entitlements must be numbers of days, like 14 or 7.5.");
                }
                if (days.signum() < 0 || days.compareTo(new BigDecimal("365")) > 0) {
                    throw new LeaveRuleException("Entitlements must be between 0 and 365 days.");
                }
            }
            services().leaveTypes.setEntitlement(userId, typeId, days);
        }
    }

    /** POST: sets a temporary password the employee must change at their next sign-in. */
    public String resetPassword() {
        try {
            User target = services().users.find(id == null ? -1 : id).orElseThrow(() -> new LeaveRuleException("That employee doesn't exist."));
            if (getCurrentUser().getRole() != Role.ADMIN && target.getRole() != Role.EMPLOYEE) {
                throw new LeaveRuleException("Only an administrator can reset HR and administrator passwords.");
            }
            services().auth.resetPassword(target.getId(), temporaryPassword);
            flash("Temporary password set for " + target.getFullName() + ". They will choose a new one when they sign in.");
        } catch (LeaveRuleException e) {
            flash(e.getMessage());
        } finally {
            temporaryPassword = null;
        }
        return SUCCESS;
    }

    private static Integer parseId(String s) {
        try {
            return isBlank(s) ? null : Integer.valueOf(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String blankToNull(String s) {
        return isBlank(s) ? null : s.trim();
    }

    // Lists for the form
    public List<Department> getDepartments() { return services().lookups.departments(); }
    public List<Designation> getDesignations() { return services().lookups.designations(); }
    public List<LeaveType> getLeaveTypes() { return services().leaveTypes.all(); }
    public Role[] getRoles() { return Role.values(); }

    /** One row per leave type for the entitlement table: the default and this person's override, if any. */
    public record EntitlementRow(int typeId, String code, String name, String defaultDays, String override) {
        public int getTypeId() { return typeId; }
        public String getCode() { return code; }
        public String getName() { return name; }
        public String getDefaultDays() { return defaultDays; }
        public String getOverride() { return override; }
    }

    public List<EntitlementRow> getEntitlementRows() {
        return services().leaveTypes.all().stream()
                .map(t -> new EntitlementRow(t.getId(), t.getCode(), t.getName(),
                        t.isUnlimited() ? "unlimited" : t.getDefaultDays().stripTrailingZeros().toPlainString(),
                        overrides.containsKey(t.getId()) ? overrides.get(t.getId()).stripTrailingZeros().toPlainString() : ""))
                .toList();
    }

    public List<User> getEmployees() { return employees; }
    public String getQ() { return q; }
    public Integer getId() { return id; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public String getEmployeeNo() { return employeeNo; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getDepartmentId() { return departmentId; }
    public String getDesignationId() { return designationId; }
    public boolean isApprover() { return approver; }
    public boolean isActive() { return active; }

    @StrutsParameter public void setQ(String q) { this.q = q; }
    @StrutsParameter public void setId(Integer id) { this.id = id; }
    @StrutsParameter public void setUsername(String username) { this.username = username; }
    @StrutsParameter public void setRole(String role) { this.role = role; }
    @StrutsParameter public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }
    @StrutsParameter public void setFirstName(String firstName) { this.firstName = firstName; }
    @StrutsParameter public void setLastName(String lastName) { this.lastName = lastName; }
    @StrutsParameter public void setEmail(String email) { this.email = email; }
    @StrutsParameter public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }
    @StrutsParameter public void setDesignationId(String designationId) { this.designationId = designationId; }
    @StrutsParameter public void setApprover(boolean approver) { this.approver = approver; }
    @StrutsParameter public void setActive(boolean active) { this.active = active; }
    @StrutsParameter public void setInitialPassword(String initialPassword) { this.initialPassword = initialPassword; }
    @StrutsParameter public void setTypeIds(String[] typeIds) { this.typeIds = typeIds; }
    @StrutsParameter public void setTypeDays(String[] typeDays) { this.typeDays = typeDays; }
    @StrutsParameter public void setTemporaryPassword(String temporaryPassword) { this.temporaryPassword = temporaryPassword; }
}
