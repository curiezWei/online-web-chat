package site.curiez.onlinewebchat.model;

import lombok.Data;

@Data
public class MessageResponse {
    private String type = "message";
    private int fromId;
    private String fromName;
    private int sessionId;
    private String content;
}
