package org.mallen.test.common.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;

/**
 * <h1>为什么需要该类</h1>
 * 其实Spring Boot支持的所有方式的redis自动配置，用户只需要配置相应的配置项即可，详细配置项可参见{@link org.springframework.boot.autoconfigure.data.redis.RedisProperties}。
 * 但是，如果需要在多个模式之间切换，就需要使用不同的配置项，导致代码配置项会由于redis的部署方式而修改。
 * 比如：
 * <ul><li>如果redis服务器为单节点模式，需要配置spring.redis.host、spring.redis.port、spring.redis.password；</li>
 * <li>如果redis服务器为cluster模式，需要配置spring.redis.password、spring.redis.cluster.nodes、spring.redis.cluster.maxRedirects等</li></ul>
 * 这种差别巨大的配置，对于不熟悉Spring Boot的运维人员而言，特别不方便。
 *
 * <h1>如何改进</h1>
 * 该类引入格式化url，约定url的各个部分，通过解析url自动初始化对应连接模式。运维人员只需按照约定，修改url的配置即可完成连接不同模式的服务器的配置。<br/>
 * <h2>url格式说明</h2>
 * url的格式为：
 * <pre class="code">
 *    {standalone,sentinel,cluster,standalones,sentinels,clusters}://[password@]host:port[,host2:port2]
 * </pre>
 * 其中，
 * <ul><li>
 * standalone，sentinel，cluster分别代表三种不同的服务器模式，而在其后添加s表示使用SSL/TSL；
 * </li><li>
 * password为密码信息，如果服务器使用了密码，则需要配置该项。需要注意的是，当为cluster模式时，每个cluster服务器节点的密码必须同时设置为同一个值；
 * </li><li>
 * host:port为节点信息，多个节点采用逗号分隔。
 * </li></ul>
 * <h1>配置事例</h1>
 * <ul><li>连接单节点服务器：</li>
 * mallen.redis.url=standalone://password@localhost:6379
 * <li>连接哨兵模式服务器：</li>
 * mallen.redis.url=sentinel://password@localhost:6379,localhost:6378,localhost:6377<br/>
 * mallen.redis.sentinelMaster=mymaster
 * <br/>需要注意的是，对于哨兵模式，url中的服务器节点为哨兵节点，而不是redis服务器节点。而sentinelMaster需要填入master节点的名称
 * <li>连接集群模式服务器：</li>
 * mallen.redis.url=cluster://password@localhost:16379,localhost:16378,localhost:16377
 * </ul>
 * 更多配置请参见：{@link MallenRedisProperties}
 * <h1>代码事例</h1>
 * 模块使用了Spring Boot的auto configuration功能，只需要引入mallen-common-redis包即可，配置类会解析url并创建{@link RedisConnectionFactory}。
 * 另外，由于经常使用访问string类型数据的RedisTemplate，该配置类还默认初始化了一个，在使用时直接注入StringRedisRemplate即可。事例代码如下：
 * <pre class="code">
 * {@literal @}Import(MallenRedisConfiguration.class)
 * public class AppConfig{
 *      {@literal @}Resource
 *      private StringRedisTemplate stringRedisTemplate;
 *      // 其他代码
 *      ....
 * }
 * </pre>
 *
 * @author mallen
 * @date 11/21/18
 */
@Configuration
@EnableConfigurationProperties(MallenRedisProperties.class)
@ConditionalOnClass({RedisTemplate.class, RedisClient.class})
public class MallenLettuceAutoConfiguration extends RedisConnectionConfiguration {
    @Resource
    ClientResources clientResources;

