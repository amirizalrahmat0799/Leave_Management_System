package com.ahmadamirizal.leave.service;

/** A request that breaks a business rule. The message is shown to the user as-is. */
public class LeaveRuleException extends RuntimeException {
    public LeaveRuleException(String message) {
        super(message);
    }
}
