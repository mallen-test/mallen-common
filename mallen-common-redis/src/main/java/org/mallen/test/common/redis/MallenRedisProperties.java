package org.mallen.test.common.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 使用说明参见{@link MallenLettuceAutoConfiguration}
 *
 * @author mallen
 * @date 11/21/18
 */
@ConfigurationProperties(prefix = "mallen.redis")
public class MallenRedisProperties {
    /**
     * Database index used by the connection factory.
     * 如果是集群模式，将会忽略该配置
     */
    private int database = 0;

    /**
     * Connection URL.
     * 格式为：{standalone,sentinel,cluster,standalones,sentinels,clusters}://[password@]host:port[,host2:port2]
     * standalone 单机模式，sentinel 哨兵模式，cluster 集群模式；各个模式后加s表示使用SSL/TSL。
     * 如果是sentinel模式，并且有多个slave，请将master的host:port配置为第一个。
     */
    private String url;

    /**
     * 指定超时时间，默认60s
     */
    private Duration timeout;

    /**
     * 当使用sentinel模式时，指定为master节点的名字
     */
    private String sentinelMaster;

    /**
     * Shutdown timeout.默认值100毫秒
     */
    private Duration shutdownTimeout = Duration.ofMillis(100);

    /**
     * Maximum number of redirects to follow when executing commands across the
     * cluster.
     */
    private Integer maxRedirects;


    /**
     * 连接池信息，对于lettuce来说，使用连接池与否不会对执行性能产生明显的影响，建议一般不使用。
     * 如果需要使用连接池，请引入commons-pool2
     */
    private Pool pool;

    /**
     * redis的prefix_key，默认可以不填此项
     */
    private String prefixKey;

    public int getDatabase() {
        return this.database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public Duration getTimeout() {
        return this.timeout;
    }

    public String getSentinelMaster() {
        return sentinelMaster;
    }

    public void setSentinelMaster(String sentinelMaster) {
        this.sentinelMaster = sentinelMaster;
    }

    public Duration getShutdownTimeout() {
        return shutdownTimeout;
    }

    public void setShutdownTimeout(Duration shutdownTimeout) {
        this.shutdownTimeout = shutdownTimeout;
    }

    public Integer getMaxRedirects() {
        return maxRedirects;
    }

    public void setMaxRedirects(Integer maxRedirects) {
        this.maxRedirects = maxRedirects;
    }

    public Pool getPool() {
        return pool;
    }

    public void setPool(Pool pool) {
        this.pool = pool;
    }

    public String getPrefixKey() {
        return prefixKey;
    }

    public void setPrefixKey(String prefixKey) {
        this.prefixKey = prefixKey;
    }

    /**
     * Pool properties.
     */
    public static class Pool {

        /**
         * Maximum number of "idle" connections in the pool. Use a negative value to
         * indicate an unlimited number of idle connections.
         */
        private int maxIdle = 8;

        /**
         * Target for the minimum number of idle connections to maintain in the pool. This
         * setting only has an effect if it is positive.
         */
        private int minIdle = 0;

        /**
         * Maximum number of connections that can be allocated by the pool at a given
         * time. Use a negative value for no limit.
         */
        private int maxActive = 8;

        /**
         * Maximum amount of time a connection allocation should block before throwing an
         * exception when the pool is exhausted. Use a negative value to block
         * indefinitely.
         */
        private Duration maxWait = Duration.ofMillis(-1);

        public int getMaxIdle() {
            return this.maxIdle;
        }

        public void setMaxIdle(int maxIdle) {
            this.maxIdle = maxIdle;
        }

        public int getMinIdle() {
            return this.minIdle;
        }

        public void setMinIdle(int minIdle) {
            this.minIdle = minIdle;
        }

        public int getMaxActive() {
            return this.maxActive;
        }

        public void setMaxActive(int maxActive) {
            this.maxActive = maxActive;
        }

        public Duration getMaxWait() {
            return this.maxWait;
        }

        public void setMaxWait(Duration maxWait) {
            this.maxWait = maxWait;
        }

    }
}
