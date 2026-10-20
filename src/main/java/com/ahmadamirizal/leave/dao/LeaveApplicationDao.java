package com.ahmadamirizal.leave.dao;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

import com.ahmadamirizal.leave.model.LeaveApplication;
import com.ahmadamirizal.leave.model.LeaveStatus;

public class LeaveApplicationDao extends JdbcDao {

    private static final String SELECT =
            "SELECT a.id, a.user_id, u.first_name, u.last_name, u.department_id, d.name AS department_name,"
            + " a.leave_type_id, t.code AS type_code, t.name AS type_name, a.start_date, a.end_date, a.half_day, a.days,"
            + " a.reason, a.status, a.decided_by, x.first_name AS decider_first, x.last_name AS decider_last,"
            + " a.decided_at, a.decision_note, a.created_at"
            + " FROM leave_applications a"
            + " JOIN users u ON u.id = a.user_id"
            + " JOIN leave_types t ON t.id = a.leave_type_id"
            + " LEFT JOIN departments d ON d.id = u.department_id"
            + " LEFT JOIN users x ON x.id = a.decided_by";

    public LeaveApplicationDao(DataSource dataSource) {
        super(dataSource);
    }

    public Optional<LeaveApplication> find(int id) {
        return queryOne(SELECT + " WHERE a.id = ?", LeaveApplicationDao::map, id);
    }

    public List<LeaveApplication> forUser(int userId) {
        return query(SELECT + " WHERE a.user_id = ? ORDER BY a.start_date DESC, a.id DESC", LeaveApplicationDao::map, userId);
    }

    /**
     * Applications visible to a reviewer. {@code departmentId} null means all departments (HR and admin);
     * {@code status} null means any status.
     */
    public List<LeaveApplication> forReview(Integer departmentId, LeaveStatus status) {
        StringBuilder sql = new StringBuilder(SELECT).append(" WHERE 1 = 1");
        List<Object> params = new ArrayList<>();
        if (departmentId != null) {
            sql.append(" AND u.department_id = ?");
            params.add(departmentId);
        }
        if (status != null) {
            sql.append(" AND a.status = ?");
            params.add(status);
        }
        sql.append(" ORDER BY CASE WHEN a.status = 'PENDING' THEN 0 ELSE 1 END, a.start_date DESC, a.id DESC");
        return query(sql.toString(), LeaveApplicationDao::map, params.toArray());
    }

    public int countByStatus(LeaveStatus status, Integer departmentId) {
        String sql = "SELECT COUNT(*) FROM leave_applications a JOIN users u ON u.id = a.user_id WHERE a.status = ?"
                + (departmentId == null ? "" : " AND u.department_id = ?");
        Object[] params = departmentId == null ? new Object[] {status} : new Object[] {status, departmentId};
        return queryOne(sql, rs -> rs.getInt(1), params).orElse(0);
    }

    /** People on approved leave on a given day. */
    public List<LeaveApplication> onLeave(LocalDate day) {
        return query(SELECT + " WHERE a.status = 'APPROVED' AND ? BETWEEN a.start_date AND a.end_date ORDER BY u.first_name",
                LeaveApplicationDao::map, day);
    }

    /** Total days of approved, or pending, leave of one type in a date range. */
    public BigDecimal sumDays(int userId, int leaveTypeId, LeaveStatus status, LocalDate from, LocalDate to) {
        return queryOne("SELECT COALESCE(SUM(days), 0) FROM leave_applications"
                        + " WHERE user_id = ? AND leave_type_id = ? AND status = ? AND start_date BETWEEN ? AND ?",
                rs -> rs.getBigDecimal(1), userId, leaveTypeId, status, from, to).orElse(BigDecimal.ZERO);
    }

    /** True if the employee already has pending or approved leave overlapping these dates. */
    public boolean overlaps(int userId, LocalDate start, LocalDate end) {
        return !query("SELECT 1 FROM leave_applications WHERE user_id = ? AND status IN ('PENDING', 'APPROVED')"
                        + " AND start_date <= ? AND end_date >= ?",
                rs -> 1, userId, end, start).isEmpty();
    }

    public int create(LeaveApplication a) {
        int id = insert("INSERT INTO leave_applications (user_id, leave_type_id, start_date, end_date, half_day, days,"
                        + " reason, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                a.getUserId(), a.getLeaveTypeId(), a.getStartDate(), a.getEndDate(), a.isHalfDay(), a.getDays(),
                a.getReason(), a.getStatus());
        a.setId(id);
        return id;
    }

    /**
     * Moves an application from one status to another. The {@code AND status = ?} guard makes this safe when two
     * people act on the same application at once: only the first change wins. Returns false if it had already moved.
     */
    public boolean transition(int id, LeaveStatus from, LeaveStatus to, Integer decidedBy, String note) {
        return update("UPDATE leave_applications SET status = ?, decided_by = ?, decided_at = ?, decision_note = ?"
                        + " WHERE id = ? AND status = ?",
                to, decidedBy, Timestamp.valueOf(LocalDateTime.now()), note, id, from) == 1;
    }

    private static LeaveApplication map(ResultSet rs) throws SQLException {
        LeaveApplication a = new LeaveApplication();
        a.setId(rs.getInt("id"));
        a.setUserId(rs.getInt("user_id"));
        a.setEmployeeName(rs.getString("first_name") + " " + rs.getString("last_name"));
        a.setDepartmentId(getInteger(rs, "department_id"));
        a.setDepartmentName(rs.getString("department_name"));
        a.setLeaveTypeId(rs.getInt("leave_type_id"));
        a.setLeaveTypeCode(rs.getString("type_code"));
        a.setLeaveTypeName(rs.getString("type_name"));
        a.setStartDate(rs.getObject("start_date", LocalDate.class));
        a.setEndDate(rs.getObject("end_date", LocalDate.class));
        a.setHalfDay(rs.getBoolean("half_day"));
        a.setDays(rs.getBigDecimal("days"));
        a.setReason(rs.getString("reason"));
        a.setStatus(LeaveStatus.valueOf(rs.getString("status")));
        a.setDecidedBy(getInteger(rs, "decided_by"));
        String df = rs.getString("decider_first");
        a.setDecidedByName(df == null ? null : df + " " + rs.getString("decider_last"));
        Timestamp decided = rs.getTimestamp("decided_at");
        a.setDecidedAt(decided == null ? null : decided.toLocalDateTime());
        a.setDecisionNote(rs.getString("decision_note"));
        Timestamp created = rs.getTimestamp("created_at");
        a.setCreatedAt(created == null ? null : created.toLocalDateTime());
        return a;
    }
}
