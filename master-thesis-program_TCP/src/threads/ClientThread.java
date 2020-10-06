package threads;

import com.badlogic.gdx.net.Socket;
import entity.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientThread extends Thread {
    static List<ClientThread> clientThreads = new ArrayList<>();
    private static List<MessageEntity> chatMessages = Collections.synchronizedList(new ArrayList<>());
    private int incomingAudioSignals;

    private Socket clientSocket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    private int clientId;
    private PlayerEntity player;
    private OtherPlayersEntity otherPlayers = new OtherPlayersEntity();
    private ChatMessagesEntity messagesToSend = new ChatMessagesEntity();

    public ClientThread(Socket clientSocket, int clientId) throws Exception {
        this.clientSocket = clientSocket;

        this.clientId = clientId;
        clientThreads.add(this);

        LocalTime time = LocalTime.now().withNano(0);
        chatMessages.add(new MessageEntity("SERVER", "[" + time + "]: Player" + clientId + " connected", new float[]{0, 1, 0}));

        oos = new ObjectOutputStream(clientSocket.getOutputStream());
        ois = new ObjectInputStream(clientSocket.getInputStream());
    }

    @Override
    public void run() {
        Object[] objectsResponse = new Object[3];
        try {
            while (true) {
                Object[] objects = (Object[]) ois.readObject();
                player = (PlayerEntity) objects[0];
                player.setId(clientId);

                otherPlayers.getPlayers().clear();
                for (ClientThread client : clientThreads) {
                    PlayerEntity otherPlayer = client.getPlayer();
                    if (otherPlayer != null && otherPlayer != player) {
                        otherPlayers.getPlayers().add(otherPlayer);
                        if (player.getNewAudioSignals() > 0) {
                            client.setIncomingAudioSignals(client.getIncomingAudioSignals() + player.getNewAudioSignals());
                        }
                    }
                }

                synchronized (chatMessages) {
                    if (chatMessages.size() >= 100) {
                        chatMessages = chatMessages.subList(60, chatMessages.size());
                    }
                }

                RawMessagesEntity newMessages = (RawMessagesEntity) objects[1];

                for (String newMessage : newMessages.getRawMessages()) {
                    LocalTime time = LocalTime.now().withNano(0);
                    chatMessages.add(new MessageEntity("Player " + player.getId(), "[" + time + "]: " + newMessage, new float[]{1, 1, 1}));
                }

                messagesToSend.getMessageEntities().clear();
                messagesToSend.getMessageEntities().addAll(chatMessages);

                objectsResponse[0] = otherPlayers;
                objectsResponse[1] = messagesToSend;
                objectsResponse[2] = incomingAudioSignals;

                oos.writeObject(objectsResponse);
                oos.reset();
                oos.flush();

                setIncomingAudioSignals(0);
            }
        } catch (IOException e) {
            System.out.println("Client " + clientId + " disconnected");
            LocalTime time = LocalTime.now().withNano(0);
            chatMessages.add(new MessageEntity("SERVER", "[" + time + "]: Player" + clientId + " disconnected", new float[]{1, 0, 0}));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            clientThreads.remove(this);
            clientSocket.dispose();
        }
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    public int getIncomingAudioSignals() {
        return incomingAudioSignals;
    }

    public void setIncomingAudioSignals(int incomingAudioSignals) {
        this.incomingAudioSignals = incomingAudioSignals;
    }
}
