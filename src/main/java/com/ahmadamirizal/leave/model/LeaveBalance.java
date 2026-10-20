package com.ahmadamirizal.leave.model;

import java.math.BigDecimal;

/** One leave type for one employee in one year: entitled, taken, waiting for approval, and what's left. */
public class LeaveBalance {
    private final LeaveType type;
    private final BigDecimal entitled;   // null = unlimited
    private final BigDecimal approved;
    private final BigDecimal pending;

    public LeaveBalance(LeaveType type, BigDecimal entitled, BigDecimal approved, BigDecimal pending) {
        this.type = type;
        this.entitled = entitled;
        this.approved = approved;
        this.pending = pending;
    }

    public LeaveType getType() { return type; }
    public BigDecimal getEntitled() { return entitled; }
    public BigDecimal getApproved() { return approved; }
    public BigDecimal getPending() { return pending; }
    public boolean isUnlimited() { return entitled == null; }

    /** Days still available to apply for: entitlement minus approved and pending. Null when unlimited. */
    public BigDecimal getAvailable() {
        return entitled == null ? null : entitled.subtract(approved).subtract(pending);
    }

    // ---- Display helpers for the JSPs ----
    private static String text(BigDecimal d) {
        return d == null ? "unlimited" : d.stripTrailingZeros().toPlainString();
    }

    public String getEntitledText() { return text(entitled); }
    public String getApprovedText() { return text(approved); }
    public String getPendingText() { return text(pending); }
    public String getAvailableText() { return text(getAvailable()); }
}
