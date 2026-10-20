package com.ahmadamirizal.leave.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.Test;

class WorkingDaysTest {

    private static final LocalDate MALAYSIA_DAY = LocalDate.of(2026, 9, 16);   // a Wednesday

    @Test
    void countsMondayToFriday() {
        assertEquals(5, WorkingDays.count(LocalDate.of(2026, 10, 19), LocalDate.of(2026, 10, 25), Set.of()));
    }

    @Test
    void skipsPublicHolidays() {
        assertEquals(4, WorkingDays.count(LocalDate.of(2026, 9, 14), LocalDate.of(2026, 9, 18), Set.of(MALAYSIA_DAY)));
        assertFalse(WorkingDays.isWorkingDay(MALAYSIA_DAY, Set.of(MALAYSIA_DAY)));
    }

    @Test
    void weekendOnlyRangeIsZero() {
        assertEquals(0, WorkingDays.count(LocalDate.of(2026, 10, 24), LocalDate.of(2026, 10, 25), Set.of()));
        assertTrue(WorkingDays.isWorkingDay(LocalDate.of(2026, 10, 23), Set.of()));
    }
}
