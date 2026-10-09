package site.curiez.onlinewebchat.mapper;

import org.apache.ibatis.annotations.Mapper;
import site.curiez.onlinewebchat.model.FriendRequest;

import java.util.List;

@Mapper
public interface FriendRequestMapper {

    int add(FriendRequest friendRequest);

    List<FriendRequest> selectByToId(int toId);

    FriendRequest selectById(int requestId);

    int deleteById(int requestId);
}
