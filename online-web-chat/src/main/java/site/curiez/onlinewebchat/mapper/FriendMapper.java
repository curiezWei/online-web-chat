package site.curiez.onlinewebchat.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import site.curiez.onlinewebchat.model.Friend;

import java.util.List;

@Mapper
public interface FriendMapper {
    List<Friend> selectFriendList(int userId);

    int addFriend(@Param("userId") int userId, @Param("friendId") int friendId);

    int checkFriend(@Param("userId") int userId, @Param("friendId") int friendId);

    int deleteFriend(@Param("userId") int userId, @Param("friendId") int friendId);
}
