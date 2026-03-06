package com.chat.exception;

import com.chat.model.ErrorCode;

public class AuthorizationException extends BaseException {

    public AuthorizationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AuthorizationException(ErrorCode errorCode, String details) {
        super(errorCode, details);
    }

    public AuthorizationException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public AuthorizationException(ErrorCode errorCode, String details, Throwable cause) {
        super(errorCode, details, cause);
    }
}