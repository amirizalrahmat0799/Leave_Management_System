package com.ahmadamirizal.leave.model;

public enum LeaveStatus {
    PENDING, APPROVED, REJECTED, CANCELLED;

    /** Pending and approved applications use up the employee's balance. */
    public boolean countsAgainstBalance() {
        return this == PENDING || this == APPROVED;
    }
}
