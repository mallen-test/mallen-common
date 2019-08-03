package org.mallen.test.common.exception;

import org.mallen.test.common.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.*;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * spring mvc 提供的全局异常拦截只支持controller中抛出异常的情况，但是如果进入不了controller，比如访问一个不存在的地址，springmvc不能处理。
 * 而spring提供了应对机制，同样基于springmvc来实现。
 * 当存在不存在地址时，spring会将请求发送到${server.error.path:${error.path:/error}}（默认为/error）这个contrller进行拦截，用户可以自定义异常信息。
 * Created by mallen on 10/18/17.
 */
@RestController
@RequestMapping("${server.error.path:${error.path:/error}}")
public class SpringBootErrorController implements ErrorController {
    @Autowired
    private ServerProperties serverProperties;

    @Override
    public String getErrorPath() {
        return this.serverProperties.getError().getPath();
    }

    @RequestMapping
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        HttpStatus status = getStatus(request);
        Response errorBody = new Response();
        errorBody.setStatus(Response.Status.FAILED);
        errorBody.setErrorCode("SYS." + String.valueOf(status.value()));
        errorBody.setErrorMessage(status.getReasonPhrase());

        return new ResponseEntity(errorBody, status);
    }

    protected HttpStatus getStatus(HttpServletRequest request) {
        Integer statusCode = (Integer) request
                .getAttribute("javax.servlet.error.status_code");
        if (statusCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        try {
            return HttpStatus.valueOf(statusCode);
        }
        catch (Exception ex) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
