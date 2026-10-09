package site.curiez.onlinewebchat.model;

import lombok.Data;

@Data
public class FriendRequest {
    private int requestId;
    private int fromId;
    private String fromName;
    private int toId;
}
