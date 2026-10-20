package com.ahmadamirizal.leave.web.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.parameter.StrutsParameter;

import com.ahmadamirizal.leave.dao.ReportDao;
import com.ahmadamirizal.leave.web.BaseAction;

/** Approved leave per department, month and type, as a table or a CSV download. */
public class ReportAction extends BaseAction {

    private int year;
    private List<ReportDao.Row> rows;

    @Override
    public String execute() {
        load();
        return SUCCESS;
    }

    public String csv() throws IOException {
        load();
        HttpServletResponse response = ServletActionContext.getResponse();
        response.setContentType("text/csv");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Content-Disposition", "attachment; filename=\"leave-report-" + year + ".csv\"");
        PrintWriter out = response.getWriter();
        out.println("department,month,leave_type,days,applications");
        for (ReportDao.Row r : rows) {
            out.println(csv(r.department()) + "," + r.month() + "," + csv(r.leaveType()) + ","
                    + r.days().stripTrailingZeros().toPlainString() + "," + r.applications());
        }
        out.flush();
        return NONE;
    }

    /** Quotes a CSV field, and stops spreadsheet formula injection (a value starting with = + - @). */
    static String csv(String value) {
        String v = value == null ? "" : value;
        if (!v.isEmpty() && "=+-@".indexOf(v.charAt(0)) >= 0) v = "'" + v;
        return "\"" + v.replace("\"", "\"\"") + "\"";
    }

    private void load() {
        if (year == 0) year = services().leave.today().getYear();
        rows = services().reports.approvedByDepartmentAndMonth(year);
    }

    public int getYear() { return year; }
    public List<ReportDao.Row> getRows() { return rows; }

    @StrutsParameter public void setYear(int year) { this.year = year; }
}
