package org.mallen.test.common.dao.rest;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mallen
 * @date 10/16/18
 * @deprecated 已迁移到mallen-common-rest-client模块
 */
@Deprecated
public class PathVariable {
    private Map params;

    public PathVariable add(String key, Object value) {
        params.put(key, value);
        return this;
    }

    public PathVariable() {
        params = new HashMap();
    }

    public Map getParams() {
        return params;
    }
}
