package org.mallen.test.common.rest.client;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 针对{@code CommonRest}类的配置项目定义
 *
 * @author mallen
 * @date 2019/08/14
 */
@ConfigurationProperties(prefix = "mallen.rest")
public class CommonRestProperties {
    private static volatile DateTimeFormatter logTimeFormatter = null;
    private int connectTimeout = 1000 * 5;
    private int readTimeout = 1000 * 60;
    private int asyncConnectTimeout = 1000 * 5;
    private int asyncReadTimeout = 1000 * 60;
    private KeepAliveTimeout keepAliveTimeout = new KeepAliveTimeout();
    private ConnectionPool connPool = new ConnectionPool();
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

    public ConnectionPool getConnPool() {
        return connPool;
    }

    public void setConnPool(ConnectionPool connPool) {
        this.connPool = connPool;
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
         * 默认的超时时间，单位：毫秒，默认59秒(tomcat默认为60秒)
         */
        private long def = 55000;
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

    /**
     * 指定http client的连接池属性，如果不设置：
     * defaultConnPerRoute默认为5，maxConnTotal默认为5*2
     */
    public static class ConnectionPool {
        /**
         * 连接池最大连接数量。注意：该值需要大于等于defaultConnPerRoute和routeConn中的所有连接数量
         * apache http client默认值10
         */
        private Integer maxConnTotal;
        /**
         * 每个域名默认的最大连接数量，apache http client默认值5
         */
        private Integer defaultConnPerRoute;
        /**
         * 特定域名的最大连接数量，apache http client默认不配值。事例：
         * tima:
         * rest:
         * conn-pool:
         * route-conn:
         * - host: http://www.baidu.com
         * max: 20
         * - host: https://www.baidu.com
         * max: 20
         */
        private List<RouteConnection> routeConn;
        /**
         * 从连接池中获取连接的超时时间。apache http client默认不超时（死等！）。单位：毫秒
         */
        private Integer connectionRequestTimeout = 10000;

        public Integer getMaxConnTotal() {
            return maxConnTotal;
        }

        public void setMaxConnTotal(Integer maxConnTotal) {
            this.maxConnTotal = maxConnTotal;
        }

        public Integer getDefaultConnPerRoute() {
            return defaultConnPerRoute;
        }

        public void setDefaultConnPerRoute(Integer defaultConnPerRoute) {
            this.defaultConnPerRoute = defaultConnPerRoute;
        }

        public List<RouteConnection> getRouteConn() {
            return routeConn;
        }

        public void setRouteConn(List<RouteConnection> routeConn) {
            this.routeConn = routeConn;
        }

        public Integer getConnectionRequestTimeout() {
            return connectionRequestTimeout;
        }

        public void setConnectionRequestTimeout(Integer connectionRequestTimeout) {
            this.connectionRequestTimeout = connectionRequestTimeout;
        }

        @Override
        public String toString() {
            return "ConnectionPool{" +
                    "maxConnTotal=" + maxConnTotal +
                    ", defaultConnPerRoute=" + defaultConnPerRoute +
                    ", routeConn=" + routeConn +
                    ", connectionRequestTimeout=" + connectionRequestTimeout +
                    '}';
        }
    }

    /**
     * 特定地址的连接数量
     */
    public static class RouteConnection {
        /**
         * 格式为：schema://域名或ip，比如：https://baidu.com，http://baidu.com，http://192.168.1.1
         */
        private String host;
        /**
         * 端口，如果不指定，则使用协议默认的端口。比如：http为80、https为443
         */
        private Integer port;
        /**
         * 最大连接数量
         */
        private Integer max;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public Integer getMax() {
            return max;
        }

        public void setMax(Integer max) {
            this.max = max;
        }

        @Override
        public String toString() {
            return "Route{" +
                    "host='" + host + '\'' +
                    ", port=" + port +
                    ", max=" + max +
                    '}';
        }
    }
}
