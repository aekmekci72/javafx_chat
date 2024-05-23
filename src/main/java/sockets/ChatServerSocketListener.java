package sockets;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class ChatServerSocketListener implements Runnable {
    private ClientConnectionData client;
    private List<ClientConnectionData> clientList;

    public ChatServerSocketListener(ClientConnectionData client, List<ClientConnectionData> clientList) {
        this.client = client;
        this.clientList = clientList;
    }

    private void processChatMessage(MessageCtoS_Chat m) {
        System.out.println("Chat received from " + client.getUserName() + " - broadcasting");
        broadcast(new MessageStoC_Chat(client.getUserName(), m.msg));
    }

    private void processKickMessage(MessageCtoS_Kick m) {
        System.out.println("Kick received from " + client.getUserName() + " to kick " + m.getUserName() + " - broadcasting");
        broadcast(new MessageStoC_Kick(client.getUserName(), m.getUserName()));
        
        for (ClientConnectionData kickedClient : clientList) {
            if (kickedClient.getUserName().equals(m.getUserName())) {
                kickedClient.setKickStatus(true);
                try {
                    kickedClient.getSocket().close();
                } catch (IOException e) {
                    System.out.println("Error closing socket of kicked client: " + e.getMessage());
                }
                clientList.remove(kickedClient);
                break;
            }
        }
    }

    private void processDirectMessage(MessageCtoS_DM m) {
        System.out.println("DM request received from " + client.getUserName() + " - responding");

        for (ClientConnectionData clientConnectionData : clientList) {
            if (clientConnectionData.getUserName().equalsIgnoreCase(m.userName.trim())) {
                respond(new MessageStoC_DMreciever(m.msg, client.getUserName()), clientConnectionData);
                respond(new MessageStoC_DMsender(m.msg, client.getUserName()), client);
                return;
            }
        }

        System.out.println("DM failed - could not find user");
    }

    private void processQuitMessage() {
        if (!client.getKickStatus()) { 
            System.out.println(client.getUserName() + " has left the chat.");
            clientList.remove(client);
            broadcast(new MessageStoC_Exit(client.getUserName()));
        }
        try {
            client.getSocket().close();
        } catch (IOException ex) {
            System.out.println("Exception closing socket: " + ex.getMessage());
        }
    }
    

    /**
     * Broadcasts a message to all clients connected to the server.
     */
    public void broadcast(Message m) {
        try {
            System.out.println("Broadcasting: " + m);
            for (ClientConnectionData c : clientList) {
                if (c.getUserName() != null) {
                    c.getOut().writeObject(m);
                }
            }
        } catch (Exception ex) {
            System.out.println("Broadcast caught exception: " + ex);
            ex.printStackTrace();
        }
    }

    public void respond(Message m, ClientConnectionData client) {
        try {
            System.out.println("Responding: " + m);
            client.getOut().writeObject(m);
        } catch (Exception ex) {
            System.out.println("Respond caught exception: " + ex);
            ex.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            ObjectInputStream in = client.getInput();
            MessageCtoS_Join joinMessage = (MessageCtoS_Join) in.readObject();
            client.setUserName(joinMessage.userName);

            // Broadcast the welcome message to all clients
            broadcast(new MessageStoC_Welcome(joinMessage.userName));

            // Send the list of existing users to the new client
            List<String> existingUsers = getUserNames();
            respond(new MessageStoC_ExistingUsers(existingUsers), client);

            while (true) {
                Message msg = (Message) in.readObject();
                if (msg instanceof MessageCtoS_Quit) {
                    processQuitMessage();
                    break;
                } else if (msg instanceof MessageCtoS_Chat) {
                    processChatMessage((MessageCtoS_Chat) msg);
                } else if (msg instanceof MessageCtoS_Kick) {
                    processKickMessage((MessageCtoS_Kick) msg);
                } else if (msg instanceof MessageCtoS_DM) {
                    processDirectMessage((MessageCtoS_DM) msg);
                } else {
                    System.out.println("Unhandled message type: " + msg.getClass());
                }
            }
        } catch (Exception ex) {
            if (ex instanceof SocketException) {
                System.out.println("Caught socket exception for " + client.getUserName());
            } else {
                System.out.println("Exception: " + ex);
                ex.printStackTrace();
            }
        } finally {
            clientList.remove(client);
            broadcast(new MessageStoC_Exit(client.getUserName()));
            try {
                client.getSocket().close();
            } catch (IOException ex) {
                System.out.println("Exception closing socket: " + ex.getMessage());
            }
        }
    }

    public List<String> getUserNames() {
        List<String> usernames = new ArrayList<>();
        for (ClientConnectionData client : clientList) {
            if (client.getUserName() != null) {
                usernames.add(client.getUserName());
            }
        }
        return usernames;
    }
}
