package ai.api.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public abstract class ParametersConverter {

    public static Date parseDateTime(final String parameter) throws ParseException {
        final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);
        return dateTimeFormat.parse(parameter);
    }

    public static Date parseDate(final String parameter) throws ParseException {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return dateFormat.parse(parameter);
    }

    public static Date parseTime(final String parameter) throws ParseException {
        final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
        final Date timeParameter = timeFormat.parse(parameter);

        final Calendar taskDueDate = Calendar.getInstance();
        taskDueDate.set(Calendar.HOUR_OF_DAY, timeParameter.getHours());
        taskDueDate.set(Calendar.MINUTE, timeParameter.getMinutes());
        taskDueDate.set(Calendar.SECOND, timeParameter.getSeconds());

        return taskDueDate.getTime();
    }

    public static int parseInteger(final String parameter) {
        return Integer.parseInt(parameter);
    }

    public static float parseFloat(final String parameter) {
        return Float.parseFloat(parameter);
    }
}
