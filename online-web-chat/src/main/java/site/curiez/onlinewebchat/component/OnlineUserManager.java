package site.curiez.onlinewebchat.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;
@Slf4j
@Component
public class OnlineUserManager {

    private ConcurrentHashMap<Integer,WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void online(int userId, WebSocketSession session){
        if(sessions.get(userId) != null) {
            log.info("用户id："+userId+" 在其他地方登录了");
            return ;
        }
        sessions.put(userId,session);
        log.info("用户id："+userId+" 上线");
    }

    public void offline(int userId,WebSocketSession session) {
        WebSocketSession currentSession = sessions.get(userId);
        if(currentSession==session) {
            sessions.remove(userId);
            log.info("用户id："+userId+"下线");
        }
    }

    public WebSocketSession getWebSocketSession(int userId) {
        return sessions.get(userId);
    }
}
