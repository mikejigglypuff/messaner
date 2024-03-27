package messaner.config;

import org.apache.tomcat.util.http.parser.Authorization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    //스프링은 개발자의 의도에 따른 최적의 CSP 값을 알 수 없기에 직접 설정해야 함
    //self는 '로 감싸서 사용할 것, 지시자별로 ;로 나누어서 사용할 것
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        //사용할 url들 정한 후 각 url들에 대한 csp 설정 적용하기
        StringBuilder sb = new StringBuilder();
        sb.append("connect-src 'self'; ");
        sb.append("script-src 'self'; ");
        sb.append("child-src 'self'; ");
        sb.append("img-src 'none'; ");
        sb.append("reflected-xss: 1; ");

        //csrfFilter에 적용할 구현체들 정하기
        http
            .csrf((csrf) -> {
                csrf
                    .csrfTokenRepository(new HttpSessionCsrfTokenRepository())
                    .csrfTokenRequestHandler(new XorCsrfTokenRequestAttributeHandler());
            })
            .exceptionHandling((exceptionHandling) -> {
            exceptionHandling.accessDeniedPage("/access-denied");
        }).headers(headers -> headers.contentSecurityPolicy(csp ->
                csp.policyDirectives(sb.toString())
        ));

        return http.build();
    }


}