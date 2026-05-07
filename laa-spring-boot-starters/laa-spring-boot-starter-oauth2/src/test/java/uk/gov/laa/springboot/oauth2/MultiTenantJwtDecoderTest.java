package uk.gov.laa.springboot.oauth2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

class MultiTenantJwtDecoderTest {

  private static final String FIRST_ISSUER = "https://login.microsoftonline.com/tenant-one/v2.0";
  private static final String SECOND_ISSUER = "https://login.microsoftonline.com/tenant-two/v2.0";

  @Test
  void routesTokenToDecoderForIssuer() {
    String firstToken = tokenForIssuer(FIRST_ISSUER);
    String secondToken = tokenForIssuer(SECOND_ISSUER);
    Jwt firstJwt = jwt(firstToken, FIRST_ISSUER);
    Jwt secondJwt = jwt(secondToken, SECOND_ISSUER);
    JwtDecoder decoder =
        new MultiTenantJwtDecoder(
            Map.of(
                FIRST_ISSUER, token -> firstJwt,
                SECOND_ISSUER, token -> secondJwt));

    assertThat(decoder.decode(firstToken)).isSameAs(firstJwt);
    assertThat(decoder.decode(secondToken)).isSameAs(secondJwt);
  }

  @Test
  void rejectsTokenFromUntrustedIssuer() {
    JwtDecoder decoder = new MultiTenantJwtDecoder(Map.of(FIRST_ISSUER, token -> jwt(token, FIRST_ISSUER)));

    assertThatThrownBy(() -> decoder.decode(tokenForIssuer("https://untrusted.example/issuer")))
        .isInstanceOf(BadJwtException.class)
        .hasMessage("JWT issuer is not trusted");
  }

  @Test
  void rejectsTokenWithoutIssuer() {
    JwtDecoder decoder = new MultiTenantJwtDecoder(Map.of(FIRST_ISSUER, token -> jwt(token, FIRST_ISSUER)));

    assertThatThrownBy(() -> decoder.decode(new PlainJWT(new JWTClaimsSet.Builder().build()).serialize()))
        .isInstanceOf(BadJwtException.class)
        .hasMessage("JWT issuer is missing");
  }

  @Test
  void rejectsMalformedToken() {
    JwtDecoder decoder = new MultiTenantJwtDecoder(Map.of(FIRST_ISSUER, token -> jwt(token, FIRST_ISSUER)));

    assertThatThrownBy(() -> decoder.decode("not-a-jwt"))
        .isInstanceOf(BadJwtException.class)
        .hasMessage("JWT could not be parsed");
  }

  private static String tokenForIssuer(String issuer) {
    return new PlainJWT(new JWTClaimsSet.Builder().issuer(issuer).build()).serialize();
  }

  private static Jwt jwt(String token, String issuer) {
    return Jwt.withTokenValue(token).header("alg", "none").claim("iss", issuer).build();
  }
}
