package com.wanda.utils.exceptions.handler;

import com.wanda.utils.exceptions.CustomException;
import com.wanda.utils.exceptions.enums.ErrorCode;
import com.wanda.utils.exceptions.response.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        ErrorResponse error = new ErrorResponse(
                e.getMessage(),
                e.getCode()
        );

        System.out.println("error "+error);
        return new ResponseEntity<>(error, e.getStatusCode());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAnyException(CustomException e) {



        ErrorResponse error = new ErrorResponse(
                (e.getMessage() != null) ? e.getMessage() : "Internal Server Error",
                (e.getCode() == null) ? ErrorCode.GENERAL_ERROR : e.getCode()
        );
        System.out.println("error exception "+error);
        return new ResponseEntity<>(error, e.getStatusCode());
    }
}
