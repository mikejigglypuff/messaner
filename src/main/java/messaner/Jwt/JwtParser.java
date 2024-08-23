package messaner.Jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtParser {

  private final SecretKey secretKey;
  private final JwtProvider jwtProvider;

  @Autowired
  public JwtParser(@Value("${spring.jwt.secret}") String secret, JwtProvider jwtProvider) {
    String keyBase64Encoded = Base64.getEncoder().encodeToString(secret.getBytes());
    this.secretKey = Keys.hmacShaKeyFor(keyBase64Encoded.getBytes());
    this.jwtProvider = jwtProvider;
  }

  public String getUserId(String token) {
    return this.getClaim(token, Claims::getSubject);
  }

  public Date getExpiration(String token) {
    return this.getClaim(token, Claims::getExpiration);
  }

  public String getSessionId(String token) {
    return this.getClaimBody(token, "sessionId", String.class);
  }

  private <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = getAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private <T> T getClaimBody(String token, String name, Class<T> type) {
    final Claims claims = getAllClaims(token);
    return claims.get(name, type);
  }

  private Claims getAllClaims(String token) {
    try {
      return Jwts.parser()
          .verifyWith(secretKey)
          .build()
          .parseSignedClaims(token)
          .getPayload();
    } catch (JwtException e) {
      log.info(e.getMessage());

      String newToken = jwtProvider.createToken();
      return getAllClaims(newToken);
    }
  }

  public boolean isTokenExpired(String token) {
    return this.getExpiration(token).before(new Date());
  }

  public boolean validateToken(String token, String name) {
    return (this.getUserId(token).equals(name) && isTokenExpired(token));
  }
}
