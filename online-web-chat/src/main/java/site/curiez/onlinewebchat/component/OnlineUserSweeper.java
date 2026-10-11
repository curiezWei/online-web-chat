package site.curiez.onlinewebchat.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;

/**
 * 定时给在线连接发心跳 Ping（浏览器会自动回 Pong），并回收长时间无响应的僵尸连接。
 * 这样即使浏览器被强杀/断网/休眠导致关闭帧丢失，也能在超时后把在线状态释放，避免防顶号锁死。
 */
@Slf4j
@Component
public class OnlineUserSweeper {

    /** 心跳间隔（毫秒） */
    private static final long PING_INTERVAL_MS = 20_000L;

    /** 超过该时长未活跃则回收（毫秒） */
    private static final long STALE_TIMEOUT_MS = 60_000L;

    @Autowired
    private OnlineUserManager onlineUserManager;

    @Scheduled(fixedDelay = PING_INTERVAL_MS)
    public void pingAndSweep() {
        for (Map.Entry<Integer, WebSocketSession> entry : onlineUserManager.getSessions().entrySet()) {
            WebSocketSession session = entry.getValue();
            if (session == null || !session.isOpen()) {
                continue;
            }
            try {
                synchronized (session) {
                    session.sendMessage(new PingMessage());
                }
            } catch (Exception e) {
                log.warn("心跳 ping 失败，userId:" + entry.getKey(), e);
            }
        }
        onlineUserManager.sweep(STALE_TIMEOUT_MS);
    }
}
