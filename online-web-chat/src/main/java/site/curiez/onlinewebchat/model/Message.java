package site.curiez.onlinewebchat.model;

import lombok.Data;

@Data
public class Message {
    private int messageId;
    private int fromId;
    private int sessionId;
    private String fromName;
    private String content;
}
