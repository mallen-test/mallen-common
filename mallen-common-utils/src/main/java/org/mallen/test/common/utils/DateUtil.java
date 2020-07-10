package org.mallen.test.common.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author mallen
 * @date 12/30/19
 */
public class DateUtil {
    private static Map<String, DateTimeFormatter> cache = new ConcurrentHashMap<>();

    /**
     * 使用默认时区格式化时间戳
     *
     * @param timestamp
     * @param pattern
     * @return
     */
    public static String formatMs(Long timestamp, String pattern) {
        return formatMs(timestamp, pattern, ZoneId.systemDefault());
    }

    public static String formatMs(Long timestamp, String pattern, ZoneId zoneId) {
        DateTimeFormatter dateTimeFormatter = getFormatter(pattern);
        Instant.ofEpochMilli(timestamp);
        return dateTimeFormatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), zoneId));
    }

    /**
     * 将date转换为毫秒级别时间戳
     *
     * @param date
     * @return
     */
    public static Long toMilliseconds(LocalDate date) {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * 将时间戳转换为local date
     *
     * @param timestamp
     * @return
     */
    public static LocalDate toDate(Long timestamp) {
        return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * 计算某天所在月份的开始时间戳，也就是当月1日的0点0分0秒
     *
     * @param someDayInMonth
     * @return 毫秒级别时间戳
     */
    public static Long firstSecondOfMonth(LocalDate someDayInMonth) {
        LocalDate firstDayOfMonth = someDayInMonth.withDayOfMonth(1);
        return DateUtil.toMilliseconds(firstDayOfMonth);
    }

    /**
     * 计算某天所在月份的结束时间戳，也就是当月最后一日（28，30，21）的23点59分59秒
     *
     * @param someDayInMonth
     * @return 毫秒级别时间戳
     */
    public static Long lastSecondOfMonth(LocalDate someDayInMonth) {
        // 下个月的第一天
        LocalDate firstDayOfNextMonth = someDayInMonth.plusMonths(1).withDayOfMonth(1);
        // 下个月的第一天减去1秒，即为当月的最后一秒
        return DateUtil.toMilliseconds(firstDayOfNextMonth) - (1 * 1000);
    }

    /**
     * 计算某天所在年份的开始时间戳，也就是当年1月1日的0点0分0秒
     *
     * @param someDayInYear
     * @return 毫秒级别时间戳
     */
    public static Long firstSecondOfYear(LocalDate someDayInYear) {
        LocalDate firstDayOfYear = someDayInYear.withDayOfYear(1);
        return DateUtil.toMilliseconds(firstDayOfYear);
    }

    /**
     * 计算某天所在年份的结束时间戳，也就是当年12月31日的23点59分59秒
     *
     * @param someDayInYear
     * @return 毫秒级别时间戳
     */
    public static Long lastSecondOfYear(LocalDate someDayInYear) {
        LocalDate firstDayOfNextYear = someDayInYear.withDayOfYear(1).withYear(someDayInYear.getYear() + 1);
        return DateUtil.toMilliseconds(firstDayOfNextYear) - (1 * 1000);
    }

    /**
     * 将日期字符串转换为LocalDate
     *
     * @param dateText 时间字符串
     * @param pattern  时间格式，比如：yyyy-MM-dd
     * @return
     */
    public static LocalDate parseDate(String dateText, String pattern) {
        DateTimeFormatter formatter = getFormatter(pattern);
        return parseDate(dateText, formatter);
    }

    /**
     * 将日期字符串转换为LocalDate
     *
     * @param dateText  时间字符串
     * @param formatter
     * @return
     */
    public static LocalDate parseDate(String dateText, DateTimeFormatter formatter) {
        TemporalAccessor temporalAccessor = formatter.parse(dateText);
        return LocalDate.from(temporalAccessor);
    }

    /**
     * 将日期字符串转换为毫秒时间戳
     *
     * @param dateText 时间字符串
     * @param pattern  时间格式，比如：yyyy-MM-dd
     * @return
     */
    public static Long parseMilliseconds(String dateText, String pattern) {
        LocalDate localDate = parseDate(dateText, pattern);
        return DateUtil.toMilliseconds(localDate);
    }

    /**
     * 将日期字符串转换为毫秒时间戳
     *
     * @param dateText  时间字符串
     * @param formatter 时间格式，比如：yyyy-MM-dd
     * @return
     */
    public static Long parseMilliseconds(String dateText, DateTimeFormatter formatter) {
        LocalDate localDate = parseDate(dateText, formatter);
        return DateUtil.toMilliseconds(localDate);
    }

    private static DateTimeFormatter getFormatter(String pattern) {
        DateTimeFormatter dateTimeFormatter = cache.get(pattern);
        if (null == dateTimeFormatter) {
            synchronized (DateUtil.class) {
                if (null == dateTimeFormatter) {
                    dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
                    cache.put(pattern, dateTimeFormatter);
                }
            }
        }
        return dateTimeFormatter;
    }
}
