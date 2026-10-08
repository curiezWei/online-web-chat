package site.curiez.onlinewebchat.mapper;

import org.apache.ibatis.annotations.Mapper;
import site.curiez.onlinewebchat.model.Message;

import java.util.List;

@Mapper
public interface MessageMapper {

    String getLastMessageBySessionId(int sessionId);

    List<Message> getMessagesBySessionId(int sessionId);

    void add(Message message);
}
