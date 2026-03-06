package com.chat.service;

import com.chat.model.ErrorLog;
import com.chat.repository.ErrorLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ErrorLogService {

    private static final Logger log = LoggerFactory.getLogger(ErrorLogService.class);

    private final ErrorLogRepository errorLogRepository;

    @Autowired
    public ErrorLogService(ErrorLogRepository errorLogRepository) {
        this.errorLogRepository = errorLogRepository;
    }

    @Async
    @Transactional
    public void logError(String errorCode, String errorMessage, String errorCategory, 
                        String errorDetails, HttpServletRequest request, Throwable throwable) {
        try {
            ErrorLog errorLog = new ErrorLog();
            errorLog.setErrorCode(errorCode);
            errorLog.setErrorMessage(errorMessage);
            errorLog.setErrorCategory(errorCategory);
            errorLog.setErrorDetails(errorDetails);
            
            if (request != null) {
                errorLog.setPath(request.getRequestURI());
                errorLog.setMethod(request.getMethod());
                errorLog.setParameters(getParameters(request));
                errorLog.setIpAddress(getClientIpAddress(request));
                errorLog.setUserAgent(request.getHeader("User-Agent"));
            }
            
            if (throwable != null) {
                errorLog.setStackTrace(getStackTrace(throwable));
            }
            
            errorLogRepository.save(errorLog);
        } catch (Exception e) {
            log.error("记录错误日志失败: {}", e.getMessage(), e);
        }
    }

    @Async
    @Transactional
    public void logError(String errorCode, String errorMessage, String errorCategory, 
                        String errorDetails, HttpServletRequest request, Throwable throwable,
                        String userId, String username) {
        try {
            ErrorLog errorLog = new ErrorLog();
            errorLog.setErrorCode(errorCode);
            errorLog.setErrorMessage(errorMessage);
            errorLog.setErrorCategory(errorCategory);
            errorLog.setErrorDetails(errorDetails);
            
            if (request != null) {
                errorLog.setPath(request.getRequestURI());
                errorLog.setMethod(request.getMethod());
                errorLog.setParameters(getParameters(request));
                errorLog.setIpAddress(getClientIpAddress(request));
                errorLog.setUserAgent(request.getHeader("User-Agent"));
            }
            
            errorLog.setUserId(userId);
            errorLog.setUsername(username);
            
            if (throwable != null) {
                errorLog.setStackTrace(getStackTrace(throwable));
            }
            
            errorLogRepository.save(errorLog);
        } catch (Exception e) {
            log.error("记录错误日志失败: {}", e.getMessage(), e);
        }
    }

    public List<ErrorLog> getErrorLogsByErrorCode(String errorCode) {
        return errorLogRepository.findByErrorCodeOrderByTimestampDesc(errorCode);
    }

    public List<ErrorLog> getErrorLogsByCategory(String errorCategory) {
        return errorLogRepository.findByErrorCategoryOrderByTimestampDesc(errorCategory);
    }

    public List<ErrorLog> getErrorLogsByTimeRange(LocalDateTime start, LocalDateTime end) {
        return errorLogRepository.findByTimestampBetweenOrderByTimestampDesc(start, end);
    }

    public List<ErrorLog> getErrorLogsByUserId(String userId) {
        return errorLogRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    public List<ErrorLog> getErrorLogsByIpAddress(String ipAddress) {
        return errorLogRepository.findByIpAddressOrderByTimestampDesc(ipAddress);
    }

    @Transactional
    public void deleteOldLogs(LocalDateTime before) {
        int deleted = errorLogRepository.deleteByTimestampBefore(before);
        log.info("删除了 {} 条过期错误日志（{}之前）", deleted, before);
    }

    private String getParameters(HttpServletRequest request) {
        try {
            StringBuilder params = new StringBuilder();
            request.getParameterMap().forEach((key, values) -> {
                params.append(key).append("=");
                for (String value : values) {
                    params.append(value).append(",");
                }
                params.append("; ");
            });
            return params.toString();
        } catch (Exception e) {
            return "参数获取失败";
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        String stackTrace = sw.toString();
        
        if (stackTrace.length() > 5000) {
            stackTrace = stackTrace.substring(0, 5000) + "...";
        }
        
        return stackTrace;
    }
}