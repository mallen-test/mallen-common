package org.mallen.test.common.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author mallen
 * @date 12/30/19
 */
public class DateUtil {
    private static Map<String, DateTimeFormatter> cache = new ConcurrentHashMap<>();
    private static ZoneOffset localZoneOffset = OffsetDateTime.now().getOffset();

    /**
     * 格式化时间
     *
     * @param temporal LocalDateTime或者LocalDate
     * @param pattern
     * @return
     */
    public static String format(TemporalAccessor temporal, String pattern) {
        DateTimeFormatter dateTimeFormatter = getFormatter(pattern);
        return dateTimeFormatter.format(temporal);
    }

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
    public static Long toMs(LocalDate date) {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * 转换DateTime为毫秒时间戳
     *
     * @param dateTime
     * @return
     */
    public static Long toMs(LocalDateTime dateTime) {
        return dateTime.toInstant(localZoneOffset).toEpochMilli();
    }

    /**
     * 将时间戳转换为LocalDate
     *
     * @param timestamp
     * @return
     */
    public static LocalDate toDate(Long timestamp) {
        return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * 将时间戳转转为LocalDateTime
     *
     * @param timestamp
     * @return
     */
    public static LocalDateTime toDateTime(Long timestamp) {
        return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * 获取当天的第0秒
     *
     * @return 毫秒级别时间戳
     */
    public static Long firstSecondOfToday() {
        LocalDate now = LocalDate.now();
        return DateUtil.toMs(now);
    }

    /**
     * 获取某一天的第0秒
     *
     * @param day
     * @return 毫秒级别时间戳
     */
    public static Long firstSecondOfDay(LocalDate day) {
        return DateUtil.toMs(day);
    }

    /**
     * 计算某天所在月份的开始时间戳，也就是当月1日的0点0分0秒
     *
     * @param someDayInMonth
     * @return 毫秒级别时间戳
     */
    public static Long firstSecondOfMonth(LocalDate someDayInMonth) {
        LocalDate firstDayOfMonth = someDayInMonth.withDayOfMonth(1);
        return DateUtil.toMs(firstDayOfMonth);
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
        return DateUtil.toMs(firstDayOfNextMonth) - (1 * 1000);
    }

    /**
     * 计算某天所在年份的开始时间戳，也就是当年1月1日的0点0分0秒
     *
     * @param someDayInYear
     * @return 毫秒级别时间戳
     */
    public static Long firstSecondOfYear(LocalDate someDayInYear) {
        LocalDate firstDayOfYear = someDayInYear.withDayOfYear(1);
        return DateUtil.toMs(firstDayOfYear);
    }

    /**
     * 计算某天所在年份的结束时间戳，也就是当年12月31日的23点59分59秒
     *
     * @param someDayInYear
     * @return 毫秒级别时间戳
     */
    public static Long lastSecondOfYear(LocalDate someDayInYear) {
        LocalDate firstDayOfNextYear = someDayInYear.withDayOfYear(1).withYear(someDayInYear.getYear() + 1);
        return DateUtil.toMs(firstDayOfNextYear) - (1 * 1000);
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
     * 解析dateTime字符串为LocalDateTime
     *
     * @param dateTimeText
     * @param pattern
     * @return
     */
    public static LocalDateTime parseDateTime(String dateTimeText, String pattern) {
        DateTimeFormatter formatter = getFormatter(pattern);
        return parseDateTime(dateTimeText, formatter);
    }

    /**
     * 解析dateTime字符串为LocalDateTime
     *
     * @param dateTimeText
     * @param formatter
     * @return
     */
    public static LocalDateTime parseDateTime(String dateTimeText, DateTimeFormatter formatter) {
        TemporalAccessor temporalAccessor = formatter.parse(dateTimeText);
        return LocalDateTime.from(temporalAccessor);
    }

    /**
     * 将日期字符串转换为毫秒时间戳
     *
     * @param dateText 时间字符串
     * @param pattern  时间格式，比如：yyyy-MM-dd
     * @return
     */
    public static Long parseDate2Ms(String dateText, String pattern) {
        LocalDate localDate = parseDate(dateText, pattern);
        return DateUtil.toMs(localDate);
    }

    /**
     * 将日期字符串转换为毫秒时间戳
     *
     * @param dateText  时间字符串
     * @param formatter 时间格式，比如：yyyy-MM-dd
     * @return
     */
    public static Long parseDate2Ms(String dateText, DateTimeFormatter formatter) {
        LocalDate localDate = parseDate(dateText, formatter);
        return DateUtil.toMs(localDate);
    }

    /**
     * 解析dateTime字符串为LocalDateTime
     *
     * @param dateTimeText
     * @param pattern
     * @return
     */
    public static Long parseDateTime2Ms(String dateTimeText, String pattern) {
        LocalDateTime localDateTime = parseDateTime(dateTimeText, pattern);
        return toMs(localDateTime);
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
