package org.mallen.test.common.rest.client;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.util.PublicSuffixMatcher;
import org.apache.http.conn.util.PublicSuffixMatcherLoader;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.TextUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * {@link CommonRest}的自动注入配置。
 *
 * @author mallen
 * @date 8/13/19
 */
@Configuration
@EnableConfigurationProperties({CommonRestProperties.class})
@ConditionalOnClass({RestTemplate.class, HttpClient.class})
public class CommonRestConfiguration {

    private final CommonRestProperties commonRestProperties;

    public CommonRestConfiguration(CommonRestProperties properties) {
        this.commonRestProperties = properties;
    }

    @Bean
    public RestTemplate restTemplate() {
        return createRestTemplate();
    }

    private RestTemplate createRestTemplate() {
        HttpComponentsClientHttpRequestFactory requestFactory = new CustomerHttpComponentsClientHttpRequestFactory();
        requestFactory.setConnectTimeout(commonRestProperties.getConnectTimeout());
        requestFactory.setReadTimeout(commonRestProperties.getReadTimeout());
        // 设置从线程池中获取连接的超时时间
        if (null != commonRestProperties.getConnPool().getConnectionRequestTimeout()) {
            requestFactory.setConnectionRequestTimeout(commonRestProperties.getConnPool().getConnectionRequestTimeout());
        }
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        addInterceptors(restTemplate);
        restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        restTemplate.getMessageConverters().add(new FormHttpMessageConverter());

        return restTemplate;
    }

    /**
     * 添加RestTemplate的拦截器
     *
     * @param restTemplate
     */
    private void addInterceptors(RestTemplate restTemplate) {
        List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
        // response type转换器
        if (!CollectionUtils.isEmpty(commonRestProperties.getResponseTypeConverters())) {
            interceptors.add(new ResponseContentTypeConverter(commonRestProperties.getResponseTypeConverters()));
        }
    }

    public class CustomerHttpComponentsClientHttpRequestFactory extends HttpComponentsClientHttpRequestFactory {

        public CustomerHttpComponentsClientHttpRequestFactory() {
            HttpClientBuilder builder = HttpClients.custom().useSystemProperties();
            // http keep alive timeout
            httpKeepAliveTimeout(builder);
            // 自定义连接池
            connectionManager(builder);

            super.setHttpClient(builder.build());
        }

