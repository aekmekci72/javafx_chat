package sockets;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.SocketException;
import java.util.List;

public class ChatServerSocketListener  implements Runnable {
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
        System.out.println("Kick received from " + client.getUserName() + " to kick " + m.getUserName()+ " - broadcasting");
        broadcast(new MessageStoC_Kick(client.getUserName(), m.getUserName()));
    }

    private void processDirectMessage(MessageCtoS_DM m) {
        System.out.println("DM request received from " + client.getUserName() + " - responding");

        for (ClientConnectionData clientConnectionData : clientList) {
            if (clientConnectionData.getUserName().toLowerCase().trim().equals(m.userName.toLowerCase().trim())) {
                respond(new MessageStoC_DM(m.msg, client.getUserName()), clientConnectionData);
                return;
            }
        }

        System.out.println("DM failed - could not find user");
    }

    /**
     * Broadcasts a message to all clients connected to the server.
     */
    public void broadcast(Message m) {
        try {
            System.out.println("broadcasting: " + m);
            for (ClientConnectionData c : clientList){
                // if c equals skipClient, then c.
                // or if c hasn't set a userName yet (still joining the server)
                if (c.getUserName()!= null){
                    c.getOut().writeObject(m);
                }
            }
        } catch (Exception ex) {
            System.out.println("broadcast caught exception: " + ex);
            ex.printStackTrace();
        }        
    }

    public void respond(Message m, ClientConnectionData client) {
        try {
            System.out.println("responding: " + m);
            client.getOut().writeObject(m);
        } catch (Exception ex) {
            System.out.println("broadcast caught exception: " + ex);
            ex.printStackTrace();
        }        
    }

    @Override
    public void run() {
        try {
            ObjectInputStream in = client.getInput();

            MessageCtoS_Join joinMessage = (MessageCtoS_Join)in.readObject();
            client.setUserName(joinMessage.userName);

            // Broadcast the welcome back to the client that joined. 
            // Their UI can decide what to do with the welcome message.
            broadcast(new MessageStoC_Welcome(joinMessage.userName));
            
            while (true) {
                Message msg = (Message) in.readObject();
                if (msg instanceof MessageCtoS_Quit) {
                    break;
                }
                else if (msg instanceof MessageCtoS_Chat) {
                    processChatMessage((MessageCtoS_Chat) msg);
                }
                else if (msg instanceof MessageCtoS_Kick) {
                    processKickMessage((MessageCtoS_Kick) msg);
                }
                else if (msg instanceof MessageCtoS_DM) {
                    processDirectMessage((MessageCtoS_DM) msg);
                }
                else {
                    System.out.println("Unhandled message type: " + msg.getClass());
                }
            }
        } catch (Exception ex) {
            if (ex instanceof SocketException) {
                System.out.println("Caught socket ex for " + 
                    client.getName());
            } else {
                System.out.println(ex);
                ex.printStackTrace();
            }
        } finally {
            //Remove client from clientList
            clientList.remove(client); 

            // Notify everyone that the user left.
            broadcast(new MessageStoC_Exit(client.getUserName()));

            try {
                client.getSocket().close();
            } catch (IOException ex) {}
        }
    }
        
}
