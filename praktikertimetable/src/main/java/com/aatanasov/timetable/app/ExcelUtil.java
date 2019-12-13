package com.aatanasov.timetable.app;

import com.aatanasov.timetable.model.DayType;
import com.aatanasov.timetable.model.PersonMonthlySchedule;
import com.aatanasov.timetable.model.Schedule;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Reads and writes to/from Excel.
 */
public class ExcelUtil {

    public static final int CELL_INDEX_DAYS = 1;
    public static final int ROW_INDEX_DAYS = 3;
    public static final int ROW_INDEX_PEOPLE = ROW_INDEX_DAYS + 1;
    public static final int SHEET_INDEX = 0;

    public static Schedule readFrom(InputStream peopleTableArg) throws IOException {
        try {
            Schedule schedule = new Schedule();

            // open the file
            HSSFWorkbook workbook = new HSSFWorkbook(peopleTableArg);
            Sheet sheet = workbook.getSheetAt(SHEET_INDEX);

            // find number of days in the month
            int numDays = 0;
            Row daysRow = sheet.getRow(ROW_INDEX_DAYS);
            for (int i = 0; i < 31; i++) {
                Cell dayCell = daysRow.getCell(CELL_INDEX_DAYS + i);
                if (StringUtils.isNotBlank(getCellAsString(dayCell))) {
                    numDays++;
                } else {
                    break;
                }
            }

            // load the people
            int peopleIndex = 0;
            while (true) {
                Row personRow = sheet.getRow(ROW_INDEX_PEOPLE + peopleIndex);
                Cell nameCell = personRow.getCell(0);
                String peopleName = getCellAsString(nameCell);
                if (StringUtils.isBlank(peopleName)) {
                    // end of people
                    break;
                }


                System.out.println("Found = " + peopleName);
                PersonMonthlySchedule personSchedule = new PersonMonthlySchedule(peopleName);

                // load the preferences
                for (int day = 1; day < numDays; day++) {
                    Cell dayScheduleCell = personRow.getCell(day);
                    if (StringUtils.isNotBlank(getCellAsString(dayScheduleCell))) {
                        DayType type = DayType.fromString(getCellAsString(dayScheduleCell));
                        System.out.println(peopleName + " on " + day + " : " + type);
                        personSchedule.getSchedule().put(Long.valueOf(day), type);
                    }
                }


                // accumulate and continue
                schedule.addPersonStatus(personSchedule);
                peopleIndex++;
            }

            System.out.println("numDays = " + numDays);
            schedule.setNumDays(numDays);

            workbook.close();

            return schedule;
        } finally {
            IOUtils.closeQuietly(peopleTableArg);
        }
    }

    private static String getCellAsString(Cell cellArg) {
        if (cellArg == null) {
            return null;
        }

        try {
            return cellArg.getStringCellValue();
        } catch (Exception e) {
            // try to read as number
            try {
                Double number = cellArg.getNumericCellValue();
                // "1.0" to be read as "1"
                return String.valueOf(number.longValue());
            } catch (Exception e2) {
                e.printStackTrace();
            }
            e.printStackTrace();
        }
        return null;
    }

    public static void writeTo(Schedule scheduleArg, String pathArg) throws IOException {

        // open the file
        FileInputStream in = new FileInputStream(pathArg);
        try {
            HSSFWorkbook workbook = new HSSFWorkbook(in);
            Sheet sheet = workbook.getSheetAt(SHEET_INDEX);

            // iterate people
            int peopleIndex = 0;
            while (true) {
                Row personRow = sheet.getRow(ROW_INDEX_PEOPLE + peopleIndex);
                Cell nameCell = personRow.getCell(0);
                String peopleName = getCellAsString(nameCell);
                if (StringUtils.isBlank(peopleName)) {
                    // end of people
                    break;
                }

                // populate person schedule
                PersonMonthlySchedule personSchedule = findSchedule(scheduleArg, peopleName);
                for (Long day : personSchedule.getSchedule().keySet()) {
                    Cell cell = personRow.getCell(day.intValue());
                    DayType dayType = personSchedule.getSchedule().get(day);
                    cell.setCellValue(dayType.getCode());
                }
                peopleIndex++;
            }

            FileOutputStream out = new FileOutputStream(pathArg);
            try {
                workbook.write(out);
            } finally {
                IOUtils.closeQuietly(out);
            }
            workbook.close();
        } finally {
            IOUtils.closeQuietly(in);
        }

    }

    private static PersonMonthlySchedule findSchedule(Schedule scheduleArg, String peopleName) {
        for (PersonMonthlySchedule personSchedule : scheduleArg.getPersonStatuses()) {
            if (peopleName.equals(personSchedule.getName())) {
                return personSchedule;
            }
        }
        return null;
    }


}
