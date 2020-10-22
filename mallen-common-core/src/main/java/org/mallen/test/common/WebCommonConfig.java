package org.mallen.test.common;

import org.mallen.test.common.exception.GlobalExceptionHandler;
import org.mallen.test.common.exception.SpringBootErrorController;
import org.mallen.test.common.log.ReqRespLoggingFilter;
import org.mallen.test.common.log.ReqRespLoggingProperties;
import org.mallen.test.common.token.TokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by mallen on 1/24/18.
 */
@Configuration
@EnableConfigurationProperties(ReqRespLoggingProperties.class)
public class WebCommonConfig implements WebMvcConfigurer {
    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @Bean
    public SpringBootErrorController springBootErrorController() {
        return new SpringBootErrorController();
    }

    @Bean
    public ReqRespLoggingFilter loggingFilter(@Autowired ReqRespLoggingProperties reqRespLoggingProperties) {
        return new ReqRespLoggingFilter(reqRespLoggingProperties);
    }

    @Bean
    public TokenInterceptor tokenInterceptor() {
        return new TokenInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor()).addPathPatterns("/**");
    }
}
