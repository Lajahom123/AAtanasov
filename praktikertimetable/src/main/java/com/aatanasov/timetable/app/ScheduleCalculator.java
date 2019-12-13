package com.aatanasov.timetable.app;

import com.aatanasov.timetable.model.DayType;
import com.aatanasov.timetable.model.PersonMonthlySchedule;
import com.aatanasov.timetable.model.Schedule;

import java.time.Month;
import java.util.Random;

/**
 * Contains the main schedule logic.
 */
public class ScheduleCalculator {

    public static final int NUM_REST_DAYS_PER_MONTH = 9;
    public static final int NUM_WORKING_DAYS_PER_MONTH = 21;
    public static final int MAX_CONSEQUENT_WORKING_DAYS = 6;
    public static final int MIN_PEOPLE_WORKING = 8;
    public static final int MAX_PEOPLE_WORKING = 10;
    public static final int MAX_RETRIES = 200;

    public static DayType[] RANDOM_GENERATED_DAY_TYPES = new DayType[]{DayType.NORMAL_SHIFT, DayType.FIRST_SHIFT, DayType.SECOND_SHIFT, DayType.REST};
    //public static DayType[] RANDOM_WORKING_DAY_TYPES = new DayType[]{DayType.NORMAL_SHIFT, DayType.SECOND_SHIFT};

    // main logic for creating schedule
    public static Schedule generateSchedule(Schedule templateArg) {

        // duplicate the template to preserve the initial state
        Schedule generated = new Schedule();
        generated.setNumDays(templateArg.getNumDays());
        for (PersonMonthlySchedule person : templateArg.getPersonStatuses()) {
            PersonMonthlySchedule copy = new PersonMonthlySchedule(person.getName());
            for (long day : person.getSchedule().keySet()) {
                copy.getSchedule().put(day, person.getSchedule().get(day));
            }
            generated.getPersonStatuses().add(copy);
        }

        long numDays = generated.getNumDays();
        System.out.println("Calculate time schedule for num days = " + numDays);



        // each person
        for (PersonMonthlySchedule personSchedule : generated.getPersonStatuses()) {
            System.out.println("Processing person " + personSchedule.getName());

            // try several times if needed to generate schedule for the person
            int retries = 0;
            while (retries < MAX_RETRIES) {
                try {
                    // calculate schedule satisfying all restrictions
                    PersonMonthlySchedule schedule = generatePersonSchedule(generated, personSchedule);

                    generated.getPersonSchedule(personSchedule.getName()).setSchedule(schedule.getSchedule());
                    break;
                } catch (Exception e) {
                    System.out.println("Retry person schedule generation :" + personSchedule.getName());
                    retries++;
                    // reset prev iteration result
                    generated.getPersonSchedule(personSchedule.getName()).setSchedule(personSchedule.getSchedule());
                }
            }
        }
        return generated;
    }



              /*TODO
              * 1. Get the consequentWorkingDays of each worker, compare them and write the biggest in an array
              * 1.1 Principle of comparison - when comparing check the following day as well (without it, it should also work)
              * 2. Get the people on the array and generate rest days for them and working days for the others
              * 3. Put all the results into the schedule and continue with the next day*/


