package sockets;

public class MessageCtoS_Kick extends Message {
    private String userName;
    public MessageCtoS_Kick(String userName){
        this.userName=userName;
    }
    public String getUserName(){
        return userName;
    }
}
