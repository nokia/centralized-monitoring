/**
 * Copyright 2020 Nokia
 * Licensed under the BSD 3-Clause Clear License.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package com.nokia.as.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DateUtil {

    public static Date getJiraDate(String s) {
        try {
            String[] splittedDate = s.split("T");
            String date = splittedDate[0];
            String time = splittedDate[1].split("\\.")[0];

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return formatter.parse(date + " " + time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Date getStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Date getStartOfDay() {
        return getStartOfDay(new Date());
    }

    public static String secondsToString(Long pTime, Integer labelsLimit) {
        final long years = pTime / 31536000;
        final long mo = (pTime % 31536000) / 2592000;
        final long days = (pTime % 2592000) / 86400;
        final long hr = (pTime % 86400) / 3600;
        final long min = (pTime % 3600) / 60;
        final long sec = pTime % 60;

        List<String> time = new ArrayList<>();

        StringBuilder s = new StringBuilder();
        if (years > 0) {
            String label = years > 1 ? "years" : "year";
            time.add(years + " " + label);
        }
        if (mo > 0) {
            time.add(mo + " mo");
        }
        if (days > 0) {
            String label = days > 1 ? "days" : "day";
            time.add(days + " " + label);
        }
        if (hr > 0) {
            time.add(hr + " hr");
        }
        if (min > 0) {
            time.add(min + " min");
        }
        if (sec > 0) {
            time.add(sec + " sec");
        }

        int size = time.size() < labelsLimit ? time.size() : labelsLimit;
        for (int i = 0; i < size; i++) {
            s.append(time.get(i));
            if (i < time.size() - 1) {
                s.append(" ");
            }
        }

        return s.toString();
    }

    public static Long millisecondsSince(Date date) {
        return new Date().getTime() - date.getTime();
    }
}
