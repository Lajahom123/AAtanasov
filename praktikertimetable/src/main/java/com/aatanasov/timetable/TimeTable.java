package com.aatanasov.timetable;

import com.aatanasov.timetable.app.ExcelUtil;
import com.aatanasov.timetable.app.ScheduleCalculator;
import com.aatanasov.timetable.model.Schedule;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by aatanasov on 3/31/17.
 */
public class TimeTable {

    public static void main(String[] args) throws IOException {

        System.out.println("Start time table");

        // validations
        if (args == null || args.length != 1) {
            System.err.println("One path parameter expected");
            return;
        }

        String templateFilePath = args[0];
        File templateFile = new File(templateFilePath);
        if (!templateFile.exists()) {
            System.err.println("File missing :" + templateFilePath);
            return;
        }

        // load
        Schedule templateSchedule = ExcelUtil.readFrom(new FileInputStream(templateFile));

        Schedule preparedSchedule = ScheduleCalculator.generateSchedule(templateSchedule);

        // create copy
        File outputFile = new File(templateFile.getParent(), "generated_" + templateFile.getName());
        FileUtils.copyFile(templateFile, outputFile);

        // update the copy
        ExcelUtil.writeTo(preparedSchedule, outputFile.getAbsolutePath());

        System.out.println("templateFilePath = " + templateFilePath);
    }


}
