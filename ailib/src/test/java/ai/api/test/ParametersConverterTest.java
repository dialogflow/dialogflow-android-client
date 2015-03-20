package ai.api.test;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

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

        } catch (final Exception e) {
            assertTrue(e.getMessage(), false);
        }
    }

    @Test
    @Ignore
    public void parseDateTimeTest() {
        final String input = "2015-03-21T07:00:00+06:00";
        try {
            final Date date = ParametersConverter.parseTime(input);

            assertEquals(2015, date.getYear());
            assertEquals(3 - 1 /* month is zero-based */, date.getMonth());
            assertEquals(21, date.getDate());
            assertEquals(7, date.getHours());
            assertEquals(0, date.getMinutes());

        } catch (final Exception e) {
            assertTrue(e.getMessage(), false);
        }
    }
}
