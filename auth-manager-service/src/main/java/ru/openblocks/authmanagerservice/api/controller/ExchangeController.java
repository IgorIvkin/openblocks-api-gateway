package ru.openblocks.authmanagerservice.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.openblocks.authmanagerservice.api.dto.exchange.get.ExchangeTokenRequest;
import ru.openblocks.authmanagerservice.api.dto.token.get.Oauth2TokenResponse;
import ru.openblocks.authmanagerservice.service.ExchangeService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/exchange")
public class ExchangeController {

    private final ExchangeService exchangeService;

    @PostMapping(produces = {MediaType.APPLICATION_JSON_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE})
    public Mono<Oauth2TokenResponse> exchange(@RequestBody @Valid ExchangeTokenRequest request) {
        return Mono.fromSupplier(() -> exchangeService.exchange(request));
    }
}
