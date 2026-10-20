package com.ahmadamirizal.leave.dao;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;

import com.ahmadamirizal.leave.model.LeaveType;

public class LeaveTypeDao extends JdbcDao {

    private static final String COLUMNS = "id, code, name, description, default_days, active";

    public LeaveTypeDao(DataSource dataSource) {
        super(dataSource);
    }

    public List<LeaveType> all() {
        return query("SELECT " + COLUMNS + " FROM leave_types ORDER BY code", LeaveTypeDao::map);
    }

    public List<LeaveType> active() {
        return query("SELECT " + COLUMNS + " FROM leave_types WHERE active = TRUE ORDER BY code", LeaveTypeDao::map);
    }

    public Optional<LeaveType> find(int id) {
        return queryOne("SELECT " + COLUMNS + " FROM leave_types WHERE id = ?", LeaveTypeDao::map, id);
    }

    public void save(LeaveType t) {
        if (t.getId() == null) {
            t.setId(insert("INSERT INTO leave_types (code, name, description, default_days, active) VALUES (?, ?, ?, ?, ?)",
                    t.getCode(), t.getName(), t.getDescription(), t.getDefaultDays(), t.isActive()));
        } else {
            update("UPDATE leave_types SET code = ?, name = ?, description = ?, default_days = ?, active = ? WHERE id = ?",
                    t.getCode(), t.getName(), t.getDescription(), t.getDefaultDays(), t.isActive(), t.getId());
        }
    }

    /** Per-employee overrides of the default days, keyed by leave type id. */
    public Map<Integer, BigDecimal> entitlementOverrides(int userId) {
        Map<Integer, BigDecimal> out = new LinkedHashMap<>();
        query("SELECT leave_type_id, days FROM leave_entitlements WHERE user_id = ?",
                rs -> out.put(rs.getInt("leave_type_id"), rs.getBigDecimal("days")), userId);
        return out;
    }

    /** Sets an employee's entitlement for one type; null removes the override (back to the default). */
    public void setEntitlement(int userId, int leaveTypeId, BigDecimal days) {
        update("DELETE FROM leave_entitlements WHERE user_id = ? AND leave_type_id = ?", userId, leaveTypeId);
        if (days != null) {
            update("INSERT INTO leave_entitlements (user_id, leave_type_id, days) VALUES (?, ?, ?)", userId, leaveTypeId, days);
        }
    }

    private static LeaveType map(ResultSet rs) throws SQLException {
        LeaveType t = new LeaveType();
        t.setId(rs.getInt("id"));
        t.setCode(rs.getString("code"));
        t.setName(rs.getString("name"));
        t.setDescription(rs.getString("description"));
        t.setDefaultDays(rs.getBigDecimal("default_days"));
        t.setActive(rs.getBoolean("active"));
        return t;
    }
}
