package threads;

import com.badlogic.gdx.net.Socket;
import entity.ChatMessagesEntity;
import entity.MessageEntity;
import entity.OtherPlayersEntity;
import entity.PlayerEntity;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClientThreadWrite extends Thread {
    private final Map<Integer, PlayerEntity> playerIdToEntity;
    private final Map<Integer, Integer> playerIdToIncomingAudioSignals;
    private final List<MessageEntity> chatMessages;
    private ClientThreadRead clientThreadRead;
    static List<ClientThreadWrite> clientThreads = new ArrayList<>();
    private int incomingAudioSignals;

    private Socket clientSocket;
    private ObjectOutputStream oos;

    private int clientId;
    private PlayerEntity player;
    private OtherPlayersEntity otherPlayers = new OtherPlayersEntity();

    public ClientThreadWrite(Socket clientSocket, ClientThreadRead clientThreadRead, int clientId,
                             Map<Integer, PlayerEntity> playerIdToEntity, Map<Integer, Integer> playerIdToIncomingAudioSignals, List<MessageEntity> chatMessages) throws Exception {
        this.clientSocket = clientSocket;
        this.clientThreadRead = clientThreadRead;
        this.playerIdToEntity = playerIdToEntity;
        this.playerIdToIncomingAudioSignals = playerIdToIncomingAudioSignals;
        this.chatMessages = chatMessages;

        this.clientId = clientId;
        clientThreads.add(this);
        oos = new ObjectOutputStream(clientSocket.getOutputStream());
    }

    @Override
    public void run() {
        try {
            while (true) {

                otherPlayers.getPlayers().clear();
                synchronized (playerIdToEntity) {
                    for (Integer playerId : playerIdToEntity.keySet()) {
                        if (playerId != clientId) {
                            otherPlayers.getPlayers().add(playerIdToEntity.get(playerId));
                        }
                    }
                }

                Integer incomingAudioSignals = playerIdToIncomingAudioSignals.get(clientId);

                ChatMessagesEntity messagesToSend;
                synchronized (chatMessages) {
                    messagesToSend = new ChatMessagesEntity();
                    messagesToSend.getMessageEntities().addAll(chatMessages);
                }

                oos.writeObject(otherPlayers);
                oos.writeObject(messagesToSend);
                oos.writeObject(incomingAudioSignals);
                oos.flush();

                setIncomingAudioSignals(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Client " + clientId + " disconnected");
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
