package site.curiez.onlinewebchat.API;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import site.curiez.onlinewebchat.mapper.FriendMapper;
import site.curiez.onlinewebchat.model.Friend;
import site.curiez.onlinewebchat.model.User;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
public class FriendAPI {
    @Autowired
    private FriendMapper friendMapper;

    @GetMapping("/friendList")
    public List<Friend> getFriendList(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if(session == null) {
            log.info("[getFriendList]当前用户会话不存在！");
            return new ArrayList<Friend>();
        }
        User user = (User) session.getAttribute("user");
        if(user==null) {
            log.info("[getFriendList]当前用户对象不在会话中");
            return new ArrayList<Friend>();
        }
        List<Friend> friends = friendMapper.selectFriendList(user.getUserId());
        return friends;
    }

}
