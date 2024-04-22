package messaner;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import messaner.DTO.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.crypto.SecretKey;
import java.security.Key;
import java.time.ZonedDateTime;
import java.util.Date;

public class JwtProvider {
    private final SecretKey secretKey;
    private final long expireTime;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public JwtProvider(String secretKey, long expireTime) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.expireTime = expireTime;
    }

    public String createToken(UserDTO userDTO) {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime tokenExp = now.plusSeconds(expireTime);

        return Jwts.builder()
                .issuedAt(Date.from(now.toInstant()))
                .expiration(Date.from(tokenExp.toInstant()))
                .claim("userId", userDTO.getUser())
                .signWith(secretKey)
                .compact();
    }

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

    public String getUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.get("userId", String.class);
        } catch (JwtException e) {
            log.info(e.getMessage());
        }
        return "";
    }
}
