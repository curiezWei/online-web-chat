package site.curiez.onlinewebchat.API;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import site.curiez.onlinewebchat.mapper.MessageMapper;
import site.curiez.onlinewebchat.model.Message;

import java.util.Collections;
import java.util.List;

@RestController
public class MessageAPI {
    @Autowired
    private MessageMapper messageMapper;

    @GetMapping("/message")
    public List<Message> getMessage(int sessionId){
        List<Message> messages = messageMapper.getMessagesBySessionId(sessionId);
        Collections.reverse(messages);
        return messages;
    }
}
