package server;

import entity.OtherPlayersEntity;
import entity.PlayerEntity;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

public class ServerUDP {
    private static int playerId = 1;
    private static Map<SocketAddress, Integer> addressToClientId = new HashMap<>();
    private static Map<Integer, PlayerEntity> players = new HashMap<>();

    public static void main(String... args) {
        new ServerUDP().serve(43);
    }

    public void serve(int port) {
        byte[] dataReceived = new byte[65535];
        DatagramPacket packet;

        try (DatagramSocket ds = new DatagramSocket(port)) {

            while (true) {
                packet = new DatagramPacket(dataReceived, dataReceived.length);
                ds.receive(packet);

                ByteArrayInputStream inputStream = new
                        ByteArrayInputStream(dataReceived);
                ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(inputStream));

                SocketAddress clientAddress = packet.getSocketAddress();
                Integer clientId = addressToClientId.get(clientAddress);

                if (clientId == null) {
                    clientId = playerId++;
                    addressToClientId.put(clientAddress, clientId);
                }

                PlayerEntity player = (PlayerEntity) ois.readObject();
                players.put(clientId, player);

                OtherPlayersEntity otherPlayers = new OtherPlayersEntity();

                for (Integer playerId : players.keySet()) {
                    PlayerEntity otherPlayer = players.get(playerId);
                    if (otherPlayer != null && player != otherPlayer) {
                        otherPlayers.getPlayers().add(otherPlayer);
                    }
                }

                ByteArrayOutputStream outputStream = new
                        ByteArrayOutputStream(5000);

                ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(outputStream));
                oos.writeObject(otherPlayers);
                oos.flush();

                byte[] sendData = outputStream.toByteArray();
                packet = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
                ds.send(packet);

                Thread.sleep(10);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static StringBuilder data(byte[] a) {
        if (a == null)
            return null;
        StringBuilder ret = new StringBuilder();
        int i = 0;
        while (a[i] != 0) {
            ret.append((char) a[i]);
            i++;
        }
        return ret;
    }
}
