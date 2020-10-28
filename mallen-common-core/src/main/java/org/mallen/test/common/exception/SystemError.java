package org.mallen.test.common.exception;

/**
 * 通用错误码
 * Created by mallen on 9/12/18
 */
public enum SystemError implements IError {
    SYSTEM_INTERNAL_ERROR("0000", "System Internal Error"),
    TOO_MUCH_REQUEST_DATA("0003", "Too much request data"),
    INVALID_TOKEN("0004", "Invalid token"),
    INVALID_SIGN("0005", "Invalid sign"),
    ACCESS_DENIED("0006", "Access denied"),
    CALL_THIRD_SYSTEM_ERROR("9998", "call third system error"),
    OTHER("9999", "unrecognized error");

    String errorCode;
    String errorMessage;

    SystemError(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    private static final String nameSpace = "SYS";

    @Override
    public String getNamespace() {
        return nameSpace;
    }

    @Override
    public String getErrorCode() {
        return nameSpace + "." + errorCode;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
}
