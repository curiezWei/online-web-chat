package site.curiez.onlinewebchat.mapper;

import org.apache.ibatis.annotations.Mapper;
import site.curiez.onlinewebchat.model.Friend;

import java.util.List;

@Mapper
public interface MessageSessionMapper {
    List<Integer> getSessionIdsByUserId(int userId);

    List<Friend> getFriendsBySessionId(int sessionId,int selfUserId);
}
