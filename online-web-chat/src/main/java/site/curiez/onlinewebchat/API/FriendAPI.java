package site.curiez.onlinewebchat.API;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import site.curiez.onlinewebchat.component.OnlineUserManager;
import site.curiez.onlinewebchat.mapper.FriendMapper;
import site.curiez.onlinewebchat.mapper.FriendRequestMapper;
import site.curiez.onlinewebchat.mapper.MessageMapper;
import site.curiez.onlinewebchat.mapper.MessageSessionMapper;
import site.curiez.onlinewebchat.mapper.UserMapper;
import site.curiez.onlinewebchat.model.Friend;
import site.curiez.onlinewebchat.model.FriendRequest;
import site.curiez.onlinewebchat.model.User;
import site.curiez.onlinewebchat.utils.JwtUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class FriendAPI {
    @Autowired
    private FriendMapper friendMapper;

    @Autowired
    private FriendRequestMapper friendRequestMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private MessageSessionMapper messageSessionMapper;

    @Autowired
    private OnlineUserManager onlineUserManager;

    @GetMapping("/friendList")
    public List<Friend> getFriendList(HttpServletRequest request) {
        User user = JwtUtils.getUser(request);
        if(user==null) {
            log.info("[getFriendList]当前用户未登录");
            return new ArrayList<Friend>();
        }
        List<Friend> friends = friendMapper.selectFriendList(user.getUserId());
        return friends;
    }

    @GetMapping("/searchUser")
    public List<User> searchUser(String userName, HttpServletRequest request) {
        List<User> result = new ArrayList<>();
        User user = JwtUtils.getUser(request);
        if (user == null) {
            log.info("[searchUser]当前用户未登录");
            return result;
        }
        if (userName == null || userName.trim().isEmpty()) {
            return result;
        }
        result = userMapper.searchByName(userName.trim(), user.getUserId());
        return result;
    }

    @PostMapping("/addFriend")
    public Map<String, Object> addFriend(int toUserId, HttpServletRequest request) {
        Map<String, Object> res = new HashMap<>();
        User user = JwtUtils.getUser(request);
        if (user == null) {
            res.put("code", 0);
            res.put("msg", "当前未登录");
            return res;
        }
        if (toUserId == user.getUserId()) {
            res.put("code", 0);
            res.put("msg", "不能添加自己为好友");
            return res;
        }
        if (friendMapper.checkFriend(user.getUserId(), toUserId) > 0) {
            res.put("code", 0);
            res.put("msg", "你们已经是好友了");
            return res;
        }
        try {
            FriendRequest friendRequest = new FriendRequest();
            friendRequest.setFromId(user.getUserId());
            friendRequest.setToId(toUserId);
            friendRequestMapper.add(friendRequest);
            log.info("好友申请已发送，from:" + user.getUserId() + "，to:" + toUserId);
            res.put("code", 1);
            res.put("msg", "好友申请已发送");
        } catch (DuplicateKeyException e) {
            res.put("code", 0);
            res.put("msg", "已发送过申请，请勿重复发送");
        }
        return res;
    }

    @GetMapping("/friendRequestList")
    public List<FriendRequest> getFriendRequestList(HttpServletRequest request) {
        List<FriendRequest> result = new ArrayList<>();
        User user = JwtUtils.getUser(request);
        if (user == null) {
            log.info("[getFriendRequestList]当前用户未登录");
            return result;
        }
        result = friendRequestMapper.selectByToId(user.getUserId());
        return result;
    }

    @PostMapping("/handleFriendRequest")
    @Transactional
    public Map<String, Object> handleFriendRequest(int requestId, boolean accept, HttpServletRequest request) {
        Map<String, Object> res = new HashMap<>();
        User user = JwtUtils.getUser(request);
        if (user == null) {
            res.put("code", 0);
            res.put("msg", "当前未登录");
            return res;
        }
        FriendRequest friendRequest = friendRequestMapper.selectById(requestId);
        if (friendRequest == null || friendRequest.getToId() != user.getUserId()) {
            res.put("code", 0);
            res.put("msg", "该申请不存在或无权处理");
            return res;
        }
        if (accept && friendMapper.checkFriend(friendRequest.getFromId(), friendRequest.getToId()) == 0) {
            friendMapper.addFriend(friendRequest.getFromId(), friendRequest.getToId());
            friendMapper.addFriend(friendRequest.getToId(), friendRequest.getFromId());
        }
        friendRequestMapper.deleteById(requestId);
        log.info("处理好友申请，requestId:" + requestId + "，accept:" + accept);
        if (accept) {
            notifyFriendAccepted(friendRequest.getFromId());
        }
        res.put("code", 1);
        res.put("msg", accept ? "已通过好友申请" : "已拒绝好友申请");
        return res;
    }

    private void notifyFriendAccepted(int userId) {
        WebSocketSession webSocketSession = onlineUserManager.getWebSocketSession(userId);
        if (webSocketSession == null || !webSocketSession.isOpen()) {
            return;
        }
        try {
            synchronized (webSocketSession) {
                webSocketSession.sendMessage(new TextMessage("{\"type\":\"friendAccepted\"}"));
            }
        } catch (Exception e) {
            log.warn("通知好友申请通过失败，userId:" + userId, e);
        }
    }

    @PostMapping("/deleteFriend")
    @Transactional
    public Map<String, Object> deleteFriend(int sessionId, HttpServletRequest request) {
        Map<String, Object> res = new HashMap<>();
        User user = JwtUtils.getUser(request);
        if (user == null) {
            res.put("code", 0);
            res.put("msg", "当前未登录");
            return res;
        }
        List<Integer> sessionUserIds = messageSessionMapper.getSessionUserIds(sessionId);
        if (sessionUserIds == null || sessionUserIds.isEmpty()) {
            res.put("code", 0);
            res.put("msg", "会话不存在");
            return res;
        }
        for (int sessionUserId : sessionUserIds) {
            if (sessionUserId != user.getUserId()) {
                friendMapper.deleteFriend(user.getUserId(), sessionUserId);
            }
        }
        messageMapper.deleteBySessionId(sessionId);
        messageSessionMapper.deleteSessionUsers(sessionId);
        messageSessionMapper.deleteSession(sessionId);
        log.info("删除好友并清理会话，sessionId:" + sessionId + "，userId:" + user.getUserId());
        res.put("code", 1);
        res.put("msg", "已删除好友");
        return res;
    }

}
