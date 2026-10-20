package com.ahmadamirizal.leave.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

import org.mindrot.jbcrypt.BCrypt;

import com.ahmadamirizal.leave.dao.UserDao;
import com.ahmadamirizal.leave.model.User;

public class AuthService {

    public static final int MIN_PASSWORD_LENGTH = 8;
    private static final int BCRYPT_COST = 12;
    // Checked against when the username doesn't exist, so a wrong username takes as long as a wrong password.
    private static final String DUMMY_HASH = BCrypt.hashpw("not-a-real-password", BCrypt.gensalt(BCRYPT_COST));

    private final UserDao users;
    private final Clock clock;

    public AuthService(UserDao users, Clock clock) {
        this.users = users;
        this.clock = clock;
    }

    public static String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_COST));
    }

    /** Returns the user if the username and password match an active account. */
    public Optional<User> login(String username, String password) {
        if (username == null || password == null) return Optional.empty();
        Optional<User> found = users.findByUsername(username.trim());
        String hash = found.map(User::getPasswordHash).orElse(DUMMY_HASH);
        boolean ok = BCrypt.checkpw(password, hash);
        if (!ok || found.isEmpty() || !found.get().isActive()) return Optional.empty();
        users.recordLogin(found.get().getId(), LocalDateTime.now(clock));
        return found;
    }

    public void changePassword(User user, String current, String replacement, String confirmation) {
        User fresh = users.find(user.getId()).orElseThrow(() -> new LeaveRuleException("Your account no longer exists."));
        if (current == null || !BCrypt.checkpw(current, fresh.getPasswordHash())) {
            throw new LeaveRuleException("Your current password is not correct.");
        }
        validateNewPassword(replacement, confirmation);
        if (BCrypt.checkpw(replacement, fresh.getPasswordHash())) {
            throw new LeaveRuleException("Choose a password different from your current one.");
        }
        users.setPassword(user.getId(), hash(replacement), false);
        user.setMustChangePassword(false);
    }

    /** HR or an admin sets a temporary password; the employee must change it when they next sign in. */
    public void resetPassword(int userId, String temporary) {
        validateNewPassword(temporary, temporary);
        users.setPassword(userId, hash(temporary), true);
    }

    public static void validateNewPassword(String password, String confirmation) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new LeaveRuleException("Passwords must be at least " + MIN_PASSWORD_LENGTH + " characters.");
        }
        if (!password.equals(confirmation)) {
            throw new LeaveRuleException("The two passwords don't match.");
        }
    }
}
