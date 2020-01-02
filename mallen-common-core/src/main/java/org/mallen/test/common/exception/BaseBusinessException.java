package org.mallen.test.common.exception;

/**
 * 基础业务异常类
 *
 * @author mallen
 * @date 1/14/19
 */
public class BaseBusinessException extends RuntimeException {
    private IError error;
    private String extMessage;

    public BaseBusinessException(IError error) {
        super(error.getErrorCode() + ", " + error.getErrorMessage());
        this.extMessage = null;
        this.error = error;
    }

    public BaseBusinessException(IError error, String extMessage) {
        super(error.getErrorCode() + ", " + error.getErrorMessage() + ", " + extMessage);
        this.extMessage = extMessage;
        this.error = error;
    }

    /**
     * 直接指定异常的错误码，以及错误消息。该构造方法一般只适用于将其他模块的错误码直接转换抛出的场景。自己模块的错误码，请使用枚举进行定义。
     * 并使用构造方法{@link BaseBusinessException#BaseBusinessException(IError)}
     *
     * @param errorCode
     * @param errorMessage
     * @param extMessage
     */
    public BaseBusinessException(String errorCode, String errorMessage, String extMessage) {
        super(errorCode + ", " + errorMessage + ", " + extMessage);
        this.extMessage = extMessage;
        this.error = new IError() {
            @Override
            public String getNamespace() {
                return null;
            }

            @Override
            public String getErrorCode() {
                return errorCode;
            }

            @Override
            public String getErrorMessage() {
                return errorMessage;
            }
        };
    }

    /**
     * 直接指定异常的错误码，以及错误消息。该构造方法一般只适用于将其他模块的错误码直接转换抛出的场景。自己模块的错误码，请使用枚举进行定义。
     * 并使用构造方法{@link BaseBusinessException#BaseBusinessException(IError)}
     *
     * @param namespace
     * @param errorCode
     * @param errorMessage
     * @param extMessage
     */
    public BaseBusinessException(String namespace, String errorCode, String errorMessage, String extMessage) {
        super(namespace + "." + errorCode + ", " + errorMessage + ", " + extMessage);
        this.extMessage = extMessage;
        this.error = new IError() {
            @Override
            public String getNamespace() {
                return namespace;
            }

            @Override
            public String getErrorCode() {
                return errorCode;
            }

            @Override
            public String getErrorMessage() {
                return errorMessage;
            }
        };
    }

    public IError getError() {
        return error;
    }

    public void setError(IError error) {
        this.error = error;
    }

    public String getExtMessage() {
        return extMessage;
    }

    public void setExtMessage(String extMessage) {
        this.extMessage = extMessage;
    }

    @Override
    public String toString() {
        return "ErrorCode : " + this.error.getErrorCode() + ", ErrorMessage : " + this.error.getErrorMessage() + ", ExtMessage : " + this.extMessage;
    }

    public String toJson() {
        return "{\"errorCode\":" + "\"" + this.error.getErrorCode() + "\","
                + "\"errorMessage\":\"" + this.error.getErrorMessage() + "\","
                + "\"extMessage\":\"" + this.extMessage + "\"}";
    }
}