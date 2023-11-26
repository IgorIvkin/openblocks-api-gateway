package ru.openblocks.authcheckservice.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.openblocks.authcheckservice.api.dto.checkauth.get.CheckAuthenticationRequest;
import ru.openblocks.authcheckservice.api.dto.checkauth.get.CheckAuthenticationResponse;
import ru.openblocks.authcheckservice.service.CheckAuthenticationService;

@RestController
@RequiredArgsConstructor
public class CheckAuthenticationController {

    private final CheckAuthenticationService checkAuthenticationService;

    @PostMapping("/api/v1/authentication/check")
    public Mono<CheckAuthenticationResponse> checkAuthentication(@RequestBody @Valid CheckAuthenticationRequest request) {
        return checkAuthenticationService.checkAuthentication(request);
    }
}
