package com.ahmadamirizal.leave.dao;

/** Unchecked wrapper for SQLException, so actions don't need try/catch around every DAO call. */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
