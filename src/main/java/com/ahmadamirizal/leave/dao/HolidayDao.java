package com.ahmadamirizal.leave.dao;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;

import com.ahmadamirizal.leave.model.PublicHoliday;

public class HolidayDao extends JdbcDao {

    public HolidayDao(DataSource dataSource) {
        super(dataSource);
    }

    public List<PublicHoliday> forYear(int year) {
        return query("SELECT holiday_date, name FROM public_holidays WHERE holiday_date BETWEEN ? AND ? ORDER BY holiday_date",
                rs -> new PublicHoliday(rs.getObject("holiday_date", LocalDate.class), rs.getString("name")),
                LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31));
    }

    public Set<LocalDate> between(LocalDate from, LocalDate to) {
        return new HashSet<>(query("SELECT holiday_date FROM public_holidays WHERE holiday_date BETWEEN ? AND ?",
                rs -> rs.getObject("holiday_date", LocalDate.class), from, to));
    }

    public void add(PublicHoliday h) {
        update("INSERT INTO public_holidays (holiday_date, name) VALUES (?, ?)", h.getDate(), h.getName());
    }

    public boolean exists(LocalDate date) {
        return !query("SELECT 1 FROM public_holidays WHERE holiday_date = ?", rs -> 1, date).isEmpty();
    }

    public void delete(LocalDate date) {
        update("DELETE FROM public_holidays WHERE holiday_date = ?", date);
    }
}
