package com.ahmadamirizal.leave.model;

public enum Role {
    ADMIN, HR, EMPLOYEE;

    /** ADMIN and HR run the system: employees, settings, and every leave application. */
    public boolean isStaffManager() {
        return this == ADMIN || this == HR;
    }
}
