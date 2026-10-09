package site.curiez.onlinewebchat.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import site.curiez.onlinewebchat.model.User;

import java.util.List;

@Mapper
public interface UserMapper {

    int insert(User user);

    User selectByName(String userName);

    List<User> searchByName(@Param("keyword") String keyword, @Param("selfId") int selfId);
}
