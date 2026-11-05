package org.example.backend.common.controllers;

import jakarta.validation.ConstraintViolationException;
import org.example.backend.common.DTO.ErrorDetail;
import org.example.backend.common.DTO.ErrorResponse;
import org.example.backend.common.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.UUID;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException exception) {
        return response(
                exception.getStatus(),
                exception.getCode(),
                exception.getMessage(),
                exception.getDetails()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
        List<ErrorDetail> details = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ErrorDetail(error.getField(), error.getDefaultMessage(), null, null))
                .toList();
        return response(
                HttpStatus.BAD_REQUEST,
                "INVALID_REQUEST",
                "Request không hợp lệ.",
                details
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableMessage() {
        return response(
                HttpStatus.BAD_REQUEST,
                "INVALID_REQUEST",
                "Request không hợp lệ.",
                List.of(new ErrorDetail("request", "Malformed JSON or invalid field type.", null, null))
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException exception) {
        List<ErrorDetail> details = exception.getConstraintViolations()
                .stream()
                .map(violation -> new ErrorDetail(
                        violation.getPropertyPath().toString(),
                        violation.getMessage(),
                        null,
                        null
                ))
                .toList();
        return response(
                HttpStatus.BAD_REQUEST,
                "INVALID_REQUEST",
                "Request không hợp lệ.",
                details
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception exception) {
        String errorId = UUID.randomUUID().toString();
        LOGGER.error(
                "Unexpected API failure errorId={} exceptionType={}",
                errorId,
                exception.getClass().getName()
        );
        return response(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                "Hệ thống chưa thể xử lý yêu cầu.",
                List.of(new ErrorDetail(
                        "errorId",
                        "Reference ID for this failure.",
                        null,
                        errorId
                ))
        );
    }

    private ResponseEntity<ErrorResponse> response(
            HttpStatus status,
            String code,
            String message,
            List<ErrorDetail> details
    ) {
        return ResponseEntity.status(status).body(
                new ErrorResponse(new ErrorResponse.ErrorBody(code, message, details))
        );
    }
}
