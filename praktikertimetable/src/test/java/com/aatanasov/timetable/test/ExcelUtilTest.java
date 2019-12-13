package com.aatanasov.timetable.test;

import com.aatanasov.timetable.app.ExcelUtil;
import com.aatanasov.timetable.model.DayType;
import com.aatanasov.timetable.model.PersonMonthlySchedule;
import com.aatanasov.timetable.model.Schedule;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by aatanasov on 4/22/17.
 */
public class ExcelUtilTest {

    public static final String SAMPLE_INPUT_SCHEDULE = "src/test/resources/sample-input.xls";

    @Test
    public void testLoadExcelFile() throws IOException {
        // read
        Schedule schedule = ExcelUtil.readFrom(new FileInputStream(SAMPLE_INPUT_SCHEDULE));

        // verify persons
        Map<String, PersonMonthlySchedule> expectedSchedules = getSampleInputExpectedSchedule();
        checkSchedule(expectedSchedules, schedule);
    }
    // The test succeeds

    @Test
    public void testWriteExcelFile() throws IOException {

        // read
        Schedule schedule = ExcelUtil.readFrom(new FileInputStream(SAMPLE_INPUT_SCHEDULE));

        // write out
        String targetFilePath = "target/sample-input.xls";
        Files.copy(FileSystems.getDefault().getPath(SAMPLE_INPUT_SCHEDULE), FileSystems.getDefault().getPath(targetFilePath), StandardCopyOption.REPLACE_EXISTING);
        ExcelUtil.writeTo(schedule, targetFilePath);

        // check properly stored
        Schedule reloadedSchedule = ExcelUtil.readFrom(new FileInputStream(targetFilePath));
        checkSchedule(getSampleInputExpectedSchedule(), reloadedSchedule);
        // The test succeeds
    }

    private Map<String, PersonMonthlySchedule> getSampleInputExpectedSchedule() {
        Map<String, PersonMonthlySchedule> expectedSchedules = new HashMap<>();
        PersonMonthlySchedule boyan = new PersonMonthlySchedule("Боян Георгиев");
        expectedSchedules.put(boyan.getName(), boyan);

        PersonMonthlySchedule vasko = new PersonMonthlySchedule("Васил Атанасов");
        vasko.add(8, DayType.HOLIDAY);
        vasko.add(13, DayType.REST);
        vasko.add(16, DayType.NORMAL_SHIFT);
        vasko.add(22, DayType.REST);
        vasko.add(27, DayType.FIRST_SHIFT);
        expectedSchedules.put(vasko.getName(), vasko);

        PersonMonthlySchedule georgi = new PersonMonthlySchedule("Георги Попов");
        expectedSchedules.put(georgi.getName(), georgi);

        PersonMonthlySchedule garo = new PersonMonthlySchedule("Гаро Каракеворкян");
        garo.add(8, DayType.HOLIDAY);
        garo.add(9, DayType.HOLIDAY);
        garo.add(13, DayType.REST);
        garo.add(18, DayType.FIRST_SHIFT);
        garo.add(19, DayType.SECOND_SHIFT);
        garo.add(26, DayType.FIRST_SHIFT);
        garo.add(27, DayType.FIRST_SHIFT);
        expectedSchedules.put(garo.getName(), garo);

        PersonMonthlySchedule desi = new PersonMonthlySchedule("Десислава Асенова");
        desi.add(22, DayType.EDUCATION);
        expectedSchedules.put(desi.getName(), desi);

        PersonMonthlySchedule aleksandar = new PersonMonthlySchedule("Александър Макавеев");
        expectedSchedules.put(aleksandar.getName(), aleksandar);
        return expectedSchedules;
    }

    private void checkSchedule(Map<String, PersonMonthlySchedule> expectedSchedules, Schedule schedule) {
        // schedule loaded
        assertNotNull(schedule);
        assertNotNull(schedule.getPersonStatuses());
        assertEquals(31l, schedule.getNumDays());

        // persons
        for (PersonMonthlySchedule personSchedule : schedule.getPersonStatuses()) {
            PersonMonthlySchedule expectedPersonMonthlySchedule = expectedSchedules.get(personSchedule.getName());
            assertNotNull("Not found " + personSchedule.getName(), expectedPersonMonthlySchedule);
            // same number of records
            assertEquals(expectedPersonMonthlySchedule.getSchedule().size(), personSchedule.getSchedule().size());
            assertEquals(expectedPersonMonthlySchedule.getName(), personSchedule.getName());
            // same data
            for (Long day : expectedPersonMonthlySchedule.getSchedule().keySet()) {
                assertTrue(personSchedule.getSchedule().containsKey(day));
                assertEquals(expectedPersonMonthlySchedule.getSchedule().get(day), personSchedule.getSchedule().get(day));
            }
        }

    }

}
