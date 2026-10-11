package site.curiez.onlinewebchat.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class OnlineUserManager {

    private final ConcurrentHashMap<Integer, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Long> lastActive = new ConcurrentHashMap<>();

    public void online(int userId, WebSocketSession session) {
        WebSocketSession oldSession = sessions.put(userId, session);
        lastActive.put(userId, System.currentTimeMillis());
        if (oldSession != null && oldSession != session && oldSession.isOpen()) {
            closeQuietly(oldSession);
        }
        log.info("用户id：" + userId + " 上线");
    }

    public void offline(int userId, WebSocketSession session) {
        WebSocketSession currentSession = sessions.get(userId);
        if (currentSession == session) {
            sessions.remove(userId);
            lastActive.remove(userId);
            log.info("用户id：" + userId + "下线");
        }
    }

    public WebSocketSession getWebSocketSession(int userId) {
        return sessions.get(userId);
    }

    public Map<Integer, WebSocketSession> getSessions() {
        return sessions;
    }

    public void markActive(int userId) {
        lastActive.put(userId, System.currentTimeMillis());
    }

    /**
     * 是否在线：连接存在且仍然打开；发现已关闭的连接顺手清理。
     */
    public boolean isOnline(int userId) {
        WebSocketSession session = sessions.get(userId);
        if (session == null) {
            return false;
        }
        if (!session.isOpen()) {
            sessions.remove(userId, session);
            lastActive.remove(userId);
            return false;
        }
        return true;
    }

    /**
     * 清理僵尸连接：连接已关闭，或超过 timeoutMillis 未活跃（未收到心跳/消息）。
     */
    public void sweep(long timeoutMillis) {
        long now = System.currentTimeMillis();
        for (Integer userId : sessions.keySet()) {
            WebSocketSession session = sessions.get(userId);
            if (session == null) {
                lastActive.remove(userId);
                continue;
            }
            Long last = lastActive.get(userId);
            boolean dead = !session.isOpen()
                    || last == null
                    || (now - last) > timeoutMillis;
            if (dead && sessions.remove(userId, session)) {
                lastActive.remove(userId);
                closeQuietly(session);
                log.info("回收失效连接 userId:" + userId);
            }
        }
    }

    private void closeQuietly(WebSocketSession session) {
        try {
            session.close();
        } catch (IOException e) {
            log.warn("关闭连接失败", e);
        }
    }
}
