package org.mallen.test.common.token;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 拦截请求header中的token数据，保存到ThreadLocal中
 * @author mallen
 * @date 2020/10/22
 */
public class TokenInterceptor extends HandlerInterceptorAdapter {
    private static final Logger logger = LoggerFactory.getLogger(TokenInterceptor.class);
    public static final String TOKEN_HEADER_NAME = "internal-token";

    private static final ObjectMapper objectMapper;


    static {
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            String tokenJson = request.getHeader(TOKEN_HEADER_NAME);
            if (StringUtils.isEmpty(tokenJson)) {
                return true;
            }
            Token token = objectMapper.readValue(tokenJson, Token.class);
            TokenUtil.setToken(token);
        } catch (Exception ex) {
            logger.error("解析header中的token失败", ex);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        TokenUtil.removeToken();
        super.afterCompletion(request, response, handler, ex);
    }
}
