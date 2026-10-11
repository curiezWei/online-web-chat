package site.curiez.onlinewebchat.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import site.curiez.onlinewebchat.model.Result;

@Slf4j
@ResponseBody
@ControllerAdvice
public class ExceptionHandle {

    @ExceptionHandler(DataAccessException.class)
    public Result handleDataAccess(DataAccessException e) {
        log.error("数据库访问异常，请检查数据库是否启动及连接配置(profile/url/账号密码)：", e);
        return Result.fail("数据库连接失败，请检查数据库配置");
    }

    @ExceptionHandler
    public Result handle(Exception e) {
        log.error("发送异常：" + e);
        return Result.fail("内部错误！请联系我");
    }
}
