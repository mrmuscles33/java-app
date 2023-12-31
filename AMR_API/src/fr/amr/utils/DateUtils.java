package fr.amr.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Stream;

public class DateUtils {

    public static final String D_M_Y = "dd/MM/yyyy";
    public static final String D_M_Y_H_M_S = "dd/MM/yyyy HH:mm:ss";
    public static final String ORA_D_M_Y_H_M_S = "DD/MM/YYYY HH24:MI:SS";
    public static final String Y_M_D = "yyyy/MM/dd";
    public static final String Y_M_D_H_M_S = "yyyy/MM/dd HH:mm:ss";
    public static final String ORA_Y_M_D_H_M_S = "YYYY/MM/DD HH:MI:SS";
    public static final String YMD = "yyyyMMdd";
    public static final String YDM = "yyyyDDmm";
    public static final String YMDHMS = "yyyyMMddHHMMSS";
    public static final String YDMHMS = "yyyyddMMHHmmss";
    public static final String ORA_YMDHMS = "YYYYMMDDHH24MISS";
    private static final String ORA_YDMHMS = "YYYYDDMMHH24MISS";


    private DateUtils() {
        super();
    }

    /**
     * Convert a date to a string using the given format
     *
     * @param date   The date to convert
     * @param format The format to use
     * @return The date as a string
     */
    public static String toString(LocalDateTime date, String format) {
        if(date == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return date.format(formatter);
    }

    /**
     * Convert a date to a string using the given format
     *
     * @param date   The date to convert
     * @param format The format to use
     * @return The date as a string
     */
    public static String toString(LocalDate date, String format) {
        return toString(date.atStartOfDay(), format);
    }

    /**
     * Convert a date to a string using the default format (dd/MM/yyyy HH:mm:ss)
     *
     * @param date The date to convert
     * @return The date as a string
     */
    public static String toString(LocalDateTime date) {
        return toString(date, D_M_Y_H_M_S);
    }

    /**
     * Convert a date to a string using the default format (dd/MM/yyyy)
     *
     * @param date The date to convert
     * @return The date as a string
     */
    public static String toString(LocalDate date) {
        return toString(date.atStartOfDay(), D_M_Y);
    }

    /**
     * Convert a string to a date using the given format
     *
     * @param date   The date to convert
     * @param format The format to use
     * @return The date
     */
    public static LocalDateTime toDateTime(String date, String format) {
        format = StringUtils.isEmpty(format) ? guessFormat(date) : format;
        return StringUtils.isEmpty(date) ? null : LocalDateTime.parse(date, DateTimeFormatter.ofPattern(format));
    }

    /**
     * Convert a string to a date and guess the format
     *
     * @param date The date to convert
     * @return The date
     */
    public static LocalDateTime toDateTime(String date) {
        return toDateTime(date, guessFormat(date));
    }

    /**
     * Convert a string to a date using the given format
     *
     * @param date   The date to convert
     * @param format The format to use
     * @return The date
     */
    public static LocalDate toDate(String date, String format) {
        return toDateTime(date, format).toLocalDate();
    }

    /**
     * Convert a string to a date and guess the format
     *
     * @param date The date to convert
     * @return The date
     */
    public static LocalDate toDate(String date) {
        return toDateTime(date, guessFormat(date)).toLocalDate();
    }

    /**
     * Return the current date time
     *
     * @return The current date time
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * Return the current date
     *
     * @return The current date
     */
    public static LocalDate today() {
        return LocalDate.now();
    }

    /**
     * Return a date time
     *
     * @param year
     * @param month
     * @param dayOfMonth
     * @param hour
     * @param minute
     * @param second
     * @return
     */
    public static LocalDateTime get(int year, int month, int dayOfMonth, int hour, int minute, int second) {
        return LocalDateTime.of(year, month, dayOfMonth, hour, minute, second);
    }

    /**
     * Return a date
     *
     * @param year
     * @param month
     * @param dayOfMonth
     * @return
     */
    public static LocalDate get(int year, int month, int dayOfMonth) {
        return LocalDate.of(year, month, dayOfMonth);
    }

    /**
     * Check if a string is a valid date
     *
     * @param date   The date to check
     * @param format The format to use
     * @return True if the string is a valid date
     */
    public static boolean isDate(String date, String format) {
        try {
            toDateTime(date, format);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Find the format of a date
     *
     * @param date The date to check
     * @return The format of the date
     */
    public static String guessFormat(String date) {
        return Stream.of(D_M_Y_H_M_S, D_M_Y, Y_M_D_H_M_S, Y_M_D, YMDHMS, YMD, YDMHMS, YDM)
                .filter(format -> isDate(date, format))
                .findFirst()
                .orElse("");
    }

    /**
     * Get format Oracle from format Java
     *
     * @param formatJava The format Java
     * @return The format Oracle
     */
    public static String formatOracle(String formatJava) {
        return switch (formatJava) {
            case D_M_Y_H_M_S -> ORA_D_M_Y_H_M_S;
            case Y_M_D_H_M_S -> ORA_Y_M_D_H_M_S;
            case YMDHMS -> ORA_YMDHMS;
            case YDMHMS -> ORA_YDMHMS;
            default -> formatJava;
        };
    }

    /**
     * Get format Java from format Oracle
     *
     * @param formatOracle The format Oracle
     * @return The format Java
     */
    public static String formatJava(String formatOracle) {
        return switch (formatOracle) {
            case ORA_D_M_Y_H_M_S -> D_M_Y_H_M_S;
            case ORA_Y_M_D_H_M_S -> Y_M_D_H_M_S;
            case ORA_YMDHMS -> YMDHMS;
            case ORA_YDMHMS -> YDMHMS;
            default -> formatOracle;
        };
    }

    /**
     * Check if a date is between two dates
     *
     * @param date  The date to check
     * @param start The start date
     * @param end   The end date
     * @return True if the date is between start and end, false otherwise
     */
    public static boolean isBetween(LocalDateTime date, LocalDateTime start, LocalDateTime end) {
        return date.isAfter(start) && date.isBefore(end);
    }

}
