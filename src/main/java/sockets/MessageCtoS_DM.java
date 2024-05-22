package sockets;

public class MessageCtoS_DM extends Message {
    public String msg;
    public String userName;

    public MessageCtoS_DM(String msg) {
        this.userName = msg.split(":")[0].substring(3).trim();
        this.msg = msg.split(":")[1].trim();
    }
    
}