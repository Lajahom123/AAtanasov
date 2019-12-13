package com.aatanasov.timetable.test;

import com.aatanasov.timetable.app.ScheduleCalculator;
import com.aatanasov.timetable.model.DayType;
import com.aatanasov.timetable.model.PersonMonthlySchedule;
import com.aatanasov.timetable.model.Schedule;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Created by aatanasov on 4/23/17.
 */
public class ScheduleCalculatorTest {

    @Test
    public void testDaysNotChanged() {
        Schedule s = new Schedule();
        s.setNumDays(30);
        assertEquals(s.getNumDays(), ScheduleCalculator.generateSchedule(s).getNumDays());
    }

    @Test
    public void testNameNotChanged() {
        Schedule s = new Schedule();
        s.setNumDays(30);
        PersonMonthlySchedule ps = new PersonMonthlySchedule("Иван");
        s.getPersonStatuses().add(ps);
        assertEquals(ps.getName(), ScheduleCalculator.generateSchedule(s).getPersonStatuses().get(0).getName());
    }

    @Test
    public void testSamePeople() {
        Schedule s = new Schedule();
        s.setNumDays(30);
        PersonMonthlySchedule ps = new PersonMonthlySchedule("Иван");
        s.getPersonStatuses().add(ps);
        PersonMonthlySchedule ps2 = new PersonMonthlySchedule("Добри");
        s.getPersonStatuses().add(ps2);
        Schedule updated = ScheduleCalculator.generateSchedule(s);
        assertEquals(2, updated.getPersonStatuses().size());
        assertEquals(ps.getName(), updated.getPersonStatuses().get(0).getName());
        assertEquals(ps2.getName(), updated.getPersonStatuses().get(1).getName());
    }

    @Test
    public void testSamePeopleData() {
        Schedule s = new Schedule();
        s.setNumDays(30);
        PersonMonthlySchedule ps = new PersonMonthlySchedule("Иван");
        for (long i=5; i<20; i++) {
            ps.add(i, ScheduleCalculator.generateRandomDayType());
        }
        s.getPersonStatuses().add(ps);
        Schedule updatedSchedule = ScheduleCalculator.generateSchedule(s);
        List<PersonMonthlySchedule> data = updatedSchedule.getPersonStatuses();
        assertEquals(1, data.size());
        PersonMonthlySchedule updatedPs = data.get(0);
        for (Long day : ps.getSchedule().keySet()) {
            DayType expected = ps.get(day);
            DayType actual = updatedPs.get(day);
            assertEquals(expected, actual);
        }
    }

    @Test
    public void testAllDaysPopulated() {
        Schedule s = new Schedule();
        s.setNumDays(30);
        s.addPersonStatus(new PersonMonthlySchedule("Dani"));
        Schedule updated = ScheduleCalculator.generateSchedule(s);
        assertEquals(1, updated.getPersonStatuses().size());
        PersonMonthlySchedule updatedPs = updated.getPersonStatuses().get(0);
        assertEquals(s.getNumDays(), updatedPs.getSchedule().size());
        for (Long day : updatedPs.getSchedule().keySet()) {
            assertNotNull(updatedPs.getSchedule().get(day));
        }
    }

    @Test
    public void testNoFirstShiftAfterSecondShift() {
        Schedule s = new Schedule();
        s.setNumDays(30);
        s.addPersonStatus(new PersonMonthlySchedule("Milko"));
        Schedule updated = ScheduleCalculator.generateSchedule(s);
        Map<Long, DayType> tested = updated.getPersonStatuses().get(0).getSchedule();
        assertEquals(30, tested.size());
        DayType prevDayType =  null;
        for (int i=0; i<30; i++) {
            DayType currDayType = tested.get(i);
            if (DayType.FIRST_SHIFT.equals(currDayType) && DayType.SECOND_SHIFT.equals(prevDayType)){
                fail("Second shift after first shift detected");
            }
            prevDayType = currDayType;
        }
    }

    @Test
    public void testNumPeopleEachDay() {
        // prepare some people
        Schedule s = new Schedule();
        s.setNumDays(30);
        for (int i=0; i<13; i++) {
            s.addPersonStatus(new PersonMonthlySchedule("person" + i));
        }

        // generate schedule
        Schedule updated = ScheduleCalculator.generateSchedule(s);

        // verify the number working people each day
        for (int i=1; i<=updated.getNumDays(); i++) {
            // count num people working
            int numPeopleWorking = ScheduleCalculator.getNumPeopleWorkingAt(updated, i);

            assertTrue("Too many people: " + numPeopleWorking,numPeopleWorking <= ScheduleCalculator.MAX_PEOPLE_WORKING);
            assertTrue("Too few people: " + numPeopleWorking, numPeopleWorking >= ScheduleCalculator.MIN_PEOPLE_WORKING);
        }
        //unusual problems with the test with the last commit
    }

