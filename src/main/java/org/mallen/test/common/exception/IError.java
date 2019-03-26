package org.mallen.test.common.exception;

public interface IError {
    String getNamespace();

    String getErrorCode();

    String getErrorMessage();
}