package sockets;

public class MessageStoC_DMsender extends Message {
    public String msg;
    public String messanged;

    public MessageStoC_DMsender(String msg, String messanged) {
        this.messanged = messanged;
        this.msg = msg;
    }

    public String toString() {
        return messanged+" DMed: "+msg;
    }

}