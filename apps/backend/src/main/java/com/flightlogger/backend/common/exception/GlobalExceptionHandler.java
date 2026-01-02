package com.flightlogger.backend.common.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Locale;

@ControllerAdvice
@Order(2)
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private static final String INVALID_REQUEST_MESSAGE = "Invalid request";
    private static final String VALIDATION_ERROR_TITLE = "Validation error";

    private final MessageSource messageSource;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ProblemDetail handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> messageSource.getMessage(error, null))
                .orElse(INVALID_REQUEST_MESSAGE);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errorMessage);
        problemDetail.setTitle(VALIDATION_ERROR_TITLE);

        return problemDetail;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ProblemDetail handleConstraintViolationException(ConstraintViolationException ex) {
        String errorMessage = ex.getConstraintViolations().stream()
                .findFirst()
                .map(this::resolveErrorMessage)
                .orElse(INVALID_REQUEST_MESSAGE);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errorMessage);
        problemDetail.setTitle(VALIDATION_ERROR_TITLE);

        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ProblemDetail handleMismatchedInputException(MethodArgumentTypeMismatchException ex) {
        String messageKey = "typeMismatch." + ex.getName();

        String errorMessage = messageSource.getMessage(
                messageKey,
                null,
                INVALID_REQUEST_MESSAGE, // Your constant
                Locale.getDefault()
        );

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errorMessage);
        problemDetail.setTitle(VALIDATION_ERROR_TITLE);

        return problemDetail;
    }

    /**
     * Helper to retrieve the constraint as well as the field name from the violation and looks for an error message
     * key.
     *
     * @return the error message or "Invalid request" if no message is found.
     * @param violation the violation to resolve the error message for
     */
    private String resolveErrorMessage(ConstraintViolation<?> violation) {
        String constraintName = violation.getConstraintDescriptor()
                .getAnnotation().annotationType().getSimpleName();

        String fieldName = getLeafNodeName(violation.getPropertyPath());

        // Construct the message key from the constraint name and field name
        String messageKey = String.format("%s.%s", constraintName, fieldName);

        return messageSource.getMessage(messageKey, null, INVALID_REQUEST_MESSAGE, Locale.getDefault());
    }

    /**
     * Helper to get the last part of the property path (the actual field name).
     * e.g., converts "validateUser.arg0.icao" -> "icao"
     */
    private String getLeafNodeName(Path propertyPath) {
        String leafNodeName = "unknown";
        for (Path.Node node : propertyPath) {
            leafNodeName = node.getName();
        }
        return leafNodeName;
    }
}
