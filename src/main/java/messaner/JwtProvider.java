package messaner;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtProvider {
    private final String secret;
    private final SecretKey secretKey;
    private final long expireTime;
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    public JwtProvider(@Value("${spring.jwt.secret}") String secret, @Value("${spring.jwt.expireTime}") long expireTime) {
        this.secret = secret;
        this.expireTime = expireTime;
        String keyBase64Encoded = Base64.getEncoder().encodeToString(secret.getBytes());
        this.secretKey = Keys.hmacShaKeyFor(keyBase64Encoded.getBytes());
    }

    public String createToken(String user) {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime tokenExp = now.plusSeconds(expireTime);

        return "Bearer " + Jwts.builder()
                .issuedAt(Date.from(now.toInstant()))
                .expiration(Date.from(tokenExp.toInstant()))
                .subject(user)
                .signWith(secretKey)
                .compact();
    }

    /*
    public String createRefreshToken(String user) {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime tokenExp = now.plusSeconds(expireTime);

        String refreshToken = Jwts.builder()
                .issuedAt(Date.from(now.toInstant()))
                .expiration(Date.from(tokenExp.toInstant()))
                .subject(user)
                .signWith(secretKey)
                .compact();

        return refreshToken;
    }
     */

    public String getUserId(String token) {
        return this.getClaim(token, Claims::getSubject);
    }

    public Date getExpiration(String token) {
        return this.getClaim(token, Claims::getExpiration);
    }

    private <T>T getClaim(String token, Function<Claims, T> claimsResolver) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claimsResolver.apply(claims);
        } catch (JwtException e) {
            log.info(e.getMessage());
            return null;
        }
    }

    public boolean isBearerToken(String token) {
        return token.startsWith("Bearer ");
    }

    public boolean isTokenExpired(String token) {
        return this.getExpiration(token).before(new Date());
    }

    public boolean validateToken(String token, String name) {
        return (this.getUserId(token).equals(name) && isTokenExpired(token));
    }
}
