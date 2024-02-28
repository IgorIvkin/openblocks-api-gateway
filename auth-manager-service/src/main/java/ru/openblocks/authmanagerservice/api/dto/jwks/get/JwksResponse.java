package ru.openblocks.authmanagerservice.api.dto.jwks.get;

import com.nimbusds.jose.jwk.JWK;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwksResponse {

    List<Map<String, Object>> keys;
}