    @Test
    public void testPersonDoesntWorkMoreThanSixDays() {
        Schedule s = new Schedule();
        s.setNumDays(30);
        s.addPersonStatus(new PersonMonthlySchedule("Mitko"));
        Schedule updated = ScheduleCalculator.generateSchedule(s);
        Map<Long, DayType> tested = updated.getPersonStatuses().get(0).getSchedule();
        DayType Day2 = null;
        DayType Day3 = null;
        DayType Day4 = null;
        DayType Day5 = null;
        DayType Day6 = null;
        DayType Day7 = null;
        for(long i=1;i<30;i++) {
            DayType Day1 = tested.get(i);
            if(i>7) {
                if ((!DayType.REST.equals(Day1)) && (!DayType.REST.equals(Day2)) && (!DayType.REST.equals(Day3)) && (!DayType.REST.equals(Day4)) && (!DayType.REST.equals(Day5)) && (!DayType.REST.equals(Day6)) && (!DayType.REST.equals(Day7))) {
                    fail("Each person can work maximum 6 days in a row");
                }
            }
            Day7 = Day6;
            Day6 = Day5;
            Day5 = Day4;
            Day4 = Day3;
            Day3 = Day2;
            Day2 = Day1;
        }
    }


    @Test
    public void testPersonRestDays() {

        // prepare
        Schedule s = new Schedule();
        s.setNumDays(30);
        s.addPersonStatus(new PersonMonthlySchedule("Ivan"));
        s.getPersonStatuses().get(0).getSchedule().put(2l, DayType.SECOND_SHIFT);
        s.getPersonStatuses().get(0).getSchedule().put(3l, DayType.FIRST_SHIFT);

        // calculate
        Schedule updated = ScheduleCalculator.generateSchedule(s);

        // verify
        for (PersonMonthlySchedule tested : updated.getPersonStatuses()) {
            int numRestDays = 0;
            for (Long day : tested.getSchedule().keySet()) {
                DayType currDayType = tested.get(day);
                if (DayType.REST.equals(currDayType)) {
                    numRestDays++;
                }
            }
            boolean condition = (ScheduleCalculator.NUM_REST_DAYS_PER_MONTH == numRestDays) || (ScheduleCalculator.NUM_REST_DAYS_PER_MONTH+1 == numRestDays);
            assertTrue("Test each person can rest 9 or 10 days during the month, currently " + numRestDays, condition);
        }
    }

    @Test
    public void testGenerateRandomDayType() {
        Set<DayType> typesGenerated = new HashSet<>();

        // generate some random day types
        ScheduleCalculator calculator = new ScheduleCalculator();
        for (int i=0; i<30; i++) {
            typesGenerated.add(calculator.generateRandomDayType());
        }

        // verify all possible types generated
        for (DayType dayType : ScheduleCalculator.RANDOM_GENERATED_DAY_TYPES) {
            if (!typesGenerated.contains(dayType)) {
                fail("DayType never generated :" + dayType);
            }
        }
    }

    @Test
    public void testCalculateWorkingPeopleCount() {
        // prepare
        Schedule s = new Schedule();
        s.setNumDays(30);
        s.addPersonStatus(new PersonMonthlySchedule());
        s.addPersonStatus(new PersonMonthlySchedule());
        s.addPersonStatus(new PersonMonthlySchedule());
        s.getPersonStatuses().get(0).getSchedule().put(1l, DayType.SECOND_SHIFT);
        s.getPersonStatuses().get(0).getSchedule().put(2l, DayType.FIRST_SHIFT);
        s.getPersonStatuses().get(1).getSchedule().put(2l, DayType.FIRST_SHIFT);
        s.getPersonStatuses().get(2).getSchedule().put(1l, DayType.REST);
        s.getPersonStatuses().get(2).getSchedule().put(3l, DayType.REST);

        assertEquals(1, ScheduleCalculator.getNumPeopleWorkingAt(s, 1));
        assertEquals(2, ScheduleCalculator.getNumPeopleWorkingAt(s, 2));
        assertEquals(0, ScheduleCalculator.getNumPeopleWorkingAt(s, 3));
    }
}

