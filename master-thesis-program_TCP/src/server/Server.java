package server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.backends.lwjgl.LwjglNet;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.Socket;
import threads.ClientThread;

public class Server {
    private int clientId = 1;

    public static void main(String... args) {
        new Server().serve(43);
    }

    public void serve(int port) {
        Gdx.net = new LwjglNet();
        final ServerSocket serverSocket = Gdx.net.newServerSocket(Net.Protocol.TCP, port, null);

        System.out.println("server.Server running");

        Socket clientSocket = null;
        try {
            while (true) {
                clientSocket = serverSocket.accept(null);
                System.out.println("Connection accepted from: " + clientSocket.getRemoteAddress());

                new ClientThread(clientSocket, clientId++).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (clientSocket != null) {
                clientSocket.dispose();
            }
        }
    }
}