        /**
         * 拷贝HttpClientBuilder的默认实现，并增加自定义连接池属性支持
         *
         * @param builder
         */
        private void connectionManager(HttpClientBuilder builder) {
            boolean systemProperties = true;
            // sslSocketFactoryCopy的创建与builder中的默认值保持一致
            PublicSuffixMatcher publicSuffixMatcherCopy = PublicSuffixMatcherLoader.getDefault();
            LayeredConnectionSocketFactory sslSocketFactoryCopy = null;
            if (sslSocketFactoryCopy == null) {
                final String[] supportedProtocols = systemProperties ? split(
                        System.getProperty("https.protocols")) : null;
                final String[] supportedCipherSuites = systemProperties ? split(
                        System.getProperty("https.cipherSuites")) : null;
                HostnameVerifier hostnameVerifierCopy = null;
                if (hostnameVerifierCopy == null) {
                    hostnameVerifierCopy = new DefaultHostnameVerifier(publicSuffixMatcherCopy);
                }

                if (systemProperties) {
                    sslSocketFactoryCopy = new SSLConnectionSocketFactory(
                            (SSLSocketFactory) SSLSocketFactory.getDefault(),
                            supportedProtocols, supportedCipherSuites, hostnameVerifierCopy);
                } else {
                    sslSocketFactoryCopy = new SSLConnectionSocketFactory(
                            SSLContexts.createDefault(),
                            hostnameVerifierCopy);
                }
            }
            // 连接池设置
            PoolingHttpClientConnectionManager poolingmgr = new PoolingHttpClientConnectionManager(
                    RegistryBuilder.<ConnectionSocketFactory>create()
                            .register("http", PlainConnectionSocketFactory.getSocketFactory())
                            .register("https", sslSocketFactoryCopy)
                            .build(),
                    null,
                    null,
                    null,
                    -1,
                    TimeUnit.MILLISECONDS);
            if (systemProperties) {
                // 默认开启http keepAlive
                String s = System.getProperty("http.keepAlive", "true");
                if ("true".equalsIgnoreCase(s)) {
                    s = System.getProperty("http.maxConnections", "5");
                    final int max = Integer.parseInt(s);
                    poolingmgr.setDefaultMaxPerRoute(max);
                    poolingmgr.setMaxTotal(2 * max);
                }
            }
            if (commonRestProperties.getConnPool().getMaxConnTotal() != null && commonRestProperties.getConnPool().getMaxConnTotal() > 0) {
                poolingmgr.setMaxTotal(commonRestProperties.getConnPool().getMaxConnTotal());
            }
            if (commonRestProperties.getConnPool().getDefaultConnPerRoute() != null && commonRestProperties.getConnPool().getDefaultConnPerRoute() > 0) {
                poolingmgr.setDefaultMaxPerRoute(commonRestProperties.getConnPool().getDefaultConnPerRoute());
            }
            // 特定域名的连接数量
            if (!CollectionUtils.isEmpty(commonRestProperties.getConnPool().getRouteConn())) {
                int specCount = 0;
                for (CommonRestProperties.RouteConnection route : commonRestProperties.getConnPool().getRouteConn()) {
                    if (StringUtils.isEmpty(route.getHost()) || route.getMax() == null || route.getMax() < 1) {
                        throw new RuntimeException("routeConn配置不正确，host必须存在，route的值必须存在且大于0");
                    }
                    String[] hostParts = route.getHost().split("://");
                    if (hostParts.length != 2) {
                        throw new RuntimeException("host配置错误，合法格式为：schema://域名或ip。比如：https://baidu.com，http://baidu.com，http://192.168.1.1");
                    }

                    HttpHost httpHost;
                    if (route.getPort() != null) {
                        httpHost = new HttpHost(hostParts[1], route.getPort(), hostParts[0]);
                    } else {
                        httpHost = new HttpHost(hostParts[1], -1, hostParts[0]);
                    }

                    boolean secure = false;
                    if ("https".equalsIgnoreCase(hostParts[0])) {
                        secure = true;
                    }
                    poolingmgr.setMaxPerRoute(new HttpRoute(httpHost, null, secure), route.getMax());
                    specCount += route.getMax();
                }
                if (poolingmgr.getMaxTotal() < specCount) {
                    throw new RuntimeException("连接池的最大连接数量需要比指定的连接数量大，请配置：tima.rest.conn-pool.max-conn-total到合理值");
                }
            }

            builder.setConnectionManager(poolingmgr);
        }

        /**
         * 自定义keep alive timeout策略
         *
         * @param builder
         */
        private void httpKeepAliveTimeout(HttpClientBuilder builder) {
            ConnectionKeepAliveStrategy myStrategy = (response, context) -> {
                // 优先使用服务器指定的keep alive时间
                if (!commonRestProperties.getKeepAliveTimeout().getDisableServer()) {
                    HeaderElementIterator it = new BasicHeaderElementIterator(
                            response.headerIterator(HTTP.CONN_KEEP_ALIVE));
                    while (it.hasNext()) {
                        HeaderElement he = it.nextElement();
                        String param = he.getName();
                        String value = he.getValue();
                        if (value != null && param.equalsIgnoreCase("timeout")) {
                            try {
                                return Long.parseLong(value) * 1000;
                            } catch (NumberFormatException ignore) {
                            }
                        }
                    }
                }
                // 是否特定域名时间
                if (!CollectionUtils.isEmpty(commonRestProperties.getKeepAliveTimeout().getSpecifics())) {
                    HttpHost target = (HttpHost) context.getAttribute(
                            HttpClientContext.HTTP_TARGET_HOST);
                    for (String key : commonRestProperties.getKeepAliveTimeout().getSpecifics().keySet()) {
                        if (key.equalsIgnoreCase(target.getHostName())) {
                            return commonRestProperties.getKeepAliveTimeout().getSpecifics().get(key);
                        }
                    }
                }
                // 其他情况返回默认时间
                return commonRestProperties.getKeepAliveTimeout().getDef();
            };
            builder.setKeepAliveStrategy(myStrategy);
        }
    }

    private String[] split(final String s) {
        if (TextUtils.isBlank(s)) {
            return null;
        }
        return s.split(" *, *");
    }
}

