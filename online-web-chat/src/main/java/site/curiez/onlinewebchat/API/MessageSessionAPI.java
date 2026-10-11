package site.curiez.onlinewebchat.API;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import site.curiez.onlinewebchat.mapper.MessageMapper;
import site.curiez.onlinewebchat.mapper.MessageSessionMapper;
import site.curiez.onlinewebchat.model.Friend;
import site.curiez.onlinewebchat.model.Message;
import site.curiez.onlinewebchat.model.MessageSession;
import site.curiez.onlinewebchat.model.MessageSessionUserItem;
import site.curiez.onlinewebchat.model.User;
import site.curiez.onlinewebchat.utils.JwtUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class MessageSessionAPI {
    @Autowired
    private MessageSessionMapper messageSessionMapper;

    @Autowired
    private MessageMapper messageMapper;

    @GetMapping("/sessionList")
    public List<MessageSession> getMessageSessionList(HttpServletRequest request) {
        //根据当前登录的用户信息，获取与该用户有关的sessionId（聊天会话id编号）
        List<MessageSession> messageSessionList = new ArrayList<>();
        User user = JwtUtils.getUser(request);
        if(user == null) {
            log.info("[getMessageSessionList] user == null");
            return messageSessionList;
        }
        //获取聊天会话id编号列表
        List<Integer> sessionIdList = messageSessionMapper.getSessionIdsByUserId(user.getUserId());


        for(int sessionId : sessionIdList){
            MessageSession messageSession = new MessageSession();
            messageSession.setSessionId(sessionId);
            List<Friend> friends = messageSessionMapper.getFriendsBySessionId(sessionId,user.getUserId());
            messageSession.setFriends(friends);
            Message last = messageMapper.getLastMessageBySessionId(sessionId);
            if (last == null) {
                messageSession.setLastMessage("");
                messageSession.setLastTime("");
            } else {
                messageSession.setLastMessage(last.getContent());
                messageSession.setLastTime(last.getPostTime());
            }
            messageSessionList.add(messageSession);
        }
        return messageSessionList;
    }

    @PostMapping("/session")
    @Transactional
    public Map<String, Integer> addMessageSession(int toUserId, HttpServletRequest request) {
        HashMap<String,Integer> res = new HashMap<>();
        User user = JwtUtils.getUser(request);
        if(user == null) {
            log.info("[addMessageSession] user == null");
            res.put("sessionId", 0);
            return res;
        }

        // 若两人之间已存在会话则直接复用，避免重复建会话导致消息不同步
        Integer existSessionId = messageSessionMapper.findSessionByTwoUsers(user.getUserId(), toUserId);
        if(existSessionId != null) {
            res.put("sessionId", existSessionId);
            return res;
        }

        MessageSession messageSession = new MessageSession();
        messageSessionMapper.addMessageSession(messageSession);

        MessageSessionUserItem item1 = new MessageSessionUserItem();
        item1.setSessionId(messageSession.getSessionId());
        item1.setUserId(user.getUserId());
        messageSessionMapper.addMessageSessionUser(item1);

        MessageSessionUserItem item2 = new MessageSessionUserItem();
        item2.setSessionId(messageSession.getSessionId());
        item2.setUserId(toUserId);
        messageSessionMapper.addMessageSessionUser(item2);



        res.put("sessionId",messageSession.getSessionId());
        return res;
    }
}
