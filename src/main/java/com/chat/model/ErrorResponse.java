package com.chat.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private String errorCode;
    private String errorMessage;
    private ErrorCategory errorCategory;
    private String errorDetails;
    private List<String> validationErrors;
    private String path;
    private String requestId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime timestamp;

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(String errorCode, String errorMessage, ErrorCategory errorCategory) {
        this();
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.errorCategory = errorCategory;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public ErrorCategory getErrorCategory() {
        return errorCategory;
    }

    public void setErrorCategory(ErrorCategory errorCategory) {
        this.errorCategory = errorCategory;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(List<String> validationErrors) {
        this.validationErrors = validationErrors;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public static class Builder {
        private ErrorResponse response = new ErrorResponse();

        public Builder errorCode(String errorCode) {
            response.setErrorCode(errorCode);
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            response.setErrorMessage(errorMessage);
            return this;
        }

        public Builder errorCategory(ErrorCategory errorCategory) {
            response.setErrorCategory(errorCategory);
            return this;
        }

        public Builder errorDetails(String errorDetails) {
            response.setErrorDetails(errorDetails);
            return this;
        }

        public Builder validationErrors(List<String> validationErrors) {
            response.setValidationErrors(validationErrors);
            return this;
        }

        public Builder path(String path) {
            response.setPath(path);
            return this;
        }

        public Builder requestId(String requestId) {
            response.setRequestId(requestId);
            return this;
        }

        public ErrorResponse build() {
            return response;
        }
    }
}