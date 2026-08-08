package site.curiez.onlinewebchat.model;

import lombok.Data;

@Data
public class User {
    private int userId;
    private String userName = "";
    private String password = "";

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
