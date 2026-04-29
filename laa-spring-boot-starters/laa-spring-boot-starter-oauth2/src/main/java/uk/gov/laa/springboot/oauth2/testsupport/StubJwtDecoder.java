package uk.gov.laa.springboot.oauth2.testsupport;

import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

/**
 * JwtDecoder test utility backed by a static map of token strings.
 */
public class StubJwtDecoder implements JwtDecoder {

  private final Map<String, Jwt> tokens;

  public StubJwtDecoder(Iterable<StubJwtToken> stubTokens) {
    this.tokens = toMap(stubTokens);
  }

  @Override
  public Jwt decode(String token) {
    Jwt jwt = tokens.get(token);
    if (jwt == null) {
      throw new OAuth2AuthenticationException(
          new OAuth2Error("invalid_token", "No matching stub JWT token.", null));
    }
    return jwt;
  }

  private Map<String, Jwt> toMap(Iterable<StubJwtToken> stubTokens) {
    return toStream(stubTokens)
        .collect(Collectors.toUnmodifiableMap(StubJwtToken::token, StubJwtToken::toJwt));
  }

  private java.util.stream.Stream<StubJwtToken> toStream(Iterable<StubJwtToken> iterable) {
    return java.util.stream.StreamSupport.stream(iterable.spliterator(), false);
  }

  /**
   * Builds a decoder from a fixed set of token fixtures.
   *
   * @param tokens tokens accepted by the decoder
   * @return decoder instance
   */
  public static StubJwtDecoder of(StubJwtToken... tokens) {
    return new StubJwtDecoder(java.util.List.of(tokens));
  }
}
