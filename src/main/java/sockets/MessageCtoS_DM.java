package sockets;

public class MessageCtoS_DM extends Message {
    public String msg;
    public String userName;

    public MessageCtoS_DM(String userName, String msg) {
        this.userName = userName;
        this.msg = msg;
    }
}
