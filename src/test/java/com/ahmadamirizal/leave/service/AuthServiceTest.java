package com.ahmadamirizal.leave.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ahmadamirizal.leave.TestDatabase;
import com.ahmadamirizal.leave.model.User;

class AuthServiceTest {

    private Services s;

    @BeforeEach
    void setUp() {
        s = TestDatabase.services();
    }

    @Test
    void logsInWithTheRightPasswordOnly() {
        User admin = s.auth.login("admin", "admin123").orElseThrow();
        assertTrue(admin.isMustChangePassword());
        assertNotNull(s.users.find(admin.getId()).orElseThrow().getLastLogin());

        assertTrue(s.auth.login("admin", "wrong").isEmpty());
        assertTrue(s.auth.login("nobody", "admin123").isEmpty());
    }

    @Test
    void v1SqlInjectionNoLongerWorks() {
        // v1 built the login query from the raw form fields, so this username logged in as admin.
        assertTrue(s.auth.login("admin' -- ", "anything").isEmpty());
        assertTrue(s.auth.login("' OR '1'='1", "' OR '1'='1").isEmpty());
    }

    @Test
    void inactiveAccountsCannotLogIn() {
        User priya = s.users.findByUsername("priya").orElseThrow();
        priya.setActive(false);
        s.users.update(priya);
        assertTrue(s.auth.login("priya", "demo1234").isEmpty());
    }

    @Test
    void changingAndResettingPasswords() {
        User admin = s.auth.login("admin", "admin123").orElseThrow();
        assertThrows(LeaveRuleException.class, () -> s.auth.changePassword(admin, "wrong", "new-password-1", "new-password-1"));
        assertThrows(LeaveRuleException.class, () -> s.auth.changePassword(admin, "admin123", "short", "short"));
        assertThrows(LeaveRuleException.class, () -> s.auth.changePassword(admin, "admin123", "new-password-1", "different"));

        s.auth.changePassword(admin, "admin123", "new-password-1", "new-password-1");
        assertFalse(admin.isMustChangePassword());
        assertTrue(s.auth.login("admin", "admin123").isEmpty());
        assertFalse(s.auth.login("admin", "new-password-1").orElseThrow().isMustChangePassword());

        User priya = s.users.findByUsername("priya").orElseThrow();
        s.auth.resetPassword(priya.getId(), "temporary-1");
        assertTrue(s.auth.login("priya", "temporary-1").orElseThrow().isMustChangePassword());
    }

    @Test
    void storesBcryptHashesNotMd5() {
        String hash = s.users.findByUsername("admin").orElseThrow().getPasswordHash();
        assertTrue(hash.startsWith("$2a$"), hash);
    }
}
