package com.ahmadamirizal.leave.dao;

import java.math.BigDecimal;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import javax.sql.DataSource;

/** Approved leave per department and month, for the report page and its CSV export. */
public class ReportDao extends JdbcDao {

    public record Row(String department, int month, String leaveType, BigDecimal days, int applications) {
        // JavaBean-style getters for the JSP tags
        public String getDepartment() { return department; }
        public int getMonth() { return month; }
        public String getMonthName() { return Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH); }
        public String getLeaveType() { return leaveType; }
        public String getDaysText() { return days.stripTrailingZeros().toPlainString(); }
        public int getApplications() { return applications; }
    }

    public ReportDao(DataSource dataSource) {
        super(dataSource);
    }

    public List<Row> approvedByDepartmentAndMonth(int year) {
        return query("SELECT COALESCE(d.name, '(none)') AS department, MONTH(a.start_date) AS m, t.code AS type_code,"
                        + " SUM(a.days) AS total_days, COUNT(*) AS applications"
                        + " FROM leave_applications a"
                        + " JOIN users u ON u.id = a.user_id"
                        + " JOIN leave_types t ON t.id = a.leave_type_id"
                        + " LEFT JOIN departments d ON d.id = u.department_id"
                        + " WHERE a.status = 'APPROVED' AND YEAR(a.start_date) = ?"
                        + " GROUP BY COALESCE(d.name, '(none)'), MONTH(a.start_date), t.code"
                        + " ORDER BY department, m, type_code",
                rs -> new Row(rs.getString("department"), rs.getInt("m"), rs.getString("type_code"),
                        rs.getBigDecimal("total_days"), rs.getInt("applications")),
                year);
    }
}
