package ru.openblocks.authmanagerservice.persistence.entity;

import lombok.*;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "user_data")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDataEntity {

    private Long id;

    private String login;

    private String password;
}

