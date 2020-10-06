package server;

import entity.LaserEntity;
import entity.PlayerEntity;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.*;

public class ServerNioUDP {
    private static final int BUFFER_SIZE = Float.BYTES * 1000;
    private static int clientId = 1;
    private static final float BYTE_BUFFER_LASER = -1.0f;
    private Map<SocketAddress, PlayerEntity> addressToPlayer = new HashMap<>();

    public static void main(String... args) {
        new ServerNioUDP().serve(43);
    }

    public static class BufferWrapper {
        ByteBuffer request;
        ByteBuffer response;
        SocketAddress address;

        BufferWrapper() {
            request = ByteBuffer.allocate(500);
            response = ByteBuffer.allocate(BUFFER_SIZE);
        }
    }

    private void serve(int port) {
        DatagramChannel serverChannel;
        try {
            Selector selector = Selector.open();
            serverChannel = DatagramChannel.open();

            InetSocketAddress address = new InetSocketAddress(port);
            serverChannel.socket().bind(address);
            serverChannel.configureBlocking(false);

            System.out.println("server.Server started: " + address.getAddress());

            SelectionKey clientKey = serverChannel.register(selector, SelectionKey.OP_READ);
            clientKey.attach(new BufferWrapper());

            while (true) {
                try {
                    selector.select();
                    Iterator selectedKeys = selector.selectedKeys().iterator();
                    while (selectedKeys.hasNext()) {
                        SelectionKey key = (SelectionKey) selectedKeys.next();
                        selectedKeys.remove();

                        if (!key.isValid()) {
                            continue;
                        }

                        if (key.isReadable()) {
                            read(key);
                            key.interestOps(SelectionKey.OP_WRITE);
                        } else if (key.isWritable()) {
                            write(key);
                            key.interestOps(SelectionKey.OP_READ);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void read(SelectionKey key) throws IOException {
        DatagramChannel channel = (DatagramChannel) key.channel();
        BufferWrapper connection = (BufferWrapper) key.attachment();

        connection.address = channel.receive(connection.request);
        PlayerEntity player = addressToPlayer.get(connection.address);
        if (player == null) {
            player = new PlayerEntity();
            player.setId(clientId++);

            System.out.println("Accepted new packets from: " + connection.address);
        }

        ByteBuffer buffer = connection.request;
        buffer.flip();

        while (buffer.hasRemaining()) {
            player.setX(buffer.getFloat());
            player.setY(buffer.getFloat());
            player.setRotation(buffer.getFloat());
            player.setMotionAngle(buffer.getFloat());
            player.setShieldPower((int) buffer.getFloat());
            boolean thrustersVisible = buffer.getFloat() == 1;
            player.setThrustersVisible(thrustersVisible);

            List<LaserEntity> lasers = new ArrayList<>();
            if (buffer.hasRemaining()) {
                float nextValue = buffer.getFloat();
                while (nextValue == BYTE_BUFFER_LASER && buffer.hasRemaining()) {
                    lasers.add(new LaserEntity(
                            buffer.getFloat(),
                            buffer.getFloat(),
                            buffer.getFloat(),
                            buffer.getFloat()
                    ));
                    if (buffer.hasRemaining()) {
                        nextValue = buffer.getFloat();
                    }
                }
                if (nextValue != BYTE_BUFFER_LASER) {
                    buffer.position(buffer.position() - Float.BYTES);
                }
            }
            player.setLasers(lasers);
            addressToPlayer.put(connection.address, player);
        }

        buffer.clear();
    }

    private void write(SelectionKey key) throws IOException {
        DatagramChannel channel = (DatagramChannel) key.channel();
        BufferWrapper connection = (BufferWrapper) key.attachment();

        ByteBuffer buffer = connection.response;
        buffer.clear();

        if (buffer.hasRemaining()) {
            FloatBuffer floatBuffer = buffer.asFloatBuffer();
            for (SocketAddress playerAddress : addressToPlayer.keySet()) {
                if (playerAddress.equals(connection.address)) {
                    continue;
                }

                PlayerEntity otherPlayer = addressToPlayer.get(playerAddress);
                if (otherPlayer != null) {
                    floatBuffer.put(otherPlayer.getId());
                    floatBuffer.put(otherPlayer.getX());
                    floatBuffer.put(otherPlayer.getY());
                    floatBuffer.put(otherPlayer.getRotation());
                    floatBuffer.put(otherPlayer.getMotionAngle());
                    floatBuffer.put(otherPlayer.getShieldPower());
                    floatBuffer.put(otherPlayer.isThrustersVisible() ? 1 : 0);

                    for (LaserEntity laserEntity : otherPlayer.getLasers()) {
                        floatBuffer.put(BYTE_BUFFER_LASER);
                        floatBuffer.put(laserEntity.getX());
                        floatBuffer.put(laserEntity.getY());
                        floatBuffer.put(laserEntity.getRotation());
                        floatBuffer.put(laserEntity.getMotionAngle());
                    }
                }
            }

            buffer.position(floatBuffer.position() * Float.BYTES);
            buffer.flip();
            channel.send(connection.response, connection.address);
        }
    }
}
