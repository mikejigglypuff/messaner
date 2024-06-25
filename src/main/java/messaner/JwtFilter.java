package messaner;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import messaner.DTO.UserDTO;
import messaner.service.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class JwtFilter extends OncePerRequestFilter {
    private final RepositoryService repositoryService;
    private final JwtProvider jwtProvider;

    @Autowired
    public JwtFilter(RepositoryService repositoryService, JwtProvider jwtProvider) {
        this.repositoryService = repositoryService;
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws ServletException, IOException {

        if(!isWebSocketRequest(request)) {
            String header = request.getHeader(HttpHeaders.AUTHORIZATION);

            log.info("Authorization: " + header);

            String token = null;
            String username = null;

            if (header != null && jwtProvider.isBearerToken(header)) {
                token = header.substring(7);
                username = jwtProvider.getUserId(token);
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtProvider.validateToken(token, username)) {
                    UsernamePasswordAuthenticationToken UPToken = new UsernamePasswordAuthenticationToken(username, null, null);
                    Authentication authentication = createAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } else if (token == null) {
                response.setHeader("Authorization", jwtProvider.createToken(repositoryService.createUser()));
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isWebSocketRequest(HttpServletRequest request) {

        return request.getRequestURI().startsWith("/ws");
    }

    private Authentication createAuthentication(String token) {
        String userId = jwtProvider.getUserId(token);
        return new UsernamePasswordAuthenticationToken("userId", userId);
    }
}
