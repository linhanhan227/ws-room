package com.chat.model;

public enum ErrorCode {

    AUTH_TOKEN_MISSING("AUTH_001", "缺少授权令牌", ErrorCategory.AUTHENTICATION_ERROR),
    AUTH_TOKEN_INVALID("AUTH_002", "Token无效或已过期", ErrorCategory.AUTHENTICATION_ERROR),
    AUTH_TOKEN_EXPIRED("AUTH_003", "Token已过期", ErrorCategory.AUTHENTICATION_ERROR),
    AUTH_USERNAME_PASSWORD_ERROR("AUTH_004", "用户名或密码错误", ErrorCategory.AUTHENTICATION_ERROR),
    AUTH_USER_NOT_FOUND("AUTH_005", "用户不存在", ErrorCategory.AUTHENTICATION_ERROR),

    PERMISSION_DENIED("PERM_001", "权限不足，需要管理员权限", ErrorCategory.AUTHORIZATION_ERROR),
    PERMISSION_INSUFFICIENT("PERM_002", "权限不足", ErrorCategory.AUTHORIZATION_ERROR),

    PARAM_MISSING("PARAM_001", "缺少必要参数", ErrorCategory.PARAMETER_ERROR),
    PARAM_INVALID("PARAM_002", "参数格式错误", ErrorCategory.PARAMETER_ERROR),
    PARAM_TYPE_ERROR("PARAM_003", "参数类型错误", ErrorCategory.PARAMETER_ERROR),
    PARAM_OUT_OF_RANGE("PARAM_004", "参数超出范围", ErrorCategory.PARAMETER_ERROR),
    PARAM_EMPTY("PARAM_005", "参数不能为空", ErrorCategory.PARAMETER_ERROR),

    VALIDATION_FAILED("VAL_001", "数据验证失败", ErrorCategory.VALIDATION_ERROR),
    VALIDATION_USERNAME_EXISTS("VAL_002", "用户名已存在", ErrorCategory.VALIDATION_ERROR),
    VALIDATION_ROOM_NAME_EXISTS("VAL_003", "房间名称已存在", ErrorCategory.VALIDATION_ERROR),
    VALIDATION_SENSITIVE_WORD("VAL_004", "消息包含敏感词", ErrorCategory.VALIDATION_ERROR),
    VALIDATION_PASSWORD_WEAK("VAL_005", "密码强度不足", ErrorCategory.VALIDATION_ERROR),

    BUSINESS_USER_NOT_FOUND("BIZ_001", "用户不存在", ErrorCategory.BUSINESS_ERROR),
    BUSINESS_ROOM_NOT_FOUND("BIZ_002", "房间不存在", ErrorCategory.BUSINESS_ERROR),
    BUSINESS_ROOM_FULL("BIZ_003", "房间已满", ErrorCategory.BUSINESS_ERROR),
    BUSINESS_ROOM_PASSWORD_WRONG("BIZ_004", "房间密码错误", ErrorCategory.BUSINESS_ERROR),
    BUSINESS_ROOM_PRIVATE("BIZ_005", "房间为私有房间", ErrorCategory.BUSINESS_ERROR),
    BUSINESS_MESSAGE_NOT_FOUND("BIZ_006", "消息不存在", ErrorCategory.BUSINESS_ERROR),
    BUSINESS_MESSAGE_RECALLED("BIZ_007", "消息已被撤回", ErrorCategory.BUSINESS_ERROR),
    BUSINESS_MESSAGE_RECALL_TIMEOUT("BIZ_008", "消息撤回超时", ErrorCategory.BUSINESS_ERROR),
    BUSINESS_USER_MUTED("BIZ_009", "用户已被禁言", ErrorCategory.BUSINESS_ERROR),
    BUSINESS_USER_ALREADY_ONLINE("BIZ_010", "用户已在线", ErrorCategory.BUSINESS_ERROR),
    BUSINESS_USER_ALREADY_IN_ROOM("BIZ_011", "用户已在房间中", ErrorCategory.BUSINESS_ERROR),
    BUSINESS_USER_NOT_IN_ROOM("BIZ_012", "用户不在房间中", ErrorCategory.BUSINESS_ERROR),
    BUSINESS_SENSITIVE_WORD_EXISTS("BIZ_013", "敏感词已存在", ErrorCategory.BUSINESS_ERROR),
    BUSINESS_SENSITIVE_WORD_NOT_FOUND("BIZ_014", "敏感词不存在", ErrorCategory.BUSINESS_ERROR),
    BUSINESS_OPERATION_FAILED("BIZ_015", "操作失败", ErrorCategory.BUSINESS_ERROR),

    SYSTEM_INTERNAL_ERROR("SYS_001", "系统内部错误", ErrorCategory.SYSTEM_ERROR),
    SYSTEM_DATABASE_ERROR("SYS_002", "数据库错误", ErrorCategory.SYSTEM_ERROR),
    SYSTEM_FILE_ERROR("SYS_003", "文件操作错误", ErrorCategory.SYSTEM_ERROR),
    SYSTEM_NETWORK_ERROR("SYS_004", "网络错误", ErrorCategory.SYSTEM_ERROR),
    SYSTEM_SERVICE_UNAVAILABLE("SYS_005", "服务不可用", ErrorCategory.SYSTEM_ERROR),
    SYSTEM_TIMEOUT("SYS_006", "请求超时", ErrorCategory.SYSTEM_ERROR),

    NOT_FOUND_RESOURCE("NF_001", "资源不存在", ErrorCategory.NOT_FOUND_ERROR),
    NOT_FOUND_ENDPOINT("NF_002", "接口不存在", ErrorCategory.NOT_FOUND_ERROR),
    NOT_FOUND_METHOD("NF_003", "请求方法不支持", ErrorCategory.NOT_FOUND_ERROR),

    RATE_LIMIT_EXCEEDED("RATE_001", "请求过于频繁，请稍后再试", ErrorCategory.RATE_LIMIT_ERROR);

    private final String code;
    private final String message;
    private final ErrorCategory category;

    ErrorCode(String code, String message, ErrorCategory category) {
        this.code = code;
        this.message = message;
        this.category = category;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public ErrorCategory getCategory() {
        return category;
    }
}