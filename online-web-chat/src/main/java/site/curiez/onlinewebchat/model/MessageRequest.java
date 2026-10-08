package site.curiez.onlinewebchat.model;


import lombok.Data;

@Data
public class MessageRequest {
    private String type = "message";
    private int sessionId;
    private String content;
}
