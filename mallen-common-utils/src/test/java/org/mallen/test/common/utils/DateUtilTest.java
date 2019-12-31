package org.mallen.test.common.utils;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;

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
}
