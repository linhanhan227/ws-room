package com.chat.exception;

import com.chat.model.ErrorCode;

public class SystemException extends BaseException {

    public SystemException(ErrorCode errorCode) {
        super(errorCode);
    }

    public SystemException(ErrorCode errorCode, String details) {
        super(errorCode, details);
    }

    public SystemException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public SystemException(ErrorCode errorCode, String details, Throwable cause) {
        super(errorCode, details, cause);
    }
}