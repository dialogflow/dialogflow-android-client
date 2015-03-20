package ai.api.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.text.ParseException;
import java.util.Date;

import ai.api.util.ParametersConverter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class ParametersConverterTest {

    @Test
    public void parseTimeTest() {
        final String inputTime = "13:17:50";
        try {
            final Date date = ParametersConverter.parseTime(inputTime);
            final Date currentDate = new Date();

            assertEquals(currentDate.getYear(), date.getYear());
            assertEquals(currentDate.getMonth(), date.getMonth());
            assertEquals(currentDate.getDate(), date.getDate());
            assertEquals(13, date.getHours());
            assertEquals(17, date.getMinutes());
            assertEquals(50, date.getSeconds());

        } catch (final ParseException e) {
            assertTrue(e.getMessage(), false);
        }
    }
}
