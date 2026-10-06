package com.queuesense.util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {

    public static final DateTimeFormatter DISPLAY_DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private DateTimeUtil() {}

    public static String formatDateTime(LocalDateTime dt) {
        return dt != null ? dt.format(DISPLAY_DATETIME) : "-";
    }

    public static String formatTime(LocalDateTime dt) {
        return dt != null ? dt.format(DISPLAY_TIME) : "-";
    }

    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DISPLAY_DATE) : "-";
    }

    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return Duration.between(start, end).toMinutes();
    }

    public static long secondsBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return Duration.between(start, end).getSeconds();
    }
}
