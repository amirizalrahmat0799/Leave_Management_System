package com.ahmadamirizal.leave.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/** Counts working days: Monday to Friday, skipping public holidays. */
public final class WorkingDays {

    private WorkingDays() {}

    public static boolean isWorkingDay(LocalDate day, Set<LocalDate> holidays) {
        DayOfWeek dow = day.getDayOfWeek();
        return dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY && !holidays.contains(day);
    }

    public static int count(LocalDate start, LocalDate end, Set<LocalDate> holidays) {
        int n = 0;
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            if (isWorkingDay(d, holidays)) n++;
        }
        return n;
    }
}
