package site.curiez.onlinewebchat.model;

import lombok.Data;
import site.curiez.onlinewebchat.enums.ResultStatus;

@Data
public class Result<T> {
    //业务码
    private ResultStatus code;
    //错误信息
    private String errorMessage;
    //接口响应数据
    private T data;

    public static <T> Result<T> success(T data) {
        Result result = new Result();
        result.setCode(ResultStatus.SUCCESS);
        result.setErrorMessage("");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> fail(String errorMessage) {
        Result result = new Result();
        result.setCode(ResultStatus.FAIL);
        result.setErrorMessage(errorMessage);
        result.setData(null);
        return result;
    }

    public static <T> Result<T> fail(String errorMessage,T data) {
        Result result = new Result();
        result.setCode(ResultStatus.FAIL);
        result.setErrorMessage(errorMessage);
        result.setData(data);
        return result;
    }
}
