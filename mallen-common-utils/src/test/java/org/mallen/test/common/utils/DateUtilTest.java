package org.mallen.test.common.utils;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Date;

/**
 * @author mallen
 * @date 12/30/19
 */
public class DateUtilTest {

    @Test
    public void testLocalDate2Milliseconds() {
        LocalDate date = LocalDate.of(2019, 12, 30);
        Assert.assertEquals(1577635200000L, (long)DateUtil.toMilliseconds(date));
    }

    @Test
    public void testMilliseconds2ToDate() {
        LocalDate translatedDate = DateUtil.toDate(1577635200000L);
        LocalDate expectDate = LocalDate.of(2019, 12, 30);
        Assert.assertEquals(expectDate, translatedDate);
    }

    @Test
    public void testFirstSecondOfMonth() {
        LocalDate date = LocalDate.of(2019, 12, 31);
        Assert.assertEquals(1575129600000L, (long)DateUtil.firstSecondOfMonth(date));
    }

    @Test
    public void testLastSecondOfMonth() {
        LocalDate date = LocalDate.of(2019, 12, 31);
        Assert.assertEquals(1577807999000L, (long)DateUtil.lastSecondOfMonth(date));
    }

    @Test
    public void testFirstSecondOfYear() {
        LocalDate date = LocalDate.of(2019, 12, 31);
        Assert.assertEquals(1546272000000L, (long)DateUtil.firstSecondOfYear(date));
    }

    @Test
    public void testLastSecondOfYear() {
        LocalDate date = LocalDate.of(2019, 12, 31);
        Assert.assertEquals(1577807999000L, (long)DateUtil.lastSecondOfYear(date));
    }

    @Test
    public void testParseDate() {
        LocalDate translatedDate = DateUtil.parseDate("2020-01-02", "yyyy-MM-dd");
        LocalDate expectDate = LocalDate.of(2020, 1, 2);
        Assert.assertEquals(expectDate, translatedDate);
    }

    @Test
    public void testParseMilliseconds() {
        Long translated = DateUtil.parseMilliseconds("2020-01-02", "yyyy-MM-dd");
        // 2020.01.02的毫秒级别时间戳
        Long expected = 1577894400000L;
        Assert.assertEquals(expected, translated);
    }
}
