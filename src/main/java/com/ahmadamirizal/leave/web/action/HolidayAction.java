package com.ahmadamirizal.leave.web.action;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.apache.struts2.interceptor.parameter.StrutsParameter;

import com.ahmadamirizal.leave.model.PublicHoliday;
import com.ahmadamirizal.leave.web.BaseAction;

public class HolidayAction extends BaseAction {

    private int year;
    private String date;
    private String name;

    @Override
    public String execute() {
        if (year == 0) year = services().leave.today().getYear();
        return SUCCESS;
    }

    public String add() {
        try {
            LocalDate d = LocalDate.parse(date == null ? "" : date.trim());
            if (name == null || name.isBlank()) {
                flash("Enter the holiday's name.");
            } else if (services().holidays.exists(d)) {
                flash("There's already a holiday on " + d + ".");
            } else {
                services().holidays.add(new PublicHoliday(d, name.trim()));
                flash("Added " + name.trim() + ". Leave applied for after this won't count that day.");
            }
            year = d.getYear();
        } catch (DateTimeParseException e) {
            flash("Choose a date.");
        }
        return SUCCESS;
    }

    public String delete() {
        try {
            LocalDate d = LocalDate.parse(date);
            services().holidays.delete(d);
            year = d.getYear();
            flash("Deleted.");
        } catch (DateTimeParseException | NullPointerException e) {
            flash("Choose a date.");
        }
        return SUCCESS;
    }

    public List<PublicHoliday> getHolidays() { return services().holidays.forYear(year); }
    public int getYear() { return year; }

    @StrutsParameter public void setYear(int year) { this.year = year; }
    @StrutsParameter public void setDate(String date) { this.date = date; }
    @StrutsParameter public void setName(String name) { this.name = name; }
}
