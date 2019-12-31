package org.mallen.test.common.utils;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * @author mallen
 * @date 12/30/19
 */
public class DateUtil {
    /**
     * 将date转换为毫秒级别时间戳
     *
     * @param date
     * @return
     */
    public static Long toMilliseconds(LocalDate date) {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
