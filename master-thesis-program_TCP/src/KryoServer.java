import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.DefaultSerializers;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import entity.*;

import java.io.IOException;
import java.time.LocalTime;
import java.util.*;

public class KryoServer {
    private static Map<Integer, PlayerEntity> players = new HashMap<>();
    private static Map<Integer, Integer> playerIdToIncomingAudioSignals = new HashMap<>();
    private static List<MessageEntity> chatMessages = new ArrayList<>();

    public static void main(String... args) throws IOException {
        new KryoServer().serve(43);
    }

    public void serve(int port) {
        Server server = new Server();

        try {
            server.start();
            server.bind(port);

            Kryo kryo = server.getKryo();
            kryo.register(float[].class);
            kryo.register(PlayerEntity.class);
            kryo.register(LaserEntity.class);
            kryo.register(OtherPlayersEntity.class);
            kryo.register(MessageEntity.class);
            kryo.register(ChatMessagesEntity.class);
            kryo.register(RawMessagesEntity.class);
            kryo.register(List.class);
            kryo.register(ArrayList.class);
            kryo.register(Collections.EMPTY_LIST.getClass(), new
                    DefaultSerializers.CollectionsEmptyListSerializer());
            kryo.register(Boolean.class);
            kryo.register(Integer.class);

            server.addListener(new Listener() {
                @Override
                public void connected(Connection connection) {
                    System.out.println("Accepted new connection from: " + connection.getRemoteAddressTCP());
                    players.put(connection.getID(), null);
                    playerIdToIncomingAudioSignals.put(connection.getID(), 0);
                    LocalTime time = LocalTime.now().withNano(0);
                    chatMessages.add(new MessageEntity("SERVER", "[" + time + "]: Player" + connection.getID() + " connected", new float[]{0, 1, 0}));
                }

                @Override
                public void disconnected(Connection connection) {
                    LocalTime time = LocalTime.now().withNano(0);
                    chatMessages.add(new MessageEntity("SERVER", "[" + time + "]: Player" + connection.getID() + " disconnected", new float[]{1, 0, 0}));
                    players.remove(connection.getID());
                    playerIdToIncomingAudioSignals.remove(connection.getID());
                }

                @Override
                public void received(Connection connection, Object object) {
                    if (object instanceof PlayerEntity) {
                        PlayerEntity player = (PlayerEntity) object;
                        int playerId = player.getId();
                        if (playerId != 0) {
                            players.put(player.getId(), player);
                        }

                        OtherPlayersEntity otherPlayersEntity = new OtherPlayersEntity();
                        for (Integer clientId : players.keySet()) {
                            PlayerEntity otherPlayer = players.get(clientId);
                            if (otherPlayer != null && player != otherPlayer) {
                                otherPlayersEntity.getPlayers().add(otherPlayer);
                                int newSounds = playerIdToIncomingAudioSignals.get(otherPlayer.getId());
                                playerIdToIncomingAudioSignals.put(otherPlayer.getId(), newSounds + player.getNewAudioSignals());
                            }
                        }

                        connection.sendTCP(otherPlayersEntity);

                        int newSignals = playerIdToIncomingAudioSignals.get(playerId);
                        if (newSignals > 0) {
                            connection.sendTCP(newSignals);
                            playerIdToIncomingAudioSignals.put(playerId, 0);
                        }
                    } else if (object instanceof RawMessagesEntity) {
                        if (chatMessages.size() >= 100) {
                            chatMessages = chatMessages.subList(60, chatMessages.size());
                        }

                        RawMessagesEntity newMessages = (RawMessagesEntity) object;

                        for (String newMessage : newMessages.getRawMessages()) {
                            LocalTime time = LocalTime.now().withNano(0);
                            chatMessages.add(new MessageEntity("Player " + connection.getID(), "[" + time + "]: " + newMessage, new float[]{1, 1, 1}));
                        }
                    }

                    if (!chatMessages.isEmpty()) {
                        ChatMessagesEntity messagesToSend = new ChatMessagesEntity();
                        messagesToSend.getMessageEntities().addAll(chatMessages);

                        connection.sendTCP(messagesToSend);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            server.close();
        }
    }
}
