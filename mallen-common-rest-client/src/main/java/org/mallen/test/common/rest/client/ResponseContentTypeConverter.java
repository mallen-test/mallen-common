package org.mallen.test.common.rest.client;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mallen
 * @date 11/25/19
 */
public class ResponseContentTypeConverter implements ClientHttpRequestInterceptor {
    private Map<String, String> hostMap;
    private Map<String, String> hostPathMap;
    private boolean covertAll;
    private String convertAllType;

    public ResponseContentTypeConverter(List<CommonRestProperties.ResponseTypeConverter> converters) {
        this.hostMap = new HashMap<>(1, 1);
        this.hostPathMap = new HashMap<>(1, 1);
        converters.forEach(c -> {
            String key = c.getKey();
            switch (c.getType()) {
                case ALL:
                    this.covertAll = true;
                    this.convertAllType = c.getTarget();
                    this.hostMap = null;
                    this.hostPathMap = null;
                    break;
                case HOST:
                    if (key.endsWith("/")) {
                        key = key.substring(0, key.length() - 1);
                    }
                    this.hostMap.put(key, c.getTarget());
                    break;
                case HOST_PATH:
                    if (key.endsWith("/")) {
                        key = key.substring(0, key.length() - 1);
                    }
                    this.hostPathMap.put(key, c.getTarget());
                    break;
                default:
                    break;
            }
        });
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        ClientHttpResponse response = execution.execute(request, body);
        convertContentType(request, response);
        return response;
    }

    private void convertContentType(HttpRequest request, ClientHttpResponse response) {
        if (covertAll) {
            response.getHeaders().set(HttpHeaders.CONTENT_TYPE, convertAllType);
            return ;
        }
        // 获取请求标识
        URI uri = request.getURI();
        StringBuilder sb = new StringBuilder(uri.getScheme()).append("://")
                .append(uri.getHost());
        if (uri.getPort() > 0) {
            sb.append(":").append(uri.getPort());
        }
        String host = sb.toString();
        sb.append(uri.getPath());
        String hostPath = sb.toString();
        // 查询转换
        String targetType = hostMap.get(host);
        if (StringUtils.isEmpty(targetType)) {
            targetType = hostPathMap.get(hostPath);
        }
        // 执行替换
        if (!StringUtils.isEmpty(targetType)) {
            response.getHeaders().set(HttpHeaders.CONTENT_TYPE, targetType);
        }
    }
}
