package sockets;

import java.io.ObjectInputStream;
import java.util.Optional;
import javafx.application.Platform;
import javafx.scene.control.TextInputDialog;

public class ChatGuiSocketListener implements Runnable {

    private ObjectInputStream socketIn;
    private ChatGuiClient chatGuiClient;
    private String username = null;

    volatile boolean appRunning = false;

    public ChatGuiSocketListener(ObjectInputStream socketIn, ChatGuiClient chatClient) {
        this.socketIn = socketIn;
        this.chatGuiClient = chatClient;
    }

    private void processWelcomeMessage(MessageStoC_Welcome m) {
        String user = m.userName;

        if (user.equals(this.username)) {
            Platform.runLater(() -> {
                chatGuiClient.getStage().setTitle("Chatter - " + username);
                chatGuiClient.getTextInput().setEditable(true);
                chatGuiClient.getSendButton().setDisable(false);
                chatGuiClient.getMessageArea().appendText("Welcome to the chat, " + username + "\n");
            });
        } else {
            Platform.runLater(() -> {
                chatGuiClient.getMessageArea().appendText(m.userName + " joined the chat!\n");
                chatGuiClient.addClient(m.userName);
            });
        }
    }

    private void processChatMessage(MessageStoC_Chat m) {
        Platform.runLater(() -> {
            chatGuiClient.getMessageArea().appendText(m.userName + ": " + m.msg + "\n");
        });
    }

    private void processKickMessage(MessageStoC_Kick m) {
        Platform.runLater(() -> {
            chatGuiClient.getMessageArea().appendText(m.getKickerName() + " kicked " + m.getUserName() + "\n");
        });
    }

    private void processDirectMessage(MessageStoC_DM m) {
        Platform.runLater(() -> {
            chatGuiClient.getMessageArea().appendText(m.messanger + " DMed you: " + m.msg + "\n");
        });
    }

    private void processExitMessage(MessageStoC_Exit m) {
        Platform.runLater(() -> {
            chatGuiClient.getMessageArea().appendText(m.userName + " has left the chat!\n");
            chatGuiClient.removeClient(m.userName);
        });
    }

    private void processExistingUsersMessage(MessageStoC_ExistingUsers m) {
        Platform.runLater(() -> {
            for (String user : m.userNames) {
                chatGuiClient.addClient(user);
            }
        });
    }

    public void run() {
        try {
            appRunning = true;

            Platform.runLater(() -> {
                this.username = getName();
                chatGuiClient.sendMessage(new MessageCtoS_Join(username));
            });

            while (appRunning) {
                Message msg = (Message) socketIn.readObject();

                if (msg instanceof MessageStoC_Welcome) {
                    processWelcomeMessage((MessageStoC_Welcome) msg);
                } else if (msg instanceof MessageStoC_Chat) {
                    processChatMessage((MessageStoC_Chat) msg);
                } else if (msg instanceof MessageStoC_Kick) {
                    processKickMessage((MessageStoC_Kick) msg);
                } else if (msg instanceof MessageStoC_DM) {
                    processDirectMessage((MessageStoC_DM) msg);
                } else if (msg instanceof MessageStoC_Exit) {
                    processExitMessage((MessageStoC_Exit) msg);
                } else if (msg instanceof MessageStoC_ExistingUsers) {
                    processExistingUsersMessage((MessageStoC_ExistingUsers) msg);
                } else {
                    System.out.println("Unhandled message type: " + msg.getClass());
                }
            }
        } catch (Exception ex) {
            System.out.println("Exception caught in listener - " + ex);
        } finally {
            System.out.println("Client Listener exiting");
        }
    }

    private String getName() {
        String username = "";
        TextInputDialog nameDialog = new TextInputDialog();
        nameDialog.setTitle("Enter Chat Name");
        nameDialog.setHeaderText("Please enter your username.");
        nameDialog.setContentText("Name: ");

        while (username.equals("")) {
            Optional<String> name = nameDialog.showAndWait();
            if (!name.isPresent() || name.get().trim().equals(""))
                nameDialog.setHeaderText("You must enter a nonempty name: ");
            else
                username = name.get().trim();
        }
        return username;
    }
}
