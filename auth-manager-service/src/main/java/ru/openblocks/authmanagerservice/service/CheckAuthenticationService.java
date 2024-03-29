package ru.openblocks.authmanagerservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.openblocks.authmanagerservice.api.dto.checkauth.get.CheckAuthenticationRequest;
import ru.openblocks.authmanagerservice.persistence.entity.UserDataEntity;
import ru.openblocks.authmanagerservice.persistence.repository.UserDataRepository;

@Slf4j
@Service
public class CheckAuthenticationService {

    private final PasswordEncoder passwordEncoder;

    private final UserDataRepository userDataRepository;

    @Autowired
    public CheckAuthenticationService(PasswordEncoder passwordEncoder,
                                      UserDataRepository userDataRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userDataRepository = userDataRepository;
    }

    /**
     * Checks if user authentication is correct.
     *
     * @param request request to check user authentication
     * @return reactive true if authentication is valid
     */
    @Transactional(readOnly = true)
    public Mono<Boolean> checkAuthentication(CheckAuthenticationRequest request) {

        final String login = request.getLogin();
        final String password = request.getPassword();

        log.info("Check password by login {}", login);

        return userDataRepository.findFirstByLogin(login)
                .map(user -> compareAndGet(password, user));
    }

    private Boolean compareAndGet(String password, UserDataEntity user) {
        if (passwordEncoder.matches(password, user.getPassword())) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

}
