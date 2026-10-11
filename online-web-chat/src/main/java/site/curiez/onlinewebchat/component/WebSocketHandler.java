package site.curiez.onlinewebchat.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import site.curiez.onlinewebchat.mapper.MessageMapper;
import site.curiez.onlinewebchat.mapper.MessageSessionMapper;
import site.curiez.onlinewebchat.model.*;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class WebSocketHandler extends TextWebSocketHandler {
    @Autowired
    private OnlineUserManager onlineUserManager;

    @Autowired
    private MessageSessionMapper messageSessionMapper;

    @Autowired
    private MessageMapper messageMapper;

    private ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("[WebSocketAPI] 连接成功～");
        User user = (User) session.getAttributes().get("user");
        if(user==null) {
            return;
        }
        onlineUserManager.online(user.getUserId(),session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("[WebSocketAPI] 收到消息～");
        //deal with the receipt message
        User user = (User) session.getAttributes().get("user");
        if(user==null) {
            log.info("user==null，未登录");
            return;
        }
        onlineUserManager.markActive(user.getUserId());
        MessageRequest request = objectMapper.readValue(message.getPayload(), MessageRequest.class);
        if(request.getType().equals("message")) {
            transferMessage(user,request);
        }else {
            log.info("message type 有误"+message.getPayload());
        }
    }

    private void transferMessage(User fromUser, MessageRequest request) throws IOException {
        // 先落库，保证消息不丢失
        Message message = new Message();
        message.setFromId(fromUser.getUserId());
        message.setSessionId(request.getSessionId());
        message.setContent(request.getContent());
        messageMapper.add(message);

        MessageResponse response = new MessageResponse();
        response.setType("message");
        response.setFromName(fromUser.getUserName());
        response.setFromId(fromUser.getUserId());
        response.setSessionId(request.getSessionId());
        response.setContent(request.getContent());
        response.setPostTime(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
        String responseJson = objectMapper.writeValueAsString(response);

        log.info("responseJson:"+responseJson);

        List<Friend> friends = messageSessionMapper.getFriendsBySessionId(request.getSessionId(), fromUser.getUserId());

        for(Friend friend : friends) {
            if(friend.getFriendId() == fromUser.getUserId()) {
                continue;
            }
            WebSocketSession webSocketSession = onlineUserManager.getWebSocketSession(friend.getFriendId());
            if(webSocketSession == null) {
                continue;
            }
            if(!webSocketSession.isOpen()) {
                onlineUserManager.offline(friend.getFriendId(), webSocketSession);
                continue;
            }
            try {
                synchronized (webSocketSession) {
                    webSocketSession.sendMessage(new TextMessage(responseJson));
                }
            } catch (Exception e) {
                log.warn("推送消息失败，userId：" + friend.getFriendId(), e);
                onlineUserManager.offline(friend.getFriendId(), webSocketSession);
            }
        }
    }

    @Override
    public void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
        User user = (User) session.getAttributes().get("user");
        if(user != null) {
            onlineUserManager.markActive(user.getUserId());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.info("[WebSocketAPI] 连接异常！");
        User user = (User) session.getAttributes().get("user");
        if(user==null) {
            return;
        }
        onlineUserManager.offline(user.getUserId(),session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("[WebSocketAPI] 连接关闭～");
        User user = (User) session.getAttributes().get("user");
        if(user==null) {
            return;
        }
        onlineUserManager.offline(user.getUserId(),session);
    }
}
