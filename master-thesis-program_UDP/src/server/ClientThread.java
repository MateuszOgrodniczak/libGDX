package server;

import com.badlogic.gdx.net.Socket;
import entity.OtherPlayersEntity;
import entity.PlayerEntity;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ClientThread extends Thread {
    static List<ClientThread> clientThreads = new ArrayList<>();

    private Socket clientSocket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    private int clientId;
    private PlayerEntity player;

    private OtherPlayersEntity otherPlayers;

    public ClientThread(Socket clientSocket, int clientId) {
        this.clientSocket = clientSocket;

        this.clientId = clientId;
        clientThreads.add(this);

        otherPlayers = new OtherPlayersEntity();
    }

    @Override
    public void run() {
        try {
            while (true) {
                oos = new ObjectOutputStream(clientSocket.getOutputStream());
                ois = new ObjectInputStream(clientSocket.getInputStream());

                player = (PlayerEntity) ois.readObject();
                player.setId(clientId);

                otherPlayers.getPlayers().clear();
                for (ClientThread client : clientThreads) {
                    PlayerEntity otherPlayer = client.getPlayer();
                    if (otherPlayer != null && otherPlayer != player) {
                        otherPlayers.getPlayers().add(otherPlayer);
                    }
                }

                oos.writeObject(otherPlayers);
                oos.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Client disconnected");
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
}
