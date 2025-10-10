package com.technicalchallenge.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ControllerAdvice
public class TradeExceptionHandler {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)

    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        Set<String> errors = e.getBindingResult().getFieldErrors()
                .stream().map(FieldError::getField).collect(Collectors.toSet());

        if (errors.contains("tradeDate")) {
            return ResponseEntity.badRequest().body("Trade date is required");
        }
        if (errors.contains("bookName") || errors.contains("counterparty")) {
            return ResponseEntity.badRequest().body("Book and Counterparty are required");
        }
//        return ResponseEntity.badRequest().body("Book and Counterparty are required");
        return ResponseEntity.badRequest().build();
    }
}


