package sockets;

public class MessageStoC_Kick extends Message {
    private String userName;
    private String kickerName;

    public MessageStoC_Kick(String userName, String kickerName) {
        this.userName = userName;
        this.kickerName = kickerName;
    }

    public String getUserName() {
        return userName;
    }

    public String getKickerName() {
        return kickerName;
    }
}
