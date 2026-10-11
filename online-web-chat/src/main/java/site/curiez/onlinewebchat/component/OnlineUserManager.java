package site.curiez.onlinewebchat.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
@Slf4j
@Component
public class OnlineUserManager {

    private ConcurrentHashMap<Integer,WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void online(int userId, WebSocketSession session){
        WebSocketSession oldSession = sessions.put(userId, session);
        if(oldSession != null && oldSession != session && oldSession.isOpen()) {
            try {
                oldSession.close();
            } catch (IOException e) {
                log.warn("关闭旧连接失败，userId：" + userId, e);
            }
        }
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

    public boolean isOnline(int userId) {
        return sessions.containsKey(userId);
    }
}
