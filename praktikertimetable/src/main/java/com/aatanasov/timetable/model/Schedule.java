package com.aatanasov.timetable.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Full schedule.
 */
public class Schedule {

    private long numDays;

    private List<PersonMonthlySchedule> personStatuses = new ArrayList<>();

    public List<PersonMonthlySchedule> getPersonStatuses() {
        return personStatuses;
    }

    public void setPersonStatuses(List<PersonMonthlySchedule> personStatuses) {
        this.personStatuses = personStatuses;
    }

    public long getNumDays() {
        return numDays;
    }

    public void setNumDays(long numDays) {
        this.numDays = numDays;
    }

    public void addPersonStatus(PersonMonthlySchedule schedule) {
        personStatuses.add(schedule);
    }

    public PersonMonthlySchedule getPersonSchedule(String name) {
        for (PersonMonthlySchedule s : personStatuses) {
            if (name.equals(s.getName())) {
                return s;
            }
        }
        return null;
    }
}

