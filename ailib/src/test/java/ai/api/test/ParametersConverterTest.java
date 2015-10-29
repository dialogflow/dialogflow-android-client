package ai.api.test;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import ai.api.BuildConfig;
import ai.api.util.ParametersConverter;
import ai.api.util.PartialDate;

import static org.junit.Assert.assertEquals;

@Config(constants = BuildConfig.class, manifest = Config.NONE, sdk = BuildConfig.TESTS_SDK)
@RunWith(RobolectricTestRunner.class)
public class ParametersConverterTest {

    @Test
    public void parseTimeTest() throws ParseException {
        final String inputTime = "13:17:50";

        final Date date = ParametersConverter.parseTime(inputTime);
        final Date currentDate = new Date();

        assertEquals(currentDate.getYear(), date.getYear());
        assertEquals(currentDate.getMonth(), date.getMonth());
        assertEquals(currentDate.getDate(), date.getDate());
        assertEquals(13, date.getHours());
        assertEquals(17, date.getMinutes());
        assertEquals(50, date.getSeconds());
    }

    @Test
    @Ignore
    public void parseDateTimeTest() throws ParseException {
        final String input = "2015-03-21T07:00:00+06:00";

        final Date date = ParametersConverter.parseTime(input);

        assertEquals(2015, date.getYear());
        assertEquals(3 - 1 /* month is zero-based */, date.getMonth());
        assertEquals(21, date.getDate());
        assertEquals(7, date.getHours());
        assertEquals(0, date.getMinutes());
    }

    @Test
    public void parsePartialDateTest() throws ParseException {
        // date in format "yyyy-MM-dd"
        final String unknownDate = "1999-05-uu";
        final String unknownMonth = "2005-uu-17";
        final String unknownYear = "uuuu-07-23";
        final String unknownMonthDate = "2008-uu-uu";

        PartialDate date = ParametersConverter.parsePartialDate(unknownDate);

        assertEquals(1999, date.get(Calendar.YEAR).intValue());
        assertEquals(5, date.get(Calendar.MONTH).intValue());
        assertEquals(PartialDate.UNSPECIFIED_VALUE, date.get(Calendar.DATE));
        assertEquals(PartialDate.UNSPECIFIED_VALUE, date.get(Calendar.DAY_OF_WEEK));
        assertEquals(PartialDate.UNSPECIFIED_VALUE, date.get(Calendar.DAY_OF_YEAR));
        assertEquals(unknownDate, date.toString());

        date = ParametersConverter.parsePartialDate(unknownMonth);
        assertEquals(2005, date.get(Calendar.YEAR).intValue());
        assertEquals(PartialDate.UNSPECIFIED_VALUE, date.get(Calendar.MONTH));
        assertEquals(17, date.get(Calendar.DATE).intValue());
        assertEquals(unknownMonth, date.toString());

        date = ParametersConverter.parsePartialDate(unknownYear);
        assertEquals(PartialDate.UNSPECIFIED_VALUE, date.get(Calendar.YEAR));
        assertEquals(7, date.get(Calendar.MONTH).intValue());
        assertEquals(23, date.get(Calendar.DATE).intValue());
        assertEquals(unknownYear, date.toString());

        date = ParametersConverter.parsePartialDate(unknownMonthDate);
        assertEquals(2008, date.get(Calendar.YEAR).intValue());
        assertEquals(PartialDate.UNSPECIFIED_VALUE, date.get(Calendar.MONTH));
        assertEquals(PartialDate.UNSPECIFIED_VALUE, date.get(Calendar.DATE));
        assertEquals(unknownMonthDate, date.toString());

    }

}