    private static PersonMonthlySchedule generatePersonSchedule(Schedule templateArg, PersonMonthlySchedule initialSchedule) {
        int consequentWorkingDays = 0;
        int totalWorkingDays = getNumWorkingDays(initialSchedule);
        int totalRestDays = getNumRestDays(initialSchedule);
        long numDays = templateArg.getNumDays();
        PersonMonthlySchedule generated = new PersonMonthlySchedule(initialSchedule.getName());
        for (long day = 1; day <= numDays; day++) {

            DayType existingDay = initialSchedule.getSchedule().get(day);
            DayType calculatedDayType = null;

            // process only days not declared upfront
            if (existingDay == null) {

                // count others working at that day
                int otherPeopleWorking = getNumPeopleWorkingAt(templateArg, day);

                // suggest days until condition is met
                boolean allConditionsMet = false;
                int numRetries = 0;
                DayType prevIterationDayTypeGuess = null;
                while (!allConditionsMet && numRetries < MAX_RETRIES) {
                    numRetries++;


                    if (prevIterationDayTypeGuess != null) {
                        // try to use guess from the previous iteration
                        calculatedDayType = prevIterationDayTypeGuess;
                        prevIterationDayTypeGuess = null;
                    } else {
                        // generate some random day type
                        calculatedDayType = generateRandomDayType();
                    }

                    // can't have second after first shift
                    DayType prevDayType = initialSchedule.getSchedule().get(day - 1);
                    if (DayType.SECOND_SHIFT.equals(prevDayType)
                            && DayType.FIRST_SHIFT.equals(calculatedDayType)) {
                        prevIterationDayTypeGuess = DayType.NORMAL_SHIFT;
                        continue;
                    }

                    // too many people at work
                    if (DayType.isWorkingDay(calculatedDayType)
                            && otherPeopleWorking == MAX_PEOPLE_WORKING) {
                        prevIterationDayTypeGuess = DayType.REST;
                        continue;
                    }

                    // too many rest days
                    if (DayType.REST.equals(calculatedDayType)
                            && totalRestDays >= NUM_REST_DAYS_PER_MONTH) {
                        prevIterationDayTypeGuess = DayType.NORMAL_SHIFT;
                        continue;
                    }

                    // too many working days
                    if (DayType.isWorkingDay(calculatedDayType)
                            && consequentWorkingDays > MAX_CONSEQUENT_WORKING_DAYS) {
                        prevIterationDayTypeGuess = DayType.REST;
                        continue;
                    }

                    allConditionsMet = true;
                }

                if (numRetries == MAX_RETRIES) {
                    System.out.println("Too many retries");
                    throw new RuntimeException("Failed to satisfy all conditions");
                }

                generated.getSchedule().put(day, calculatedDayType);
            } else {
                generated.getSchedule().put(day, existingDay);
            }

            // count the result
            if (DayType.REST.equals(calculatedDayType)) {
                totalRestDays++;
                consequentWorkingDays = 0;
            } else {
                consequentWorkingDays++;
                totalWorkingDays++;
            }
        }

        PersonMonthlySchedule personInitialSchedule = templateArg.getPersonSchedule(initialSchedule.getName());
        while (totalWorkingDays < NUM_WORKING_DAYS_PER_MONTH) {
            // get random day
            Long day = generateRandomRestDay((int) numDays);
            if (personInitialSchedule.getSchedule().get(day) != null) {
                // day present in the initial schedule
                continue;
            }

            // convert rest days to work one
            if (DayType.REST.equals(initialSchedule.getSchedule().get(day))) {

                // should not introduce too many consequent working days
                int numWorkingDaysIfDayConverted = 0;
                boolean tooManyWorkingDaysIfConverted = false;
                for (long i = Math.max(1, day - MAX_CONSEQUENT_WORKING_DAYS + 1); i < Math.min(numDays, day + MAX_CONSEQUENT_WORKING_DAYS - 1); i++) {
                    if (i != day) {
                        if (DayType.isWorkingDay(generated.getSchedule().get(i))) {
                            numWorkingDaysIfDayConverted++;
                        } else {
                            numWorkingDaysIfDayConverted = 0;
                        }
                    } else {
                        numWorkingDaysIfDayConverted++;
                    }

                    if (numWorkingDaysIfDayConverted > MAX_CONSEQUENT_WORKING_DAYS) {
                        tooManyWorkingDaysIfConverted = true;
                        break;
                    }
                }

                if (tooManyWorkingDaysIfConverted) {
                    // will introduce too many consequent days
                    continue;
                }

                int numWorkers = getNumPeopleWorkingAt(templateArg, day);
                if (numWorkers + 1 > MAX_PEOPLE_WORKING) {
                    // will introduce too many workers
                    continue;
                }

                generated.getSchedule().put(day, DayType.NORMAL_SHIFT);
                totalWorkingDays++;
                totalRestDays--;
            }
        }

        // add more rest days if needed
        totalRestDays = getNumRestDays(generated);
        while (totalRestDays < NUM_REST_DAYS_PER_MONTH) {
            // get random day
            Long day = generateRandomRestDay((int) numDays);
            if (personInitialSchedule.getSchedule().get(day) != null) {
                // day present in the initial schedule
                continue;
            }

            // convert to rest day if not already
            if (!DayType.REST.equals(initialSchedule.getSchedule().get(day))) {
                generated.getSchedule().put(day, DayType.REST);
                totalRestDays++;
                totalWorkingDays--;
            }
        }


        return generated;
    }

    public static DayType generateRandomDayType() {
        int dayIndex = new Random().nextInt(RANDOM_GENERATED_DAY_TYPES.length);
        return RANDOM_GENERATED_DAY_TYPES[dayIndex];
    }

    public static Long generateRandomRestDay(int numMonthDays) {
        return (long) new Random().nextInt(numMonthDays);
    }

    /*public static DayType generateRandomShiftWorkingDay() {
        int dayIndex = new Random().nextInt(RANDOM_WORKING_DAY_TYPES.length);
        return RANDOM_WORKING_DAY_TYPES[dayIndex];
    }*/

    public static int getNumPeopleWorkingAt(Schedule schedule, long day) {
        int numPeopleWorking = 0;
        for (PersonMonthlySchedule personSchedule : schedule.getPersonStatuses()) {
            DayType daySchedule = personSchedule.getSchedule().get(day);
            if (daySchedule != null && DayType.isWorkingDay(daySchedule)) {
                numPeopleWorking++;
            }
        }
        return numPeopleWorking;
    }

    public static int getNumRestDays(PersonMonthlySchedule schedule) {
        int numRestDays = 0;
        for (DayType day : schedule.getSchedule().values()) {
            if (DayType.REST.equals(day)) {
                numRestDays++;
            }
        }
        return numRestDays;
    }
    public static int getNumWorkingDays(PersonMonthlySchedule schedule) {
        int numWorkingDays = 0;
        for (DayType day : schedule.getSchedule().values()) {
            if (!DayType.REST.equals(day)) {
                numWorkingDays++;
            }
        }
        return numWorkingDays;
    }
    public static int calculateConsequentWorkingDays(PersonMonthlySchedule personMonthlySchedule, int numdays) {
        int consequentWorkingDays=0;
        for(int i=1;i<numdays;i++){
            DayType currentDay = personMonthlySchedule.get(i);
            if(currentDay==null){
                break;
            }
            else {
                if(currentDay.equals(DayType.REST)) consequentWorkingDays=0;
                else consequentWorkingDays++;
            }
        }
        return consequentWorkingDays;
    }
}

