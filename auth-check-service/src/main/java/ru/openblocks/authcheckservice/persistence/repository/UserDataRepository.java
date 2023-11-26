package ru.openblocks.authcheckservice.persistence.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.openblocks.authcheckservice.persistence.entity.UserDataEntity;

@Repository
public interface UserDataRepository extends ReactiveCrudRepository<UserDataEntity, Long> {

    @Query(value = "select * from user_data where login = :login")
    Mono<UserDataEntity> findFirstByLogin(@Param("login") String login);
}
