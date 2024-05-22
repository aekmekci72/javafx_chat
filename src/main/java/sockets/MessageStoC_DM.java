package sockets;

public class MessageStoC_DM extends Message {
    public String msg;
    public String messanger;

    public MessageStoC_DM(String msg, String messanger) {
        this.messanger = messanger;
        this.msg = msg;
    }

    public String toString() {
        return messanger+" is DMing somebody: "+msg;
    }

}