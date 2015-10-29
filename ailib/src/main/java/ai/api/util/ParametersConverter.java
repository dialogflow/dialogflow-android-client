package ai.api.util;

import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public abstract class ParametersConverter {

    public static final String PROTOCOL_DATE_FORMAT = "yyyy-MM-dd";
    public static final String PROTOCOL_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
    public static final String PROTOCOL_TIME_FORMAT = "HH:mm:ss";

    public static Date parseDateTime(final String parameter) throws ParseException {
        final SimpleDateFormat dateTimeFormat = new SimpleDateFormat(PROTOCOL_DATE_TIME_FORMAT, Locale.US);
        return dateTimeFormat.parse(parameter);
    }

    public static Date parseDate(final String parameter) throws ParseException {
        final SimpleDateFormat dateFormat = new SimpleDateFormat(PROTOCOL_DATE_FORMAT, Locale.US);
        return dateFormat.parse(parameter);
    }

    public static Date parseTime(final String parameter) throws ParseException {
        final SimpleDateFormat timeFormat = new SimpleDateFormat(PROTOCOL_TIME_FORMAT, Locale.US);
        final Date timeParameter = timeFormat.parse(parameter);

        final Calendar taskDueDate = Calendar.getInstance();
        taskDueDate.set(Calendar.HOUR_OF_DAY, timeParameter.getHours());
        taskDueDate.set(Calendar.MINUTE, timeParameter.getMinutes());
        taskDueDate.set(Calendar.SECOND, timeParameter.getSeconds());

        return taskDueDate.getTime();
    }

    public static PartialDate parsePartialDate(final String parameter) throws ParseException {
        if (TextUtils.isEmpty(parameter)) {
            throw new IllegalArgumentException("parameter must not be empty");
        }

        if (parameter.contains("u")) {
            // if date contains unknown parts
            final String[] parts = parameter.split("-");
            if (parts.length != 3) {
                throw new ParseException(String.format("Partial date must have 3 parts, but have %s: %s", parts.length, parameter), 0);
            }

            // check each part for unknown
            // each part must contains all digits or all 'u' without mixing
            final Integer year = parsePart(parts[0]);
            final Integer month = parsePart(parts[1]);
            final Integer day = parsePart(parts[2]);

            final PartialDate result = new PartialDate();
            result.set(Calendar.YEAR, year);
            result.set(Calendar.MONTH, month);
            result.set(Calendar.DATE, day);

            return result;
        } else {
            // parse as normal date

            final SimpleDateFormat dateFormat = new SimpleDateFormat(PROTOCOL_DATE_FORMAT, Locale.US);
            final Date date = dateFormat.parse(parameter);
            return new PartialDate(date);
        }
    }

    private static Integer parsePart(final String part) {
        if (part.contains("u")) {
            return PartialDate.UNSPECIFIED_VALUE;
        } else {
            return Integer.parseInt(part);
        }
    }

    public static int parseInteger(final String parameter) {
        return Integer.parseInt(parameter);
    }

    public static float parseFloat(final String parameter) {
        return Float.parseFloat(parameter);
    }
}
