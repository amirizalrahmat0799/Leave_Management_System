package com.ahmadamirizal.leave.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class PublicHoliday {
    private LocalDate date;
    private String name;

    public PublicHoliday() {}

    public PublicHoliday(LocalDate date, String name) {
        this.date = date;
        this.name = name;
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    /** "Monday, 31 Aug 2026" */
    public String getDateText() {
        return date == null ? "" : date.format(DateTimeFormatter.ofPattern("EEEE, d MMM yyyy", Locale.ENGLISH));
    }
}