    /**
     * 用于Redis repository
     * @param redisConnectionFactory
     * @return
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

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    public DefaultClientResources lettuceClientResources() {
        return DefaultClientResources.create();
    }

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        ConnectionInfo connectionInfo = super.getConnectionInfo();
        if (connectionInfo.getMode().equals(RedisMode.CLUSTER) || connectionInfo.getMode().equals(RedisMode.CLUSTERS)) {
            return createClusterConnectionFactory(connectionInfo);
        } else if (connectionInfo.getMode().equals(RedisMode.SENTINEL) || connectionInfo.getMode().equals(RedisMode.SENTINELS)) {
            return createSentinelConnectionFactory(connectionInfo);
        } else {
            return createStandaloneConnectionFactory(connectionInfo);
        }
    }

    /**
     * 创建cluster模式的连接工厂
     *
     * @param connectionInfo
     * @return
     */
    private LettuceConnectionFactory createClusterConnectionFactory(ConnectionInfo connectionInfo) {
        LettuceClientConfiguration clientConfig = getLettuceClientConfiguration(
                clientResources, redisProperties.getPool(), connectionInfo);

        return new LettuceConnectionFactory(getClusterConfiguration(connectionInfo), clientConfig);
    }

    /**
     * 创建sentinel模式的连接工厂
     *
     * @param connectionInfo
     * @return
     */
    private LettuceConnectionFactory createSentinelConnectionFactory(ConnectionInfo connectionInfo) {
        LettuceClientConfiguration clientConfig = getLettuceClientConfiguration(
                clientResources, redisProperties.getPool(), connectionInfo);
        return new LettuceConnectionFactory(getSentinelConfig(connectionInfo), clientConfig);
    }

    /**
     * 创建单节点模式的连接工厂
     *
     * @param connectionInfo
     * @return
     */
    private LettuceConnectionFactory createStandaloneConnectionFactory(ConnectionInfo connectionInfo) {
        LettuceClientConfiguration clientConfig = getLettuceClientConfiguration(
                clientResources, redisProperties.getPool(), connectionInfo);

        RedisStandaloneConfiguration config = getStandaloneConfiguration(connectionInfo);

        return new LettuceConnectionFactory(config, clientConfig);
    }


    /**
     * 创建lettuce的客户端配置
     *
     * @param clientResources
     * @param pool
     * @param connectionInfo
     * @return
     */
    private LettuceClientConfiguration getLettuceClientConfiguration(
            ClientResources clientResources, MallenRedisProperties.Pool pool, ConnectionInfo connectionInfo) {
        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder = createBuilder(pool);
        applyProperties(builder, connectionInfo);
        builder.clientResources(clientResources);

        return builder.build();
    }

    private LettuceClientConfiguration.LettuceClientConfigurationBuilder createBuilder(MallenRedisProperties.Pool pool) {
        if (pool == null) {
            return LettuceClientConfiguration.builder();
        }
        return new PoolBuilderFactory().createBuilder(pool);
    }

    private LettuceClientConfiguration.LettuceClientConfigurationBuilder applyProperties(
            LettuceClientConfiguration.LettuceClientConfigurationBuilder builder, ConnectionInfo connectionInfo) {
        if (connectionInfo.getSsl()) {
            builder.useSsl();
        }
        if (redisProperties.getTimeout() != null) {
            builder.commandTimeout(redisProperties.getTimeout());
        }
        if (redisProperties.getShutdownTimeout() != null && !redisProperties.getShutdownTimeout().isZero()) {
            builder.shutdownTimeout(
                    redisProperties.getShutdownTimeout());
        }
        return builder;
    }


    /**
     * Inner class to allow optional commons-pool2 dependency.
     */
    private static class PoolBuilderFactory {

        public LettuceClientConfiguration.LettuceClientConfigurationBuilder createBuilder(MallenRedisProperties.Pool properties) {
            return LettucePoolingClientConfiguration.builder()
                    .poolConfig(getPoolConfig(properties));
        }

        private GenericObjectPoolConfig<?> getPoolConfig(MallenRedisProperties.Pool properties) {
            GenericObjectPoolConfig<?> config = new GenericObjectPoolConfig<>();
            config.setMaxTotal(properties.getMaxActive());
            config.setMaxIdle(properties.getMaxIdle());
            config.setMinIdle(properties.getMinIdle());
            if (properties.getMaxWait() != null) {
                config.setMaxWaitMillis(properties.getMaxWait().toMillis());
            }
            return config;
        }

    }
}
