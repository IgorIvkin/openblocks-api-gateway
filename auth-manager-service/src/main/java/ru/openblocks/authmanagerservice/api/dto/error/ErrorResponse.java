package ru.openblocks.authmanagerservice.api.dto.error;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class ErrorResponse {

    private Integer code;

    private String message;
}
