package com.ahmadamirizal.leave.model;

import java.math.BigDecimal;

public class LeaveType {
    private Integer id;
    private String code;
    private String name;
    private String description;
    private BigDecimal defaultDays;   // null = unlimited
    private boolean active = true;

    public boolean isUnlimited() { return defaultDays == null; }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getDefaultDays() { return defaultDays; }
    public void setDefaultDays(BigDecimal defaultDays) { this.defaultDays = defaultDays; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
