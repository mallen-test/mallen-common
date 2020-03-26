package org.mallen.test.common.utils;

/**
 * @author mallen
 * @date 3/26/20
 */
public class PageUtil {
    /**
     * 分页pageIndex参数处理，
     *
     * @param pageIndex 小于1取1，默认为1
     */
    public static Integer setPageIndex(Integer pageIndex) {
        //页索引 小于1取1，默认为1
        if (pageIndex == null) {
            pageIndex = 1;
        } else if (pageIndex < 1) {
            pageIndex = 1;
        }
        return pageIndex;
    }

    /**
     * 分页pageSize参数处理，
     *
     * @param pageSize 小于1取1，大于200取200，默认1
     */
    public static Integer setPageSize(Integer pageSize) {
        //每页记录数 小于1取1，大于200取200，默认1
        if (pageSize == null) {
            pageSize = 1;
        } else if (pageSize < 1) {
            pageSize = 1;
        } else if (pageSize > 200) {
            pageSize = 200;
        }
        return pageSize;
    }
}
