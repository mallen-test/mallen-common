package org.mallen.test.common.dao.rest;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 针对{@code CommonRest}类的配置项目定义
 *
 * @author taoningbo
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "mallen.rest")
public class CommonRestProperties {
    private static volatile DateTimeFormatter logTimeFormatter = null;
    private int connectTimeout = 1000 * 60 * 2;
    private int readTimeout = 1000 * 60 * 2;
    private int asyncConnectTimeout = 1000 * 60 * 2;
    private int asyncReadTimeout = 1000 * 60 * 2;
    private KeepAliveTimeout keepAliveTimeout = new KeepAliveTimeout();
    private String logTimeFormat;

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getAsyncConnectTimeout() {
        return asyncConnectTimeout;
    }

    public void setAsyncConnectTimeout(int asyncConnectTimeout) {
        this.asyncConnectTimeout = asyncConnectTimeout;
    }

    public int getAsyncReadTimeout() {
        return asyncReadTimeout;
    }

    public void setAsyncReadTimeout(int asyncReadTimeout) {
        this.asyncReadTimeout = asyncReadTimeout;
    }

    public KeepAliveTimeout getKeepAliveTimeout() {
        return keepAliveTimeout;
    }

    public void setKeepAliveTimeout(KeepAliveTimeout keepAliveTimeout) {
        this.keepAliveTimeout = keepAliveTimeout;
    }

    public String getLogTimeFormat() {
        return logTimeFormat;
    }

    public void setLogTimeFormat(String logTimeFormat) {
        this.logTimeFormat = logTimeFormat;
    }

    public DateTimeFormatter getLogTimeFormatter() {
        if (!StringUtils.isEmpty(logTimeFormat) && null == logTimeFormatter) {
            synchronized (CommonRestProperties.class) {
                if (null == logTimeFormatter) {
                    logTimeFormatter = DateTimeFormatter.ofPattern(logTimeFormat);
                }
            }
        }

        return logTimeFormatter;
    }

    /**
     * 指定Http的keep alive 超时时间，都为毫秒
     */
    public class KeepAliveTimeout {
        /**
         * 默认的超时时间，默认29分钟(一般服务器设置的为30分钟)
         */
        private long def = 104400000;
        /**
         * 特定域名的超时时间，key为域名，value为超时时间。比如：www.baidu.com=10000，即指定百度的访问超时为10s
         */
        private Map<String, Long> specifics = new HashMap<>();
        /**
         * 是否禁用服务器指定的超时时间
         */
        private Boolean disableServer = false;

        public long getDef() {
            return def;
        }

        public void setDef(long def) {
            this.def = def;
        }

        public Map<String, Long> getSpecifics() {
            return specifics;
        }

        public void setSpecifics(Map<String, Long> specifics) {
            this.specifics = specifics;
        }

        public Boolean getDisableServer() {
            return disableServer;
        }

        public void setDisableServer(Boolean disableServer) {
            this.disableServer = disableServer;
        }
    }
}
