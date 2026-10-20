package com.ahmadamirizal.leave.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

import com.ahmadamirizal.leave.model.Role;
import com.ahmadamirizal.leave.model.User;

public class UserDao extends JdbcDao {

    private static final String SELECT =
            "SELECT u.id, u.username, u.password_hash, u.must_change_password, u.role, u.employee_no, u.first_name,"
            + " u.last_name, u.email, u.department_id, d.name AS department_name, u.designation_id,"
            + " g.name AS designation_name, u.approver, u.active, u.last_login"
            + " FROM users u"
            + " LEFT JOIN departments d ON d.id = u.department_id"
            + " LEFT JOIN designations g ON g.id = u.designation_id";

    public UserDao(DataSource dataSource) {
        super(dataSource);
    }

    public Optional<User> findByUsername(String username) {
        return queryOne(SELECT + " WHERE u.username = ?", UserDao::map, username);
    }

    public Optional<User> find(int id) {
        return queryOne(SELECT + " WHERE u.id = ?", UserDao::map, id);
    }

    public List<User> all() {
        return query(SELECT + " ORDER BY u.active DESC, u.first_name, u.last_name", UserDao::map);
    }

    public List<User> search(String text) {
        String like = "%" + text.toLowerCase() + "%";
        return query(SELECT + " WHERE LOWER(u.first_name) LIKE ? OR LOWER(u.last_name) LIKE ? OR LOWER(u.username) LIKE ?"
                + " OR LOWER(u.employee_no) LIKE ? ORDER BY u.active DESC, u.first_name, u.last_name",
                UserDao::map, like, like, like, like);
    }

    public boolean usernameTaken(String username, Integer exceptId) {
        return !query("SELECT 1 FROM users WHERE username = ? AND id <> ?", rs -> 1,
                username, exceptId == null ? -1 : exceptId).isEmpty();
    }

    public int create(User u) {
        int id = insert("INSERT INTO users (username, password_hash, must_change_password, role, employee_no, first_name,"
                        + " last_name, email, department_id, designation_id, approver, active)"
                        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                u.getUsername(), u.getPasswordHash(), u.isMustChangePassword(), u.getRole(), u.getEmployeeNo(),
                u.getFirstName(), u.getLastName(), u.getEmail(), u.getDepartmentId(), u.getDesignationId(),
                u.isApprover(), u.isActive());
        u.setId(id);
        return id;
    }

    /** Updates profile fields; the password is changed separately. */
    public void update(User u) {
        update("UPDATE users SET username = ?, role = ?, employee_no = ?, first_name = ?, last_name = ?, email = ?,"
                        + " department_id = ?, designation_id = ?, approver = ?, active = ? WHERE id = ?",
                u.getUsername(), u.getRole(), u.getEmployeeNo(), u.getFirstName(), u.getLastName(), u.getEmail(),
                u.getDepartmentId(), u.getDesignationId(), u.isApprover(), u.isActive(), u.getId());
    }

    public void setPassword(int id, String hash, boolean mustChange) {
        update("UPDATE users SET password_hash = ?, must_change_password = ? WHERE id = ?", hash, mustChange, id);
    }

    public void recordLogin(int id, LocalDateTime when) {
        update("UPDATE users SET last_login = ? WHERE id = ?", Timestamp.valueOf(when), id);
    }

    private static User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setMustChangePassword(rs.getBoolean("must_change_password"));
        u.setRole(Role.valueOf(rs.getString("role")));
        u.setEmployeeNo(rs.getString("employee_no"));
        u.setFirstName(rs.getString("first_name"));
        u.setLastName(rs.getString("last_name"));
        u.setEmail(rs.getString("email"));
        u.setDepartmentId(getInteger(rs, "department_id"));
        u.setDepartmentName(rs.getString("department_name"));
        u.setDesignationId(getInteger(rs, "designation_id"));
        u.setDesignationName(rs.getString("designation_name"));
        u.setApprover(rs.getBoolean("approver"));
        u.setActive(rs.getBoolean("active"));
        Timestamp last = rs.getTimestamp("last_login");
        u.setLastLogin(last == null ? null : last.toLocalDateTime());
        return u;
    }
}
