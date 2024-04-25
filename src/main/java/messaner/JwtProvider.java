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

    public String createToken(String user, String role) {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime tokenExp = now.plusSeconds(expireTime);

        return Jwts.builder()
                .issuedAt(Date.from(now.toInstant()))
                .expiration(Date.from(tokenExp.toInstant()))
                .claim("userId", user)
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
                .claim("userId", user)
                .signWith(secretKey)
                .compact();



        return refreshToken;
    }
     */

    public boolean validateToken(String token) {
        try {
            Jwt<?, ?> jwt = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parse(token);
            return true;
        } catch (JwtException e) {
            log.info(e.getMessage());
        }
        return false;
    }

    public String getClaim(String token, String claim) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.get(claim, String.class);
        } catch (JwtException e) {
            log.info(e.getMessage());
        }
        return "";
    }
}
