package com.aatanasov.timetable.test;

import com.aatanasov.timetable.TimeTable;
import com.aatanasov.timetable.app.ExcelUtil;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static com.aatanasov.timetable.test.ExcelUtilTest.SAMPLE_INPUT_SCHEDULE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by aatanasov on 4/23/17.
 */
public class TimeTableTest {

    @Test
    public void testInputValidation() throws IOException {
        TimeTable.main(null);
        TimeTable.main(new String []{ "" });
        TimeTable.main(new String []{ "missing" });
        assertFalse(new File("missing_generated").exists());
    }
    //The test succeeds

    @Test
    public void testGenerateTimeTable() throws IOException {
        // prepare file
        Path templatePath = FileSystems.getDefault().getPath("target/input.xls");
        File targetFile = templatePath.toFile();
        Files.copy(FileSystems.getDefault().getPath(SAMPLE_INPUT_SCHEDULE), templatePath, StandardCopyOption.REPLACE_EXISTING);

        // generate
        TimeTable.main(new String[] {templatePath.toString()});

        // verify created
        File generated = new File("target/generated_input.xls");
        assertTrue(generated.exists());
        assertTrue(targetFile.length() < generated.length());

        // and valid
        ExcelUtil.readFrom(new FileInputStream(targetFile));
    }
}

