package ru.openblocks.authmanagerservice.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;
import ru.openblocks.authmanagerservice.api.dto.error.ErrorResponse;
import ru.openblocks.authmanagerservice.exception.BadCredentialsException;

@RestControllerAdvice
public class ExceptionHandlerAdvice {

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(BadCredentialsException.class)
    public Mono<ErrorResponse> badCredentialsException(BadCredentialsException ex) {
        return Mono.just(ErrorResponse.builder()
                .message(ex.getMessage())
                .code(HttpStatus.UNAUTHORIZED.value())
                .build());
    }
}
