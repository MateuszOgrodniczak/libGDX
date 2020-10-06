package server;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.DefaultSerializers;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import entity.LaserEntity;
import entity.OtherPlayersEntity;
import entity.PlayerEntity;

import java.util.*;

public class KryoServer {
    private static Map<Integer, PlayerEntity> players = new HashMap<>();
    private OtherPlayersEntity otherPlayersEntity = new OtherPlayersEntity();

    public static void main(String... args) {
        new KryoServer().serve(43);
    }

    public void serve(int port) {
        Server server = new Server();

        try {
            server.start();
            server.bind(42, port);
            Kryo kryo = server.getKryo();
            kryo.register(PlayerEntity.class);
            kryo.register(LaserEntity.class);
            kryo.register(OtherPlayersEntity.class);
            kryo.register(List.class);
            kryo.register(ArrayList.class);
            kryo.register(Collections.EMPTY_LIST.getClass(), new
                    DefaultSerializers.CollectionsEmptyListSerializer());

            server.addListener(new Listener() {
                @Override
                public void connected(Connection connection) {
                    System.out.println("Accepted new connection from: " + connection.getRemoteAddressUDP());
                    players.put(connection.getID(), null);
                }

                @Override
                public void received(Connection connection, Object object) {
                    if (object instanceof PlayerEntity) {
                        PlayerEntity player = (PlayerEntity) object;
                        int id = player.getId();
                        if (id != 0) {
                            players.put(player.getId(), player);
                        }

                        otherPlayersEntity.getPlayers().clear();
                        for (Integer clientId : players.keySet()) {
                            PlayerEntity otherPlayer = players.get(clientId);
                            if (otherPlayer != null && player != otherPlayer) {
                                otherPlayersEntity.getPlayers().add(otherPlayer);
                            }
                        }

                        connection.sendUDP(otherPlayersEntity);
                    }
                }

                @Override
                public void disconnected(Connection connection) {
                    System.out.println("Client " + connection.getRemoteAddressUDP() + " disconnected");
                    players.remove(connection.getID());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            server.close();
        }
    }
}
