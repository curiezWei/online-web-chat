package site.curiez.onlinewebchat.mapper;

import org.apache.ibatis.annotations.Mapper;
import site.curiez.onlinewebchat.model.Friend;

import java.util.List;

@Mapper
public interface FriendMapper {
    List<Friend> selectFriendList(int userId);
}
