package com.example.demo.exceptions;

import com.example.demo.dtos.responses.BaseResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalAdviceException {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<BaseResponse<Object>> handleBadRequest(BadRequestException exception) {
        log.warn("[BUSINESS LOGIC FAILED] Bad Request: {}", exception.getMessage());
        BaseResponse<Object> response = BaseResponse.error(exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<BaseResponse<Object>> handleDataNotFound(DataNotFoundException exception) {
        log.warn("[DATA NOT FOUND] resource missing: {}", exception.getMessage());
        BaseResponse<Object> response = BaseResponse.error(exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        ArrayList<String> errorMessages = new ArrayList<>();
        exception.getBindingResult()
                .getFieldErrors()
                .forEach(err ->
                        errorMessages.add(err.getDefaultMessage()));
        
        log.warn("[VALIDATION FAILED] Invalid input parameters: {}", errorMessages);
        
        BaseResponse<Object> response = BaseResponse.error("Validation Error", errorMessages);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<BaseResponse<Object>> handleConstraintViolation(ConstraintViolationException exception) {
        List<String> errorMessages = exception.getConstraintViolations()
                .stream()
                .map(violation -> {
                    String propertyPath = violation.getPropertyPath().toString();
                    // Extract index from property path like "createBulk.requests[0].stock"
                    String indexPart = propertyPath.contains("[") && propertyPath.contains("]") 
                        ? propertyPath.substring(propertyPath.indexOf("[") + 1, propertyPath.indexOf("]"))
                        : null;
                    
                    if (indexPart != null) {
                        try {
                            int index = Integer.parseInt(indexPart) + 1;
                            return "Data ke-" + index + ": " + violation.getMessage();
                        } catch (NumberFormatException e) {
                            return violation.getMessage();
                        }
                    }
                    return violation.getMessage();
                })
                .collect(Collectors.toList());

        log.warn("[CONSTRAINT VIOLATION] Validation failed: {}", errorMessages);

        BaseResponse<Object> response = BaseResponse.error("Validation Error", errorMessages);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<BaseResponse<Object>> handleUnauthorized(UnauthorizedException exception) {
        log.warn("[UNAUTHORIZED] Access denied: {}", exception.getMessage());
        BaseResponse<Object> response = BaseResponse.error(exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<BaseResponse<Object>> handleAccountLocked(AccountLockedException exception) {
        log.warn("[ACCOUNT LOCKED] Access denied: {}", exception.getMessage());
        BaseResponse<Object> response = BaseResponse.error(exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.LOCKED).body(response);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<BaseResponse<Object>> handleDuplicateResource(DuplicateResourceException exception) {
        log.warn("[DUPLICATE RESOURCE] Conflict: {}", exception.getMessage());
        BaseResponse<Object> response = BaseResponse.error(exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Object>> handleGeneralException(Exception exception) {
        log.error("[SYSTEM ERROR] Unexpected exception: {}", exception.getMessage(), exception);
        BaseResponse<Object> response = BaseResponse.error("Internal Server Error: " + exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
