package sockets;

public class MessageStoC_DMreciever extends Message {
    public String msg;
    public String messanger;

    public MessageStoC_DMreciever(String msg, String messanger) {
        this.messanger = messanger;
        this.msg = msg;
    }

    public String toString() {
        return messanger+" is DMing somebody: "+msg;
    }

}