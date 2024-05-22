package sockets;
import java.util.List;

public class MessageStoC_ExistingUsers extends Message {
    public List<String> userNames;

    public MessageStoC_ExistingUsers(List<String> userNames) {
        this.userNames = userNames;
    }
}
