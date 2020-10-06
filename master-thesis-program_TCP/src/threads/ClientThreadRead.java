package threads;

import com.badlogic.gdx.net.Socket;
import entity.MessageEntity;
import entity.OtherPlayersEntity;
import entity.PlayerEntity;
import entity.RawMessagesEntity;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ClientThreadRead extends Thread {
    boolean reading = true;
    private final Map<Integer, PlayerEntity> playerIdToEntity;
    private final Map<Integer, Integer> playerIdToIncomingAudioSignals;
    static final List<ClientThreadRead> clientThreads = Collections.synchronizedList(new ArrayList<>());
    private final List<MessageEntity> chatMessages;
    private int incomingAudioSignals;

    private Socket clientSocket;
    private ObjectInputStream ois;

    private int clientId;
    private PlayerEntity player;
    private OtherPlayersEntity otherPlayers = new OtherPlayersEntity();

    public ClientThreadRead(Socket clientSocket, int clientId, Map<Integer, PlayerEntity> playerIdToEntity, Map<Integer, Integer> playerIdToIncomingAudioSignals, List<MessageEntity> chatMessages) {
        this.clientSocket = clientSocket;
        this.playerIdToEntity = playerIdToEntity;
        this.playerIdToIncomingAudioSignals = playerIdToIncomingAudioSignals;
        this.chatMessages = chatMessages;

        this.clientId = clientId;
        playerIdToIncomingAudioSignals.put(clientId, 0);

        clientThreads.add(this);

        LocalTime time = LocalTime.now().withNano(0);
        chatMessages.add(new MessageEntity("SERVER", "[" + time + "]: Player" + clientId + " connected", new float[]{0, 1, 0}));

        this.otherPlayers.setPlayers(Collections.synchronizedList(new ArrayList<>()));
    }

    public List<MessageEntity> getChatMessages() {
        return chatMessages;
    }

    public OtherPlayersEntity getOtherPlayers() {
        return otherPlayers;
    }

    public void setOtherPlayers(OtherPlayersEntity otherPlayers) {
        this.otherPlayers = otherPlayers;
    }

    @Override
    public void run() {
        try {
            while (true) {
                ois = new ObjectInputStream(clientSocket.getInputStream());

                player = (PlayerEntity) ois.readObject();
                player.setId(clientId);

                playerIdToEntity.put(clientId, player);

                if (player.getNewAudioSignals() > 0) {
                    for (Integer playerId : playerIdToEntity.keySet()) {
                        if (playerId != clientId) {
                            int signals = playerIdToIncomingAudioSignals.get(playerId);
                            playerIdToIncomingAudioSignals.put(playerId, signals + player.getNewAudioSignals());
                        }
                    }
                }

                synchronized (chatMessages) {
                    if (chatMessages.size() >= 100) {
                        List<MessageEntity> sublist = new ArrayList<>(chatMessages.subList(60, chatMessages.size()));
                        chatMessages.clear();
                        chatMessages.addAll(sublist);
                    }

                    RawMessagesEntity newMessages = (RawMessagesEntity) ois.readObject();

                    for (String newMessage : newMessages.getRawMessages()) {
                        LocalTime time = LocalTime.now().withNano(0);
                        chatMessages.add(new MessageEntity("Player " + player.getId(), "[" + time + "]: " + newMessage, new float[]{1, 1, 1}));
                    }
                }
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
