package com.chat.exception;

import com.chat.model.ErrorResponse;
import com.chat.service.ErrorLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Autowired
    private ErrorLogService errorLogService;

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex, HttpServletRequest request) {
        log.error("业务异常: {} - {}", ex.getErrorCode().getCode(), ex.getMessage(), ex);
        
        errorLogService.logError(
            ex.getErrorCode().getCode(),
            ex.getErrorCode().getMessage(),
            ex.getErrorCode().getCategory().getCode(),
            ex.getDetails(),
            request,
            ex
        );
        
        ErrorResponse response = ErrorResponse.builder()
                .errorCode(ex.getErrorCode().getCode())
                .errorMessage(ex.getErrorCode().getMessage())
                .errorCategory(ex.getErrorCode().getCategory())
                .errorDetails(ex.getDetails())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(getHttpStatus(ex.getErrorCode())).body(response);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        log.error("业务异常: {} - {}", ex.getErrorCode().getCode(), ex.getMessage(), ex);
        
        ErrorResponse response = ErrorResponse.builder()
                .errorCode(ex.getErrorCode().getCode())
                .errorMessage(ex.getErrorCode().getMessage())
                .errorCategory(ex.getErrorCode().getCategory())
                .errorDetails(ex.getDetails())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(SystemException.class)
    public ResponseEntity<ErrorResponse> handleSystemException(SystemException ex, HttpServletRequest request) {
        log.error("系统异常: {} - {}", ex.getErrorCode().getCode(), ex.getMessage(), ex);
        
        ErrorResponse response = ErrorResponse.builder()
                .errorCode(ex.getErrorCode().getCode())
                .errorMessage(ex.getErrorCode().getMessage())
                .errorCategory(ex.getErrorCode().getCategory())
                .errorDetails(ex.getDetails())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(ParameterException.class)
    public ResponseEntity<ErrorResponse> handleParameterException(ParameterException ex, HttpServletRequest request) {
        log.error("参数异常: {} - {}", ex.getErrorCode().getCode(), ex.getMessage());
        
        ErrorResponse response = ErrorResponse.builder()
                .errorCode(ex.getErrorCode().getCode())
                .errorMessage(ex.getErrorCode().getMessage())
                .errorCategory(ex.getErrorCode().getCategory())
                .errorDetails(ex.getDetails())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        log.warn("认证异常: {} - {}", ex.getErrorCode().getCode(), ex.getMessage());
        
        ErrorResponse response = ErrorResponse.builder()
                .errorCode(ex.getErrorCode().getCode())
                .errorMessage(ex.getErrorCode().getMessage())
                .errorCategory(ex.getErrorCode().getCategory())
                .errorDetails(ex.getDetails())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationException(AuthorizationException ex, HttpServletRequest request) {
        log.warn("授权异常: {} - {}", ex.getErrorCode().getCode(), ex.getMessage());
        
        ErrorResponse response = ErrorResponse.builder()
                .errorCode(ex.getErrorCode().getCode())
                .errorMessage(ex.getErrorCode().getMessage())
                .errorCategory(ex.getErrorCode().getCategory())
                .errorDetails(ex.getDetails())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());
        
        log.warn("参数验证失败: {}", validationErrors);
        
        ErrorResponse response = ErrorResponse.builder()
                .errorCode("VAL_001")
                .errorMessage("数据验证失败")
                .errorCategory(com.chat.model.ErrorCategory.VALIDATION_ERROR)
                .validationErrors(validationErrors)
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException ex, HttpServletRequest request) {
        List<String> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());
        
        log.warn("参数绑定失败: {}", validationErrors);
        
        ErrorResponse response = ErrorResponse.builder()
                .errorCode("VAL_001")
                .errorMessage("数据验证失败")
                .errorCategory(com.chat.model.ErrorCategory.VALIDATION_ERROR)
                .validationErrors(validationErrors)
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {
        List<String> validationErrors = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());
        
        log.warn("约束验证失败: {}", validationErrors);
        
        ErrorResponse response = ErrorResponse.builder()
                .errorCode("VAL_001")
                .errorMessage("数据验证失败")
                .errorCategory(com.chat.model.ErrorCategory.VALIDATION_ERROR)
                .validationErrors(validationErrors)
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameterException(MissingServletRequestParameterException ex, HttpServletRequest request) {
        log.warn("缺少必要参数: {}", ex.getParameterName());
        
        ErrorResponse response = ErrorResponse.builder()
                .errorCode("PARAM_001")
                .errorMessage("缺少必要参数")
                .errorCategory(com.chat.model.ErrorCategory.PARAMETER_ERROR)
                .errorDetails("缺少参数: " + ex.getParameterName())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        log.warn("参数类型错误: {} - 期望类型: {}", ex.getName(), ex.getRequiredType());
        
        ErrorResponse response = ErrorResponse.builder()
                .errorCode("PARAM_003")
                .errorMessage("参数类型错误")
                .errorCategory(com.chat.model.ErrorCategory.PARAMETER_ERROR)
                .errorDetails("参数 " + ex.getName() + " 类型错误，期望类型: " + ex.getRequiredType().getSimpleName())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadableException(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("请求体格式错误: {}", ex.getMessage());
        
        ErrorResponse response = ErrorResponse.builder()
                .errorCode("PARAM_002")
                .errorMessage("参数格式错误")
                .errorCategory(com.chat.model.ErrorCategory.PARAMETER_ERROR)
                .errorDetails("请求体格式错误，请检查JSON格式")
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        log.warn("不支持的请求方法: {} - {}", request.getMethod(), request.getRequestURI());
        
        ErrorResponse response = ErrorResponse.builder()
                .errorCode("NF_003")
                .errorMessage("请求方法不支持")
                .errorCategory(com.chat.model.ErrorCategory.NOT_FOUND_ERROR)
                .errorDetails("不支持的请求方法: " + ex.getMethod())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFound(NoHandlerFoundException ex, HttpServletRequest request) {
        log.warn("接口不存在: {} {}", ex.getHttpMethod(), ex.getRequestURL());
        
        ErrorResponse response = ErrorResponse.builder()
                .errorCode("NF_002")
                .errorMessage("接口不存在")
                .errorCategory(com.chat.model.ErrorCategory.NOT_FOUND_ERROR)
                .errorDetails("接口不存在: " + ex.getHttpMethod() + " " + ex.getRequestURL())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("未处理的异常: {}", ex.getMessage(), ex);
        
        errorLogService.logError(
            "SYS_001",
            "系统内部错误",
            "SYSTEM",
            "未处理的异常",
            request,
            ex
        );
        
        ErrorResponse response = ErrorResponse.builder()
                .errorCode("SYS_001")
                .errorMessage("系统内部错误")
                .errorCategory(com.chat.model.ErrorCategory.SYSTEM_ERROR)
                .errorDetails("系统内部错误，请稍后重试")
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private HttpStatus getHttpStatus(com.chat.model.ErrorCode errorCode) {
        return switch (errorCode.getCategory()) {
            case AUTHENTICATION_ERROR -> HttpStatus.UNAUTHORIZED;
            case AUTHORIZATION_ERROR -> HttpStatus.FORBIDDEN;
            case PARAMETER_ERROR, VALIDATION_ERROR -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND_ERROR -> HttpStatus.NOT_FOUND;
            case RATE_LIMIT_ERROR -> HttpStatus.TOO_MANY_REQUESTS;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}