package org.mallen.test.common.dao.rest;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpHost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

/**
 * {@link CommonRest}的自动注入配置。
 *
 * @author mallen
 * @date 8/13/19
 */
@Configuration
@EnableConfigurationProperties({CommonRestProperties.class})
public class CommonRestConfiguration {

    private final CommonRestProperties commonRestProperties;

    public CommonRestConfiguration(CommonRestProperties properties) {
        this.commonRestProperties = properties;
    }

    @Bean
    public RestTemplate restTemplate() {
        return createRestTemplate();
    }

    @LoadBalanced
    @Bean("loadBalancedRestTemplate")
    @ConditionalOnMissingBean(name = "loadBalancedRestTemplate")
    public RestTemplate loadBalancedRestTemplate() {
        return createRestTemplate();
        // 不能使用，不然会导致原本的restTemplate也被LoadBalanced
        //return restTemplate();
    }

    private RestTemplate createRestTemplate() {
        HttpComponentsClientHttpRequestFactory requestFactory = new CustomerHttpComponentsClientHttpRequestFactory();
        requestFactory.setConnectTimeout(commonRestProperties.getConnectTimeout());
        requestFactory.setReadTimeout(commonRestProperties.getReadTimeout());
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        restTemplate.getMessageConverters().add(new FormHttpMessageConverter());

        return restTemplate;
    }

    public class CustomerHttpComponentsClientHttpRequestFactory extends HttpComponentsClientHttpRequestFactory {

        public CustomerHttpComponentsClientHttpRequestFactory() {
            HttpClientBuilder builder = HttpClients.custom().useSystemProperties();
            // http keep alive timeout
            httpKeepAliveTimeout(builder);
            super.setHttpClient(builder.build());
        }

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
}

