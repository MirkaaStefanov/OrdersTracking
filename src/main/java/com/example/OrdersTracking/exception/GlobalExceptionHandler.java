package com.example.OrdersTracking.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.nio.file.AccessDeniedException;

@RequiredArgsConstructor
@ControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDeniedError(AccessDeniedException ex) {
        return "redirect:/";
    }

    @ExceptionHandler(Exception.class)
    public String alertSlackChannelWhenUnexpectedErrorOccurs(Exception ex, HttpServletRequest request) {

        request.setAttribute("javax.servlet.error.status_code", HttpStatus.INTERNAL_SERVER_ERROR.value());
        request.setAttribute("javax.servlet.error.message", ex.getMessage());

        return "forward:/error";
    }
}
