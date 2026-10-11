package site.curiez.onlinewebchat.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import site.curiez.onlinewebchat.model.User;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Slf4j
public class JwtUtils {
    private static String secret = "5jLnJChxR8N1+pZuFkjCObhuxRis14t875Hp5QtEbgQ=";

    private static long EXPIRATION = 24*60*60*1000;

    private static SecretKey secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));

    public static String getJwtToken (Map<String,Object> claim) {
        String token = Jwts.builder().setClaims(claim)
                .setExpiration(new Date(System.currentTimeMillis()+EXPIRATION))
                .signWith(secretKey)
                .compact();
        return token;
    }

    public static Claims parseToken (String token) {
        JwtParser build = Jwts.parserBuilder().setSigningKey(secretKey).build();
        Claims claims = null;
        try{
            claims = build.parseClaimsJws(token).getBody();
        }catch (Exception e) {
            log.error("解析token失败，token:{}",token);
            return null;
        }
        return claims;
    }

    public static User getUser(HttpServletRequest request) {
        String token = null;
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            token = authorization.substring(7);
        }
        if (token == null) {
            token = request.getHeader("token");
        }
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        User user = new User();
        Object userId = claims.get("userId");
        if (userId instanceof Number) {
            user.setUserId(((Number) userId).intValue());
        }
        Object userName = claims.get("userName");
        if (userName != null) {
            user.setUserName(userName.toString());
        }
        return user;
    }
}
