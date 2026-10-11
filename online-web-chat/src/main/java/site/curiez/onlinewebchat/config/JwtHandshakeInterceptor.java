package site.curiez.onlinewebchat.config;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import site.curiez.onlinewebchat.model.User;
import site.curiez.onlinewebchat.utils.JwtUtils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String token = null;
        String query = request.getURI().getQuery();
        if (query != null) {
            for (String pair : query.split("&")) {
                int index = pair.indexOf('=');
                if (index > 0 && "token".equals(pair.substring(0, index))) {
                    token = URLDecoder.decode(pair.substring(index + 1), StandardCharsets.UTF_8);
                    break;
                }
            }
        }
        Claims claims = JwtUtils.parseToken(token);
        if (claims != null) {
            User user = new User();
            Object userId = claims.get("userId");
            if (userId instanceof Number) {
                user.setUserId(((Number) userId).intValue());
            }
            Object userName = claims.get("userName");
            if (userName != null) {
                user.setUserName(userName.toString());
            }
            attributes.put("user", user);
        } else {
            log.info("[JwtHandshakeInterceptor] 无效或缺失的 token");
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}
