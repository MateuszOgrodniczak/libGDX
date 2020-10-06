package server;

import entity.OtherPlayersEntity;
import entity.PlayerEntity;

import java.io.*;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

public class ServerUDP {
    private static final int BUFFER_SIZE = Float.BYTES * 1000;
    private static final int BUFFER_WRITE_SIZE = Float.BYTES * 100;
    private static int playerId = 1;
    private static Map<SocketAddress, Integer> addressToClientId = new HashMap<>();
    private static Map<Integer, PlayerEntity> players = new HashMap<>();
    private OtherPlayersEntity otherPlayers = new OtherPlayersEntity();

    public static void main(String... args) {
        new ServerUDP().serve();
    }

    private void serve() {
        byte[] dataReceived = new byte[BUFFER_WRITE_SIZE];
        java.net.DatagramPacket packet;

        try (DatagramSocket ds = new DatagramSocket(43)) {
            while (true) {
                packet = new java.net.DatagramPacket(dataReceived, dataReceived.length);
                ds.receive(packet);

                ByteArrayInputStream inputStream = new
                        ByteArrayInputStream(dataReceived);
                ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(inputStream));

                SocketAddress clientAddress = packet.getSocketAddress();
                Integer clientId = addressToClientId.get(clientAddress);

                PlayerEntity player = (PlayerEntity) ois.readObject();

                if (clientId == null) {
                    clientId = playerId++;
                    addressToClientId.put(clientAddress, clientId);
                }

                player.setId(clientId);
                players.put(clientId, player);

                otherPlayers.getPlayers().clear();

                for (Integer playerId : players.keySet()) {
                    if (player.getId() != playerId) {
                        otherPlayers.getPlayers().add(players.get(playerId));
                    }
                }

                ByteArrayOutputStream outputStream = new
                        ByteArrayOutputStream(BUFFER_SIZE);

                ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(outputStream));
                oos.writeObject(otherPlayers);
                oos.flush();

                byte[] sendData = outputStream.toByteArray();
                packet = new java.net.DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
                ds.send(packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
