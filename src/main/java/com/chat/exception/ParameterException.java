package com.chat.exception;

import com.chat.model.ErrorCode;

public class ParameterException extends BaseException {

    public ParameterException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ParameterException(ErrorCode errorCode, String details) {
        super(errorCode, details);
    }

    public ParameterException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public ParameterException(ErrorCode errorCode, String details, Throwable cause) {
        super(errorCode, details, cause);
    }
}