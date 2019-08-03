package org.mallen.test.common.redis;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;

import java.net.UnknownHostException;
import java.time.Duration;

/**
 * 使用说明参见{@link MallenLettuceAutoConfiguration}
 *
 * @author mallen
 * @date 11/23/18
 */
@Configuration
@EnableConfigurationProperties(MallenRedisProperties.class)
@ConditionalOnClass({JedisConnection.class, RedisOperations.class, Jedis.class})
public class MallenJedisAutoConfiguration extends RedisConnectionConfiguration {

    /**
     * 用于Redis repository
     *
     * @param redisConnectionFactory
     * @return
     * @throws UnknownHostException
     */
    @Bean
    @ConditionalOnMissingBean(name = "redisTemplate")
    public RedisTemplate<Object, Object> redisTemplate(
            RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    @Bean
    @ConditionalOnMissingBean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    @Bean
    @ConditionalOnMissingBean(RedisConnectionFactory.class)
    public JedisConnectionFactory redisConnectionFactory() {
        RedisConnectionConfiguration.ConnectionInfo connectionInfo = super.getConnectionInfo();
        if (connectionInfo.getMode().equals(RedisConnectionConfiguration.RedisMode.CLUSTER) || connectionInfo.getMode().equals(RedisConnectionConfiguration.RedisMode.CLUSTERS)) {
            return createClusterConnectionFactory(connectionInfo);
        } else if (connectionInfo.getMode().equals(RedisConnectionConfiguration.RedisMode.SENTINEL) || connectionInfo.getMode().equals(RedisConnectionConfiguration.RedisMode.SENTINELS)) {
            return createSentinelConnectionFactory(connectionInfo);
        } else {
            return createStandaloneConnectionFactory(connectionInfo);
        }
    }

    private JedisConnectionFactory createClusterConnectionFactory(ConnectionInfo connectionInfo) {
        return new JedisConnectionFactory(getClusterConfiguration(connectionInfo),
                getJedisClientConfiguration(connectionInfo));
    }

    private JedisConnectionFactory createSentinelConnectionFactory(ConnectionInfo connectionInfo) {
        return new JedisConnectionFactory(getSentinelConfig(connectionInfo), getJedisClientConfiguration(connectionInfo));
    }

    private JedisConnectionFactory createStandaloneConnectionFactory(ConnectionInfo connectionInfo) {
        return new JedisConnectionFactory(getStandaloneConfiguration(connectionInfo), getJedisClientConfiguration(connectionInfo));
    }

    private JedisClientConfiguration getJedisClientConfiguration(ConnectionInfo connectionInfo) {
        JedisClientConfiguration.JedisClientConfigurationBuilder builder = applyProperties(
                JedisClientConfiguration.builder(), connectionInfo);
        // 如果没有配置线程池信息，则使用默认线程池配置
        JedisPoolConfig poolConfig = redisProperties.getPool() != null
                ? jedisPoolConfig(redisProperties.getPool()) : new JedisPoolConfig();
        builder.usePooling().poolConfig(poolConfig);

        return builder.build();
    }

    private JedisClientConfiguration.JedisClientConfigurationBuilder applyProperties(
            JedisClientConfiguration.JedisClientConfigurationBuilder builder, ConnectionInfo connectionInfo) {

        if (connectionInfo.getSsl()) {
            builder.useSsl();
        }

        if (redisProperties.getTimeout() != null) {
            Duration timeout = redisProperties.getTimeout();
            builder.readTimeout(timeout).connectTimeout(timeout);
        }

        return builder;
    }

    private JedisPoolConfig jedisPoolConfig(MallenRedisProperties.Pool pool) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(pool.getMaxActive());
        config.setMaxIdle(pool.getMaxIdle());
        config.setMinIdle(pool.getMinIdle());
        if (pool.getMaxWait() != null) {
            config.setMaxWaitMillis(pool.getMaxWait().toMillis());
        }
        return config;
    }
}
