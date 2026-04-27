package com.tcu.projectpulse.common.exception;

import com.tcu.projectpulse.common.dto.Result;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ObjectNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    Result handleObjectNotFoundException(ObjectNotFoundException ex) {
        return Result.error(404, ex.getMessage());
    }

    @ExceptionHandler(AccountAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    Result handleAccountAlreadyExistsException(AccountAlreadyExistsException ex) {
        return Result.error(409, ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Result handleIllegalStateException(IllegalStateException ex) {
        return Result.error(400, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Result handleIllegalArgumentException(IllegalArgumentException ex) {
        return Result.error(400, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    Result handleGenericException(Exception ex) {
        return Result.error(500, "An unexpected error occurred: " + ex.getMessage());
    }
}
