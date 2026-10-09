package site.curiez.onlinewebchat.mapper;

import org.apache.ibatis.annotations.Mapper;
import site.curiez.onlinewebchat.model.Friend;
import site.curiez.onlinewebchat.model.MessageSession;
import site.curiez.onlinewebchat.model.MessageSessionUserItem;

import java.util.List;

@Mapper
public interface MessageSessionMapper {
    List<Integer> getSessionIdsByUserId(int userId);

    List<Friend> getFriendsBySessionId(int sessionId,int selfUserId);

    int addMessageSession(MessageSession messageSession);

    void addMessageSessionUser(MessageSessionUserItem messageSessionUserItem);

    List<Integer> getSessionUserIds(int sessionId);

    int deleteSessionUsers(int sessionId);

    int deleteSession(int sessionId);
}
