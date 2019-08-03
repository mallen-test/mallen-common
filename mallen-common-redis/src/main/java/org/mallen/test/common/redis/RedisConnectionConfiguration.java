package org.mallen.test.common.redis;

import org.springframework.data.redis.connection.*;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mallen
 * @date 11/23/18
 */
public class RedisConnectionConfiguration {
    @Resource
    protected MallenRedisProperties redisProperties;

    /**
     * 获取集群模式的配置
     *
     * @param connectionInfo
     * @return
     */
    protected final RedisClusterConfiguration getClusterConfiguration(ConnectionInfo connectionInfo) {
        RedisClusterConfiguration config = new RedisClusterConfiguration();
        config.setClusterNodes(connectionInfo.getRedisNodes());

        if (connectionInfo.getPassword() != null) {
            config.setPassword(RedisPassword.of(connectionInfo.getPassword()));
        }
        if (redisProperties.getMaxRedirects() != null) {
            config.setMaxRedirects(redisProperties.getMaxRedirects());
        }
        return config;
    }

    /**
     * 获取哨兵模式的配置
     *
     * @param connectionInfo
     * @return
     */
    protected final RedisSentinelConfiguration getSentinelConfig(ConnectionInfo connectionInfo) {
        RedisSentinelConfiguration config = new RedisSentinelConfiguration();
        config.master(redisProperties.getSentinelMaster());
        config.setSentinels(connectionInfo.getRedisNodes());
        if (connectionInfo.getPassword() != null) {
            config.setPassword(connectionInfo.getPassword());
        }
        config.setDatabase(redisProperties.getDatabase());
        return config;
    }

    /**
     * 获取单节点模式的配置
     *
     * @param connectionInfo
     * @return
     */
    protected RedisStandaloneConfiguration getStandaloneConfiguration(ConnectionInfo connectionInfo) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        if (connectionInfo.getPassword() != null) {
            config.setPassword(connectionInfo.getPassword());
        }
        RedisNode redisNode = connectionInfo.redisNodes.get(0);
        config.setHostName(redisNode.getHost());
        config.setPort(redisNode.getPort());
        config.setDatabase(redisProperties.getDatabase());

        return config;
    }

    /**
     * 根据MallenRedisProperties配置的url解析出连接信息
     *
     * @return
     */
    protected ConnectionInfo getConnectionInfo() {
        if (!StringUtils.hasText(redisProperties.getUrl())) {
            throw new RuntimeException("使用MallenRedisConfiguration时必须存在配置项mallen.redis.url");
        }
        return parseUrl(redisProperties.getUrl());
    }

    /**
     * 解析url信息，url的格式必须为：
     * {standalone,sentinel,cluster,standalones,sentinels,clusters}://[password@]host:port[,host2:port2]
     *
     * @param url
     * @return
     */
    protected ConnectionInfo parseUrl(String url) {
        int protocolIndex = url.indexOf("://");
        if (-1 == protocolIndex) {
            throw new RuntimeException("mallen.redis.url格式不正确，正确的格式为{standalone,sentinel,cluster,standalones,sentinels,clusters}://[password@]host:port[,host2:port2]");
        }
        // 获取协议头
        String protocol = url.substring(0, protocolIndex);
        RedisMode redisMode = RedisMode.ofName(protocol);
        if (redisMode == null) {
            throw new RuntimeException("mallen.redis.url必须包含协议头，正确的格式为{standalone,sentinel,cluster,standalones,sentinels,clusters}://[password@]host:port[,host2:port2]");
        }
        // 获取密码
        String password = null;
        String nodes;
        int passwordIndex = url.lastIndexOf("@");
        if (-1 != passwordIndex) {
            password = url.substring(protocolIndex + 3, passwordIndex);
            // 获取host:port
            nodes = url.substring(passwordIndex + 1);
        } else {
            nodes = url.substring(protocolIndex + 3);
        }
        if (!StringUtils.hasText(nodes)) {
            throw new RuntimeException("mallen.redis.url必须包含host和port， 正确的格式为{standalone,sentinel,cluster,standalones,sentinels,clusters}://[password@]host:port[,host2:port2]");
        }
        return new ConnectionInfo(redisMode, password, nodes);
    }

    protected static class ConnectionInfo {
        /**
         * 连接模式
         */
        private RedisMode mode;
        /**
         * 是否需要ssl支持
         */
        private Boolean isSsl;
        private String password;
        /**
         * 原始的节点信息，格式为host:port[,host:post]...
         */
        private String nodes;
        /**
         * 根据nodes解析而来的RedisNode集合
         */
        private List<RedisNode> redisNodes;

        public ConnectionInfo(RedisMode mode, String password, String nodes) {
            this.mode = mode;
            this.password = password;
            this.nodes = nodes;
            if (mode.equals(RedisMode.STANDALONES) || mode.equals(RedisMode.SENTINELS) || mode.equals(MallenLettuceAutoConfiguration.RedisMode.CLUSTERS)) {
                this.isSsl = true;
            } else {
                this.isSsl = false;
            }
            // 解析node信息
            String[] nodesSplit = nodes.split(",");
            redisNodes = new ArrayList<>(nodesSplit.length);
            for (String node : nodesSplit) {
                String[] nodeSplit = node.split(":");
                if (nodeSplit.length < 2) {
                    throw new RuntimeException("mallen.redis.url中host:port的格式配置错误，正确的格式为{standalone,sentinel,cluster,standalones,sentinels,clusters}://[password@]host:port[,host2:port2]");
                }
                redisNodes.add(new RedisNode(nodeSplit[0], Integer.valueOf(nodeSplit[1])));
            }
        }

        public RedisMode getMode() {
            return mode;
        }

        public void setMode(RedisMode mode) {
            this.mode = mode;
        }

        public Boolean getSsl() {
            return isSsl;
        }

        public void setSsl(Boolean ssl) {
            isSsl = ssl;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getNodes() {
            return nodes;
        }

        public void setNodes(String nodes) {
            this.nodes = nodes;
        }

        public List<RedisNode> getRedisNodes() {
            return redisNodes;
        }

        public void setRedisNodes(List<RedisNode> redisNodes) {
            this.redisNodes = redisNodes;
        }
    }

    protected enum RedisMode {
        STANDALONE("standalone"), SENTINEL("sentinel"), CLUSTER("cluster"),
        STANDALONES("standalones"), SENTINELS("sentinels"), CLUSTERS("clusters");
        private String name;

        RedisMode(String mode) {
            this.name = mode;
        }

        public static RedisMode ofName(String name) {
            for (RedisMode mode : RedisMode.values()) {
                if (mode.name.equals(name)) {
                    return mode;
                }
            }

            return null;
        }
    }
}
