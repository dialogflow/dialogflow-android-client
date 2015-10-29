package ai.api.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

/***********************************************************************************************************************
 * API.AI Android SDK - client-side libraries for API.AI
 * =================================================
 * <p/>
 * Copyright (C) 2015 by Speaktoit, Inc. (https://www.speaktoit.com)
 * https://www.api.ai
 * <p/>
 * *********************************************************************************************************************
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 ***********************************************************************************************************************/

/*
Class representing date with some unknown field values. Format according to http://www.loc.gov/standards/datetime/pre-submission.html
 */
public class PartialDate {
    public static final Integer UNSPECIFIED_VALUE = null;

    private static final String UNSPECIFIED_YEAR = "uuuu";
    private static final String UNSPECIFIED_MONTH = "uu";
    private static final String UNSPECIFIED_DATE = "uu";
    private static final String UNSPECIFIED_HOUR = "uu";
    private static final String UNSPECIFIED_MINUTE = "uu";

    private final Calendar c;
    private final Set<Integer> unspecifiedFields = new HashSet<>();

    public PartialDate() {
        c = Calendar.getInstance();
    }

    public PartialDate(final Calendar calendar) {
        this.c = calendar;
    }

    public PartialDate(final Date date) {
        c = new GregorianCalendar();
        c.setTime(date);
    }

    public void set(final int field, final Integer value) {
        if (value == UNSPECIFIED_VALUE) {
            if (field == Calendar.YEAR) {
                unspecifiedFields.add(Calendar.YEAR);
            } else if (field == Calendar.MONTH) {
                unspecifiedFields.add(Calendar.MONTH);
            } else if (field >= Calendar.WEEK_OF_YEAR && field <= Calendar.DAY_OF_WEEK_IN_MONTH) {
                unspecifiedFields.add(Calendar.DATE);
            } else if (field >= Calendar.HOUR && field <= Calendar.HOUR_OF_DAY) {
                unspecifiedFields.add(Calendar.HOUR_OF_DAY);
            } else if (field == Calendar.MINUTE) {
                unspecifiedFields.add(Calendar.MINUTE);
            }

            // do nothing with other fields

        } else {
            unspecifiedFields.remove(field);
            c.set(field, value);
        }
    }

    public Integer get(final int field) {

        if (field == Calendar.YEAR) {
            if (!unspecifiedFields.contains(Calendar.YEAR)) {
                return c.get(field);
            }
            return UNSPECIFIED_VALUE;
        } else if (field == Calendar.MONTH) {
            if (!unspecifiedFields.contains(Calendar.MONTH)) {
                return c.get(field);
            }
            return UNSPECIFIED_VALUE;
        } else if (field >= Calendar.WEEK_OF_YEAR && field <= Calendar.DAY_OF_WEEK_IN_MONTH) {
            if (!unspecifiedFields.contains(Calendar.DATE)) {
                return c.get(field);
            }
            return UNSPECIFIED_VALUE;
        } else if (field >= Calendar.HOUR && field <= Calendar.HOUR_OF_DAY) {
            if (!unspecifiedFields.contains(Calendar.HOUR_OF_DAY)) {
                return c.get(field);
            }
            return UNSPECIFIED_VALUE;
        } else if (field == Calendar.MINUTE) {
            if (!unspecifiedFields.contains(Calendar.MINUTE)) {
                return c.get(Calendar.MINUTE);
            }
            return UNSPECIFIED_VALUE;
        } else {
            return c.get(field);
        }

        //return UNSPECIFIED_VALUE;
    }

    private String getFieldAsString(final int field) {
        if (field == Calendar.YEAR) {
            if (unspecifiedFields.contains(Calendar.YEAR)) {
                return UNSPECIFIED_YEAR;
            } else {
                return String.format("%4d", c.get(field));
            }
        } else if (field == Calendar.MONTH) {
            if (unspecifiedFields.contains(Calendar.MONTH)) {
                return UNSPECIFIED_MONTH;
            } else {
                return String.format("%02d", c.get(field));
            }
        } else if (field >= Calendar.WEEK_OF_YEAR && field <= Calendar.DAY_OF_WEEK_IN_MONTH) {
            if (unspecifiedFields.contains(Calendar.DATE)) {
                return UNSPECIFIED_DATE;
            } else {
                return String.format("%02d", c.get(field));
            }
        } else if (field >= Calendar.HOUR && field <= Calendar.HOUR_OF_DAY) {
            if (unspecifiedFields.contains(Calendar.HOUR_OF_DAY)) {
                return UNSPECIFIED_HOUR;
            } else {
                return String.format("%02d", c.get(field));
            }
        } else if (field == Calendar.MINUTE) {
            if (unspecifiedFields.contains(Calendar.MINUTE)) {
                return UNSPECIFIED_MINUTE;
            } else {
                return String.format("%02d", c.get(field));
            }
        } else {
            return String.format("%s", c.get(field));
        }
    }

    @Override
    public String toString() {
        return String.format("%s-%s-%s",
                getFieldAsString(Calendar.YEAR),
                getFieldAsString(Calendar.MONTH),
                getFieldAsString(Calendar.DATE));
    }
}
