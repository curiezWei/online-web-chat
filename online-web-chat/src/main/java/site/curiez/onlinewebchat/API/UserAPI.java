package site.curiez.onlinewebchat.API;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import site.curiez.onlinewebchat.mapper.UserMapper;
import site.curiez.onlinewebchat.model.User;

@Slf4j
@RestController
public class UserAPI {

    @Autowired
    private UserMapper userMapper;


    @PostMapping("/login")
    public User login(String userName, String password, HttpServletRequest request) {
        User user = userMapper.selectByName(userName);
        if (user == null || !user.getPassword().equals(password)) {
            user.setPassword("");
            log.info("登录失败！用户名或密码错误！" + user);
            return new User();
        }
        HttpSession session = request.getSession(true);
        session.setAttribute("user", user);
        user.setPassword("");
        log.info("登录成功！");
        return user;
    }

    @PostMapping("register")
    public User register(String userName, String password) {
        User user = null;
        try {
            user = new User();
            user.setUserName(userName);
            user.setPassword(password);
            int result = userMapper.insert(user);
            log.info("注册成功，改变行数:" + result + "，uid:" + user.getUserId());
            user.setPassword("");
        } catch (DuplicateKeyException e) {
            user = new User();
            log.info("注册失败！userName:" + user.getUserName());
        }
        return user;
    }

    @GetMapping("/userInfo")
    public User getUserInfo(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if(session == null) {
            log.info("[getUserInfo]当前获取不到session对象");
            return new User();
        }
        User user = (User) session.getAttribute("user");
        if(user==null) {
            log.info("[getUserInfo]当前获取不到session对象");
            return new User();
        }
        user.setPassword("");
        return user;
    }
}
