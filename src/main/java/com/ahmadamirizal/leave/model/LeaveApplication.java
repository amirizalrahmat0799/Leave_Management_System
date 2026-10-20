package com.ahmadamirizal.leave.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class LeaveApplication {
    private Integer id;
    private Integer userId;
    private String employeeName;
    private Integer departmentId;
    private String departmentName;
    private Integer leaveTypeId;
    private String leaveTypeCode;
    private String leaveTypeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean halfDay;
    private BigDecimal days;
    private String reason;
    private LeaveStatus status = LeaveStatus.PENDING;
    private Integer decidedBy;
    private String decidedByName;
    private LocalDateTime decidedAt;
    private String decisionNote;
    private LocalDateTime createdAt;
    private boolean actionable;   // set by the page: can the viewer cancel / decide on it?

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public Integer getDepartmentId() { return departmentId; }
    public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public Integer getLeaveTypeId() { return leaveTypeId; }
    public void setLeaveTypeId(Integer leaveTypeId) { this.leaveTypeId = leaveTypeId; }
    public String getLeaveTypeCode() { return leaveTypeCode; }
    public void setLeaveTypeCode(String leaveTypeCode) { this.leaveTypeCode = leaveTypeCode; }
    public String getLeaveTypeName() { return leaveTypeName; }
    public void setLeaveTypeName(String leaveTypeName) { this.leaveTypeName = leaveTypeName; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public boolean isHalfDay() { return halfDay; }
    public void setHalfDay(boolean halfDay) { this.halfDay = halfDay; }
    public BigDecimal getDays() { return days; }
    public void setDays(BigDecimal days) { this.days = days; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LeaveStatus getStatus() { return status; }
    public void setStatus(LeaveStatus status) { this.status = status; }
    public Integer getDecidedBy() { return decidedBy; }
    public void setDecidedBy(Integer decidedBy) { this.decidedBy = decidedBy; }
    public String getDecidedByName() { return decidedByName; }
    public void setDecidedByName(String decidedByName) { this.decidedByName = decidedByName; }
    public LocalDateTime getDecidedAt() { return decidedAt; }
    public void setDecidedAt(LocalDateTime decidedAt) { this.decidedAt = decidedAt; }
    public String getDecisionNote() { return decisionNote; }
    public void setDecisionNote(String decisionNote) { this.decisionNote = decisionNote; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public boolean isActionable() { return actionable; }
    public void setActionable(boolean actionable) { this.actionable = actionable; }

    // ---- Display helpers for the JSPs ----
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a", Locale.ENGLISH);

    /** "2 Nov 2026 (half day)", "5 Oct 2026" or "14 Sep 2026 to 18 Sep 2026". */
    public String getPeriodText() {
        if (startDate == null) return "";
        if (halfDay) return startDate.format(DAY) + " (half day)";
        if (endDate == null || endDate.equals(startDate)) return startDate.format(DAY);
        return startDate.format(DAY) + " to " + endDate.format(DAY);
    }

    public int getYear() {
        return startDate == null ? 0 : startDate.getYear();
    }

    public String getDaysText() {
        return days == null ? "" : days.stripTrailingZeros().toPlainString();
    }

    public String getDecidedAtText() {
        return decidedAt == null ? "" : decidedAt.format(STAMP);
    }

    public String getCreatedAtText() {
        return createdAt == null ? "" : createdAt.format(STAMP);
    }

    public String getStatusLabel() {
        return status.name().charAt(0) + status.name().substring(1).toLowerCase();
    }
}
