package com.dauducbach.identity_service.configuration;

import com.nimbusds.jwt.SignedJWT;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.text.ParseException;

@Component
public class CustomJwtDecoder implements ReactiveJwtDecoder {
    @NonFinal
    @Value("${jwt.signerKey}")
    private String SIGNER_KEY;

    @Override
    public Mono<Jwt> decode(String token) throws JwtException {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier jwsVerifier = new MACVerifier(SIGNER_KEY.getBytes());

            if (!signedJWT.verify(jwsVerifier)) {
                return Mono.error(new RuntimeException("Invalid Signature!"));
            }

            Jwt jwt = new Jwt(
                    token,
                    signedJWT.getJWTClaimsSet().getIssueTime().toInstant(),
                    signedJWT.getJWTClaimsSet().getExpirationTime().toInstant(),
                    signedJWT.getHeader().toJSONObject(),
                    signedJWT.getJWTClaimsSet().getClaims()
            );

            return Mono.just(jwt);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
