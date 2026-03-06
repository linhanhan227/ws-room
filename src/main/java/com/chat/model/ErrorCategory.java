package com.chat.model;

public enum ErrorCategory {

    BUSINESS_ERROR("BUSINESS", "业务错误"),
    SYSTEM_ERROR("SYSTEM", "系统错误"),
    PARAMETER_ERROR("PARAMETER", "参数错误"),
    AUTHENTICATION_ERROR("AUTH", "认证错误"),
    AUTHORIZATION_ERROR("PERMISSION", "授权错误"),
    NOT_FOUND_ERROR("NOT_FOUND", "资源不存在"),
    VALIDATION_ERROR("VALIDATION", "验证错误"),
    RATE_LIMIT_ERROR("RATE_LIMIT", "限流错误");

    private final String code;
    private final String description;

    ErrorCategory(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}