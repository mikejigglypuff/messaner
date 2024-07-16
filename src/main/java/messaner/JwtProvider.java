package messaner;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import messaner.service.RepositoryService;
import messaner.Utility;
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
    private final RepositoryService repositoryService;
    private final Utility utility;
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    public JwtProvider(
            @Value("${spring.jwt.secret}") String secret,
            @Value("${spring.jwt.expireTime}") long expireTime,
            RepositoryService repositoryService,
            Utility utility
    ) {
        this.secret = secret;
        this.expireTime = expireTime;
        String keyBase64Encoded = Base64.getEncoder().encodeToString(secret.getBytes());
        this.secretKey = Keys.hmacShaKeyFor(keyBase64Encoded.getBytes());
        this.repositoryService = repositoryService;
        this.utility = utility;
    }

    public String createToken() {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime tokenExp = now.plusSeconds(expireTime);

        return Jwts.builder()
                .issuedAt(Date.from(now.toInstant()))
                .expiration(Date.from(tokenExp.toInstant()))
                .subject(repositoryService.createUser())
                .claim("sessionId", utility.createSessionId())
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

    public String getSessionId(String token) { return this.getClaimBody(token, "sessionId", String.class); }

    private <T>T getClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private <T>T getClaimBody(String token, String name, Class<T> type) {
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

            String newToken = createToken();
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
