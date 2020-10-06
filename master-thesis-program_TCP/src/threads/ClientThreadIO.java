package threads;

import entity.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientThreadIO extends Thread {
    static List<ClientThreadIO> clientThreads = new ArrayList<>();
    private static List<MessageEntity> chatMessages = Collections.synchronizedList(new ArrayList<>());
    private int incomingAudioSignals;

    private Socket clientSocket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    private int clientId;
    private PlayerEntity player;
    private OtherPlayersEntity otherPlayers = new OtherPlayersEntity();
    private ChatMessagesEntity messagesToSend = new ChatMessagesEntity();

    public ClientThreadIO(Socket clientSocket, int clientId) throws Exception {
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
        try {
            Object[] objectResponse = new Object[3];
            while (true) {
                Object[] objects = (Object[]) ois.readObject();
                player = (PlayerEntity) objects[0];
                player.setId(clientId);

                otherPlayers.getPlayers().clear();
                for (ClientThreadIO client : clientThreads) {
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
                        chatMessages = chatMessages.subList(60, chatMessages.size() - 1);
                    }
                }

                RawMessagesEntity newMessages = (RawMessagesEntity) objects[1];//ois.readObject();

                for (String newMessage : newMessages.getRawMessages()) {
                    LocalTime time = LocalTime.now().withNano(0);
                    chatMessages.add(new MessageEntity("Player " + player.getId(), "[" + time + "]: " + newMessage, new float[]{1, 1, 1}));
                }

                messagesToSend.getMessageEntities().clear();
                messagesToSend.getMessageEntities().addAll(chatMessages);

                objectResponse[0] = otherPlayers;
                objectResponse[1] = messagesToSend;
                objectResponse[2] = incomingAudioSignals;

                oos.writeObject(objectResponse);
                oos.reset();
                oos.flush();

                setIncomingAudioSignals(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Client " + clientId + " disconnected");
            LocalTime time = LocalTime.now().withNano(0);
            chatMessages.add(new MessageEntity("SERVER", "[" + time + "]: Player" + clientId + " disconnected", new float[]{1, 0, 0}));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            clientThreads.remove(this);
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
