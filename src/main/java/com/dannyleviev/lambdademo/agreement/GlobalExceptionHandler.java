package com.dannyleviev.lambdademo.agreement;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

/** RFC 9457 ProblemDetail responses; the base class already maps validation errors to 400. */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(AgreementNotFoundException.class)
    public ProblemDetail handleNotFound(AgreementNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ConditionalCheckFailedException.class)
    public ProblemDetail handleConflict(ConditionalCheckFailedException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Agreement already exists");
    }
}