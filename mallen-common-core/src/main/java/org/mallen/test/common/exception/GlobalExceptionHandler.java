package org.mallen.test.common.exception;

import org.mallen.test.common.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.util.WebUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 如果exception handler没有与controller在一个目录中，需要将该类配置为一个bean（自动扫描，或者声明为bean）
 * Created by mallen on 3/29/17.
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 父类中的handle方法只会处理spring mvc的异常，如果是其他异常，则会交由该方法处理
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(Throwable.class)
    public final ResponseEntity<Object> handleExceptionGlobal(Throwable ex, WebRequest request) throws Exception {
        if (!(ex instanceof BaseBusinessException))
            return super.handleException((Exception) ex, request);
        BaseBusinessException exception = (BaseBusinessException) ex;
        IError error = exception.getError();
        Response errorBody = new Response();
        errorBody.setStatus(Response.Status.FAILED);
        errorBody.setErrorCode(error.getErrorCode());
        errorBody.setErrorMessage(error.getErrorMessage());
        errorBody.setExtMessage(exception.getExtMessage());
        errorBody.setData(exception.getData());

        return new ResponseEntity<>(errorBody, null, HttpStatus.OK);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatus status, WebRequest request) {
        if (HttpStatus.INTERNAL_SERVER_ERROR.equals(status)) {
            request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex, WebRequest.SCOPE_REQUEST);
            LOGGER.error("", ex);
        }
        Response errorBody = new Response();
        errorBody.setStatus(Response.Status.FAILED);
        errorBody.setErrorCode("SYS." + status.value());
        errorBody.setErrorMessage(status.getReasonPhrase());

        return new ResponseEntity<>(errorBody, headers, HttpStatus.OK);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        List<String> details = new ArrayList<>();
        for(ObjectError error : ex.getBindingResult().getAllErrors()) {
            details.add(error.getDefaultMessage());
        }
        Response errorBody = new Response();

        String errMessage = "input parameters invalid:" + details.stream().collect(Collectors.joining(","));
        errorBody.setStatus(Response.Status.FAILED);
        errorBody.setErrorCode("SYS." + String.valueOf(status.value()));
        errorBody.setErrorMessage(status.getReasonPhrase());
        errorBody.setExtMessage(errMessage);

        logger.error(errMessage);

        return new ResponseEntity(errorBody, HttpStatus.OK);
    }
}


