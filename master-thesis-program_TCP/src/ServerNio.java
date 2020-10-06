import entity.LaserEntity;
import entity.MessageEntity;
import entity.PlayerEntity;
import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.LocalTime;
import java.util.*;

@SuppressWarnings("SameParameterValue")
public class ServerNio {
    private static int clientId = 1;
    private Map<SocketAddress, PlayerEntity> addressToPlayer = new HashMap<>();
    private static Map<SocketAddress, Integer> playerAddressToIncomingAudioSignals = new HashMap<>();
    private static List<MessageEntity> chatMessages = Collections.synchronizedList(new ArrayList<>());

    private static final float BYTE_BUFFER_LASER = -1.0f;
    private static final float BYTE_BUFFER_OTHER_PLAYERS = -2.0f;
    private static final float BYTE_BUFFER_SOUND_EFFECTS = -3.0f;

    private static final byte BYTE_BUFFER_MESSAGE_SENDER = -4;
    private static final byte BYTE_BUFFER_MESSAGE_CONTENT = -5;
    private static final byte BYTE_BUFFER_MESSAGE_COLOR = -6;
    private static final byte BYTE_BUFFER_MESSAGE_END = -7;


    public static void main(String... args) throws IOException {
        new ServerNio().serve(43);
    }

    private void serve(int port) {
        ServerSocketChannel serverChannel;
        Selector selector;
        try {
            serverChannel = ServerSocketChannel.open();
            ServerSocket ss = serverChannel.socket();

            InetSocketAddress address = new InetSocketAddress(port);
            ss.bind(address);
            serverChannel.configureBlocking(false);

            selector = Selector.open();
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Server is ready for connections at address: " + address);
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
        while (true) {
            try {
                selector.select();
            } catch (IOException ex) {
                ex.printStackTrace();
                break;
            }
            Set<SelectionKey> readyKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = readyKeys.iterator();
            List<LaserEntity> lasers = new ArrayList<>();
            List<Byte> contentBytesList = new ArrayList<>();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                try {
                    if (key.isAcceptable()) {
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        SocketChannel client = server.accept();

                        addressToPlayer.put(client.getRemoteAddress(), null);
                        playerAddressToIncomingAudioSignals.put(client.getRemoteAddress(), 0);

                        System.out.println("Accepted connection from " + client);

                        client.configureBlocking(false);
                        SelectionKey key2 = client.register(selector, SelectionKey.OP_READ);

                        ByteBuffer buffer = ByteBuffer.allocateDirect(Float.BYTES * 2000);
                        client.read(buffer);

                        buffer.flip();
                        key2.attach(buffer);
                    }
                    if (key.isWritable()) {
                        SocketChannel client = (SocketChannel) key.channel();
                        ByteBuffer buffer = (ByteBuffer) key.attachment();

                        //make buffer ready for writing
                        buffer.clear();

                        FloatBuffer floatBuffer = buffer.asFloatBuffer();
                        for (SocketAddress playerAddress : addressToPlayer.keySet()) {
                            if (playerAddress.equals(client.getRemoteAddress())) {
                                continue;
                            }

                            floatBuffer.put(BYTE_BUFFER_OTHER_PLAYERS);
                            PlayerEntity otherPlayer = addressToPlayer.get(playerAddress);
                            if (otherPlayer != null) {
                                floatBuffer.put(otherPlayer.getId());
                                floatBuffer.put(otherPlayer.getX());
                                floatBuffer.put(otherPlayer.getY());
                                floatBuffer.put(otherPlayer.getRotation());
                                floatBuffer.put(otherPlayer.getMotionAngle());
                                floatBuffer.put(otherPlayer.getShieldPower());

                                float thrustersVisible = otherPlayer.isThrustersVisible() ? 1.0f : 0.0f;
                                floatBuffer.put(thrustersVisible);

                                for (LaserEntity laserEntity : otherPlayer.getLasers()) {
                                    floatBuffer.put(BYTE_BUFFER_LASER);
                                    floatBuffer.put(laserEntity.getX());
                                    floatBuffer.put(laserEntity.getY());
                                    floatBuffer.put(laserEntity.getRotation());
                                    floatBuffer.put(laserEntity.getMotionAngle());
                                }
                            }
                        }

                        int newSounds = playerAddressToIncomingAudioSignals.get(client.getRemoteAddress());
                        if (newSounds > 0) {
                            floatBuffer.put(BYTE_BUFFER_SOUND_EFFECTS);
                            floatBuffer.put(newSounds);
                            playerAddressToIncomingAudioSignals.put(client.getRemoteAddress(), 0);
                        }

                        buffer.position(floatBuffer.position() * Float.BYTES);

                        for (MessageEntity message : chatMessages) {
                            buffer.put(BYTE_BUFFER_MESSAGE_SENDER);
                            buffer.put(message.getPlayerName().getBytes());

                            buffer.put(BYTE_BUFFER_MESSAGE_CONTENT);
                            buffer.put(message.getContent().getBytes());

                            buffer.put(BYTE_BUFFER_MESSAGE_COLOR);
                            float[] messageColorFloat = message.getRgb();
                            byte[] messageColor = new byte[]{(byte) messageColorFloat[0], (byte) messageColorFloat[1], (byte) messageColorFloat[2]};
                            buffer.put(messageColor);
                        }

                        buffer.flip();

                        //read from buffer to channel
                        client.write(buffer);
                        //make buffer ready for writing
                        buffer.clear();
                        key.interestOps(SelectionKey.OP_READ);
                    } else if (key.isReadable()) {
                        SocketChannel client = (SocketChannel) key.channel();
                        ByteBuffer buffer = (ByteBuffer) key.attachment();

                        //write into buffer
                        int bytesRead = client.read(buffer);
                        if (bytesRead > 0) {
                            //make buffer ready for reading
                            buffer.flip();

                            PlayerEntity player = addressToPlayer.get(client.getRemoteAddress());
                            if (player == null) {
                                LocalTime time = LocalTime.now().withNano(0);
                                chatMessages.add(new MessageEntity("SERVER", "[" + time + "]: Player" + clientId + " connected", new float[]{0, 1, 0}));

                                player = new PlayerEntity();
                                player.setId(clientId++);
                            }
                            player.getLasers().clear();

                            if (buffer.hasRemaining() && buffer.remaining() >= 7 * Float.BYTES) {
                                player.setX(buffer.getFloat());
                                player.setY(buffer.getFloat());
                                player.setRotation(buffer.getFloat());
                                player.setMotionAngle(buffer.getFloat());
                                player.setShieldPower((int) buffer.getFloat());

                                boolean thrustersVisible = buffer.getFloat() == 1;
                                player.setThrustersVisible(thrustersVisible);

                                player.setNewAudioSignals((int) buffer.getFloat());

                                if (player.getNewAudioSignals() > 0) {
                                    for (SocketAddress playerAddress : playerAddressToIncomingAudioSignals.keySet()) {
                                        if (!playerAddress.equals(client.getRemoteAddress())) {
                                            int otherPlayerSignals = playerAddressToIncomingAudioSignals.get(playerAddress);
                                            playerAddressToIncomingAudioSignals.put(playerAddress, otherPlayerSignals + player.getNewAudioSignals());
                                        }
                                    }
                                    player.setNewAudioSignals(0);
                                }

                                lasers.clear();
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
                                    if (chatMessages.size() >= 100) {
                                        chatMessages = chatMessages.subList(60, chatMessages.size());
                                    }
                                    while (nextValue == BYTE_BUFFER_MESSAGE_CONTENT && buffer.hasRemaining()) {
                                        contentBytesList.clear();
                                        byte nextByte;
                                        while ((nextByte = buffer.get()) != BYTE_BUFFER_MESSAGE_END) {
                                            contentBytesList.add(nextByte);
                                        }

                                        byte[] contentBytes = ArrayUtils.toPrimitive(contentBytesList.toArray(new Byte[0]));
                                        LocalTime time = LocalTime.now().withNano(0);
                                        MessageEntity msg = new MessageEntity("Player " + player.getId(), "[" + time + "]: " + new String(contentBytes), new float[]{1, 1, 1});
                                        chatMessages.add(msg);

                                        if (buffer.hasRemaining()) {
                                            nextValue = buffer.getFloat();
                                        }
                                    }
                                    if (nextValue != BYTE_BUFFER_LASER && nextValue != BYTE_BUFFER_MESSAGE_CONTENT) {
                                        buffer.position(buffer.position() - Float.BYTES);
                                    }
                                }

                                player.getLasers().addAll(lasers);
                                addressToPlayer.put(client.getRemoteAddress(), player);
                            }

                            //make buffer ready for writing
                            buffer.clear();
                        }
                        key.interestOps(SelectionKey.OP_WRITE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    SocketChannel client = (SocketChannel) key.channel();
                    try {
                        PlayerEntity player = addressToPlayer.remove(client.getRemoteAddress());
                        playerAddressToIncomingAudioSignals.remove(client.getRemoteAddress());
                        LocalTime time = LocalTime.now().withNano(0);
                        chatMessages.add(new MessageEntity("SERVER", "[" + time + "]: Player" + player.getId() + " disconnected", new float[]{1, 0, 0}));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    key.cancel();
                    try {
                        key.channel().close();
                    } catch (IOException cex) {
                    }
                }
            }
        }
    }
}
