package site.curiez.onlinewebchat.API;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import site.curiez.onlinewebchat.component.OnlineUserManager;
import site.curiez.onlinewebchat.mapper.UserMapper;
import site.curiez.onlinewebchat.model.Result;
import site.curiez.onlinewebchat.model.User;
import site.curiez.onlinewebchat.utils.JwtUtils;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
public class UserAPI {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OnlineUserManager onlineUserManager;

    private final static String registerKey = "curiez";

    @PostMapping("/login")
    public Result<String> login(String userName, String password) {
        User user = userMapper.selectByName(userName);
        if (user == null || !user.getPassword().equals(password)) {
            log.info("登录失败！用户名或密码错误！");
            return Result.fail("用户名或密码错误");
        }
        if (onlineUserManager.isOnline(user.getUserId())) {
            log.info("登录失败！该账号已在其他地方登录，userId:" + user.getUserId());
            return Result.fail("该账号已在其他地方登录");
        }
        Map<String, Object> claim = new HashMap<>();
        claim.put("userId", user.getUserId());
        claim.put("userName", user.getUserName());
        String token = JwtUtils.getJwtToken(claim);
        log.info("登录成功！");
        return Result.success(token);
    }

    @PostMapping("register")
    public Result<User> register(String userName, String password, String key) {
        if (!registerKey.equals(key)) {
            log.info("注册失败！注册通行证不正确！");
            return Result.fail("注册通行证不正确");
        }
        try {
            User user = new User();
            user.setUserName(userName);
            user.setPassword(password);
            int result = userMapper.insert(user);
            log.info("注册成功，改变行数:" + result + "，uid:" + user.getUserId());
            user.setPassword("");
            return Result.success(user);
        } catch (DuplicateKeyException e) {
            log.info("注册失败！userName:" + userName);
            return Result.fail("用户名已被占用");
        }
    }

    @GetMapping("/userInfo")
    public Result<User> getUserInfo(HttpServletRequest request) {
        User user = JwtUtils.getUser(request);
        if (user == null) {
            log.info("[getUserInfo]无效或缺失的 token");
            return Result.fail("未登录");
        }
        return Result.success(user);
    }
}
