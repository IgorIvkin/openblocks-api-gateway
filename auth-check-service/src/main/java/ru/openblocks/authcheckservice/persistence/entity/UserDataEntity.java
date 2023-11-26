package ru.openblocks.authcheckservice.persistence.entity;

import lombok.*;
import org.springframework.data.relational.core.mapping.Table;

@Table
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
