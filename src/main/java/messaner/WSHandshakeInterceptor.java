package messaner;

/*
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
public class WSHandshakeInterceptor implements HandshakeInterceptor {
    private final JwtProvider jwtProvider;
    
    @Autowired
    public WSHandshakeInterceptor(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        log.info(request.getHeaders().toString());
        String token = request.getHeaders().getFirst("Authorization");

        log.info(token);
        if(token != null) {
            String name = jwtProvider.getUserId(token);
            return jwtProvider.validateToken(token, name);
        } else {
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
 */