package com.example.demo.exceptions;

import com.example.demo.dtos.responses.BaseResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalAdviceException {
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<BaseResponse<Object>> handleBadRequest(BadRequestException exception) {
        BaseResponse<Object> response = BaseResponse.error(exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<BaseResponse<Object>> handleDataNotFound(DataNotFoundException exception) {
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
        BaseResponse<Object> response = BaseResponse.error("Validation Error", errorMessages);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<BaseResponse<Object>> handleConstraintViolation(ConstraintViolationException exception) {
        List<String> errorMessages = exception.getConstraintViolations()
                .stream()
                .map(violation -> {
                    String propertyPath = violation.getPropertyPath().toString();
                    String indexPart = propertyPath.contains("[") && propertyPath.contains("]") 
                        ? propertyPath.substring(propertyPath.indexOf("[") + 1, propertyPath.indexOf("]"))
                        : null;
                    
                    if (indexPart != null) {
                        try {
                            int index = Integer.parseInt(indexPart) + 1;
                            return "Data-" + index + ": " + violation.getMessage();
                        } catch (NumberFormatException e) {
                            return violation.getMessage();
                        }
                    }
                    return violation.getMessage();
                })
                .collect(Collectors.toList());
        BaseResponse<Object> response = BaseResponse.error("Validation Error", errorMessages);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<BaseResponse<Object>> handleUnauthorized(UnauthorizedException exception) {
        BaseResponse<Object> response = BaseResponse.error(exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<BaseResponse<Object>> handleDuplicateResource(DuplicateResourceException exception) {
        BaseResponse<Object> response = BaseResponse.error(exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Object>> handleGeneralException(Exception exception) {
        BaseResponse<Object> response = BaseResponse.error("Internal Server Error: " + exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
