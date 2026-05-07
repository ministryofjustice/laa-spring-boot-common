package uk.gov.laa.springboot.oauth2;

import com.nimbusds.jwt.JWTParser;
import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

class MultiTenantJwtDecoder implements JwtDecoder {

  private final Map<String, JwtDecoder> decodersByIssuer;
  private final Function<String, JwtDecoder> decoderFactory;
  private final Set<String> trustedIssuers;

  MultiTenantJwtDecoder(List<String> trustedIssuers, Function<String, JwtDecoder> decoderFactory) {
    this.decoderFactory = decoderFactory;
    this.trustedIssuers = new HashSet<>(trustedIssuers);
    this.decodersByIssuer = new ConcurrentHashMap<>();
  }

  MultiTenantJwtDecoder(Map<String, JwtDecoder> decodersByIssuer) {
    this.decoderFactory = decodersByIssuer::get;
    this.trustedIssuers = decodersByIssuer.keySet();
    this.decodersByIssuer = new ConcurrentHashMap<>(decodersByIssuer);
  }

  @Override
  public Jwt decode(String token) throws JwtException {
    String issuer = extractIssuer(token);

    if (!trustedIssuers.contains(issuer)) {
      throw new BadJwtException("JWT issuer is not trusted");
    }

    JwtDecoder decoder = decodersByIssuer.computeIfAbsent(issuer, decoderFactory);
    return decoder.decode(token);
  }

  private String extractIssuer(String token) {
    try {
      String issuer = JWTParser.parse(token).getJWTClaimsSet().getIssuer();
      if (issuer == null || issuer.isBlank()) {
        throw new BadJwtException("JWT issuer is missing");
      }
      return issuer;
    } catch (ParseException exception) {
      throw new BadJwtException("JWT could not be parsed", exception);
    }
  }
}
