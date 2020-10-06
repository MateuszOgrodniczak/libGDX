import threads.ClientThreadIO;

import java.io.IOException;
import java.net.InetSocketAddress;

public class ServerIO {
    private int clientId = 1;

    public static void main(String... args) throws IOException {
        new ServerIO().serve(43);
    }

    public void serve(int port) throws IOException {
        final java.net.ServerSocket serverSocket = new java.net.ServerSocket();

        InetSocketAddress address = new InetSocketAddress(port);
        serverSocket.bind(address);

        System.out.println("server.Server running");
        try {
            while (true) {
                final java.net.Socket clientSocket = serverSocket.accept();
                System.out.println("Connection accepted from: " + clientSocket.getRemoteSocketAddress());

                new ClientThreadIO(clientSocket, clientId++).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            serverSocket.close();
        }
    }
}
