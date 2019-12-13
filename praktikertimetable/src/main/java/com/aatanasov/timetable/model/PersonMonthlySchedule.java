package com.aatanasov.timetable.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Schedule for one person.
 */
public class PersonMonthlySchedule {

    public long consequentWorkingDays=0;
    private String name;
    private Map<Long, DayType> schedule = new HashMap<>();

    public PersonMonthlySchedule() {
    }

    public PersonMonthlySchedule(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<Long, DayType> getSchedule() {
        return schedule;
    }

    public void setSchedule(Map<Long, DayType> schedule) {
        this.schedule = schedule;
    }

    public void add(Number day, DayType type) {
        schedule.put(day.longValue(), type);
    }

    public DayType get(Number day) {
        return schedule.get(day.longValue());
    }

}
