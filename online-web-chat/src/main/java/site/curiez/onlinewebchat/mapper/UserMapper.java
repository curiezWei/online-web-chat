package site.curiez.onlinewebchat.mapper;

import org.apache.ibatis.annotations.Mapper;
import site.curiez.onlinewebchat.model.User;

@Mapper
public interface UserMapper {

    int insert(User user);

    User selectByName(String userName);
}
