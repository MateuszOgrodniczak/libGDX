package game;

import actor.Spaceship;
import actor.SpaceshipEnemy;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.DefaultSerializers;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import entity.*;
import global.GlobalConfig;
import handlers.ClientNettyHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.apache.commons.lang3.ArrayUtils;
import screen.LevelScreen;
import test.ResponseTimeTest;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.*;

@SuppressWarnings("Duplicates")
public class SpaceGame extends BaseGame {

    public static final int GAME_WIDTH = 1200;
    public static final int GAME_HEIGHT = 900;
    public static final int CHAT_HEIGHT = 850;
    public static final int CHAT_WIDTH = 250;

    private static final float BYTE_BUFFER_LASER = -1.0f;
    private static final float BYTE_BUFFER_OTHER_PLAYERS = -2.0f;
    private static final float BYTE_BUFFER_SOUND_EFFECTS = -3.0f;

    private static final byte BYTE_BUFFER_MESSAGE_SENDER = -4;
    private static final byte BYTE_BUFFER_MESSAGE_CONTENT = -5;
    private static final byte BYTE_BUFFER_MESSAGE_COLOR = -6;
    private static final byte BYTE_BUFFER_MESSAGE_END = -7;

    public LevelScreen screen;
    private PlayerEntity player;

    private int enemiesCount;
    private long serverResponseTime = 0;
    private double totalResponseTime = 0;
    private long iterationsCount = 0;

    @Override
    public void create() {
        super.create();
        screen = new LevelScreen();
        setScreen(screen);

        // connectNio2();
        // connectIO();
        //connect2();
        //  connectKryo();
        //connectNetty();
        //connectNio();
        connectIO2();
    }

    @Override
    public void update(float delta) {
        getScreen().render(delta);
    }

    public void connectUDP() {
        String host = "172.24.25.33";
        int port = 43;
        byte[] dataReceived = new byte[65535];
        InetAddress serverAddress;

        Spaceship spaceship = screen.addSpaceShip(400, 300);
        player = new PlayerEntity(spaceship.getX(), spaceship.getY(), spaceship.getRotation(), spaceship.getMotionAngle(), spaceship.getShieldPower());

        try {
            serverAddress = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try (DatagramSocket ds = new DatagramSocket()) {
                    while (true) {
                        player.setX(spaceship.getX());
                        player.setY(spaceship.getY());
                        player.setRotation(spaceship.getRotation());
                        player.setMotionAngle(spaceship.getMotionAngle());
                        player.setShieldPower(spaceship.getShieldPower());
                        player.setThrustersVisible(spaceship.areThrustersVisible());
                        player.setLasers(spaceship.getNewLasers());
                        player.setDestroyed(spaceship.isDestroyed());

                        ByteArrayOutputStream outputStream = new
                                ByteArrayOutputStream(5000);

                        ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(outputStream));
                        oos.writeObject(player);
                        oos.flush();

                        byte[] sendData = outputStream.toByteArray();
                        DatagramPacket packet = new DatagramPacket(sendData, sendData.length, serverAddress, port);
                        ds.send(packet);

                        packet = new DatagramPacket(dataReceived, dataReceived.length);
                        ds.receive(packet);

                        ByteArrayInputStream inputStream = new
                                ByteArrayInputStream(dataReceived);
                        ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(inputStream));

                        OtherPlayersEntity otherPlayers = (OtherPlayersEntity) ois.readObject();

                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                for (PlayerEntity otherPlayer : otherPlayers.getPlayers()) {
                                    SpaceshipEnemy otherPlayerSpaceship = screen.getEnemySpaceships().get(otherPlayer.getId());
                                    if (otherPlayerSpaceship == null) {
                                        screen.addEnemySpaceShip(otherPlayer.getX(), otherPlayer.getY(), otherPlayer.getId());
                                    } else if (otherPlayer.isDestroyed()) {
                                        screen.removeDestroyedSpaceship(otherPlayerSpaceship);
                                        if (otherPlayers.getPlayers().size() == 1) {
                                            screen.addWinMessage();
                                        }
                                    } else {
                                        otherPlayerSpaceship.setX(otherPlayer.getX());
                                        otherPlayerSpaceship.setY(otherPlayer.getY());
                                        otherPlayerSpaceship.setRotation(otherPlayer.getRotation());
                                        otherPlayerSpaceship.setMotionAngle(otherPlayer.getMotionAngle());
                                        otherPlayerSpaceship.setShieldPower(otherPlayer.getShieldPower());
                                        otherPlayerSpaceship.setThrustersVisible(otherPlayer.isThrustersVisible());

                                        screen.addLasers(otherPlayer.getLasers());
                                    }
                                }
                            }
                        });

                        Thread.sleep(10);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void connectKryo() {
        String host = "172.24.25.33";
        int port = 43;
        Spaceship spaceship = screen.addSpaceShip(400, 300);
        player = new PlayerEntity(spaceship.getX(), spaceship.getY(), spaceship.getRotation(), spaceship.getMotionAngle(), spaceship.getShieldPower());

        Client client = new Client(9000, 5000);

        try {
            client.start();

            Kryo kryo = client.getKryo();
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

            client.addListener(new Listener() {
                @Override
                public void connected(Connection connection) {
                    player.setId(connection.getID());
                }

                @Override
                public void received(Connection connection, Object object) {
                    if (object instanceof OtherPlayersEntity) {
                        measureTime();

                        OtherPlayersEntity otherPlayers = (OtherPlayersEntity) object;
                        enemiesCount = otherPlayers.getPlayers().size();
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                Set<Integer> enemiesIds = screen.getEnemySpaceships().keySet();
                                List<PlayerEntity> otherPlayersEntities = otherPlayers.getPlayers();
                                Set<Integer> toRemove = new HashSet<>();

                                for (Integer enemyId : enemiesIds) {
                                    if (otherPlayersEntities.stream().noneMatch(p -> p.getId() == enemyId)) {
                                        screen.getEnemySpaceships().get(enemyId).addAction(Actions.removeActor());
                                        toRemove.add(enemyId);
                                    }
                                }
                                screen.getEnemySpaceships().keySet().removeAll(toRemove);

                                for (PlayerEntity otherPlayer : otherPlayersEntities) {
                                    SpaceshipEnemy otherPlayerSpaceship = screen.getEnemySpaceships().get(otherPlayer.getId());
                                    if (otherPlayerSpaceship == null) {
                                        screen.addEnemySpaceShip(otherPlayer.getX(), otherPlayer.getY(), otherPlayer.getId());
                                    } else {
                                        otherPlayerSpaceship.setX(otherPlayer.getX());
                                        otherPlayerSpaceship.setY(otherPlayer.getY());
                                        otherPlayerSpaceship.setRotation(otherPlayer.getRotation());
                                        otherPlayerSpaceship.setMotionAngle(otherPlayer.getMotionAngle());
                                        otherPlayerSpaceship.setShieldPower(otherPlayer.getShieldPower());
                                        otherPlayerSpaceship.setThrustersVisible(otherPlayer.isThrustersVisible());

                                        screen.addLasers(otherPlayer.getLasers());
                                    }
                                }
                            }

                        });
                    } else if (object instanceof Integer) {
                        if (!GlobalConfig.isMuted) {
                            int newSignals = (Integer) object;
                            if (newSignals > 0) {
                                System.out.println("new signals: " + newSignals);
                            }
                            for (int i = 0; i < newSignals; i++) {
                                Sound laserSound = Gdx.audio.newSound(Gdx.files.internal("assets/laser.mp3"));
                                laserSound.play(1.0f);
                            }
                        }
                    } else if (object instanceof ChatMessagesEntity) {

                        ChatMessagesEntity chatMessages = (ChatMessagesEntity) object;
                        List<MessageEntity> chatMessageEntities = chatMessages.getMessageEntities();

                        if (screen.getChatMessages().size() != chatMessageEntities.size()) {
                            screen.getChatMessages().clear();
                            screen.getChatMessages().addAll(chatMessageEntities);
                        }
                    }
                }
            });

            client.connect(5000, host, port);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            player.setX(spaceship.getX());
                            player.setY(spaceship.getY());
                            player.setRotation(spaceship.getRotation());
                            player.setMotionAngle(spaceship.getMotionAngle());
                            player.setShieldPower(spaceship.getShieldPower());
                            player.setThrustersVisible(spaceship.areThrustersVisible());
                            player.setLasers(spaceship.getNewLasers());
                            player.setNewAudioSignals(player.getLasers().size());

                            client.sendTCP(player);

                            List<String> messages = screen.getMessagesToSend();

                            if (!messages.isEmpty()) {
                                RawMessagesEntity chatMessagesEntity = new RawMessagesEntity();
                                chatMessagesEntity.getRawMessages().addAll(messages);

                                client.sendTCP(chatMessagesEntity);

                                messages.clear();
                            }

                            Thread.sleep(10);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        client.close();
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
            client.close();
        }
    }

    public void connectNio() {
        String host = "172.24.25.33";
        int port = 43;

        Spaceship spaceship = screen.addSpaceShip(400, 300);
        player = new PlayerEntity(spaceship.getX(), spaceship.getY(), spaceship.getRotation(), spaceship.getMotionAngle(), spaceship.getShieldPower());

        try {
            SocketAddress address = new InetSocketAddress(host, port);
            java.nio.channels.SocketChannel client = java.nio.channels.SocketChannel.open(address);

            client.configureBlocking(false);
            ByteBuffer buffer = ByteBuffer.allocateDirect(Float.BYTES * 2000);

            new Thread(() -> {
                try {
                    List<Byte> senderBytesList = new ArrayList<>();
                    List<Byte> contentBytesList = new ArrayList<>();
                    List<LaserEntity> lasers = new ArrayList<>();
                    while (true) {
                        //write from channel to buffer
                        int bytesRead = client.read(buffer);
                        List<MessageEntity> chatMessages = new ArrayList<>();
                        if (bytesRead > 0) {
                            //MEASUREMENTS
                            measureTime();

                            //make buffer ready for reading (lim=pos, pos=0)
                            buffer.flip();
                            Map<Integer, PlayerEntity> otherPlayers = new HashMap<>();
                            float nextValue;

                            if (buffer.hasRemaining()) {
                                nextValue = buffer.getFloat();
                                while (buffer.hasRemaining() && nextValue == BYTE_BUFFER_OTHER_PLAYERS) {
                                    int id = (int) buffer.getFloat();
                                    PlayerEntity otherPlayer = otherPlayers.get(id);
                                    if (otherPlayer == null) {
                                        otherPlayer = new PlayerEntity();
                                        otherPlayer.setId(id);
                                    }
                                    otherPlayer.setX(buffer.getFloat());
                                    otherPlayer.setY(buffer.getFloat());
                                    otherPlayer.setRotation(buffer.getFloat());
                                    otherPlayer.setMotionAngle(buffer.getFloat());
                                    otherPlayer.setShieldPower((int) buffer.getFloat());
                                    boolean thrustersVisible = buffer.getFloat() == 1;
                                    otherPlayer.setThrustersVisible(thrustersVisible);

                                    lasers.clear();
                                    if (buffer.hasRemaining()) {
                                        nextValue = buffer.getFloat();
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

                                    otherPlayer.getLasers().addAll(lasers);
                                    otherPlayers.put(id, otherPlayer);
                                    if (buffer.hasRemaining()) {
                                        nextValue = buffer.getFloat();
                                    }
                                }
                                if (nextValue != BYTE_BUFFER_OTHER_PLAYERS) {
                                    buffer.position(buffer.position() - Float.BYTES);
                                }
                            }

                            enemiesCount = otherPlayers.size();

                            if (buffer.hasRemaining()) {
                                nextValue = (int) buffer.getFloat();
                                if (nextValue == BYTE_BUFFER_SOUND_EFFECTS) {
                                    int newAudioSignals = (int) buffer.getFloat();
                                    if (!GlobalConfig.isMuted) {
                                        for (int i = 0; i < newAudioSignals; i++) {
                                            Sound laserSound = Gdx.audio.newSound(Gdx.files.internal("assets/laser.mp3"));
                                            laserSound.play(1.0f);
                                        }
                                    }
                                } else {
                                    buffer.position(buffer.position() - Float.BYTES);
                                }
                            }

                            if (buffer.hasRemaining()) {
                                nextValue = buffer.get();
                                while (nextValue == BYTE_BUFFER_MESSAGE_SENDER && buffer.hasRemaining()) {
                                    senderBytesList.clear();
                                    contentBytesList.clear();
                                    byte nextByte;
                                    while ((nextByte = buffer.get()) != BYTE_BUFFER_MESSAGE_CONTENT && buffer.hasRemaining()) {
                                        senderBytesList.add(nextByte);
                                    }
                                    while (nextByte != BYTE_BUFFER_MESSAGE_COLOR && buffer.hasRemaining()) {
                                        contentBytesList.add(nextByte);
                                        nextByte = buffer.get();
                                    }
                                    if (!buffer.hasRemaining()) {
                                        break;
                                    }
                                    byte r = buffer.get();
                                    byte g = buffer.get();
                                    byte b = buffer.get();

                                    byte[] senderBytes = ArrayUtils.toPrimitive(senderBytesList.toArray(new Byte[0]));
                                    byte[] contentBytes = ArrayUtils.toPrimitive(contentBytesList.toArray(new Byte[0]));

                                    MessageEntity msg = new MessageEntity(new String(senderBytes), new String(contentBytes), new float[]{r, g, b});
                                    chatMessages.add(msg);

                                    if (buffer.hasRemaining()) {
                                        nextValue = buffer.get();
                                    }
                                }
                                if (nextValue != BYTE_BUFFER_MESSAGE_SENDER) {
                                    buffer.position(buffer.position() - Byte.BYTES);
                                }

                                if (screen.getChatMessages().size() != chatMessages.size()) {
                                    screen.getChatMessages().clear();
                                    screen.getChatMessages().addAll(chatMessages);
                                }
                            }

                            // }
                            Gdx.app.postRunnable(() -> {
                                Set<Integer> enemiesIds = screen.getEnemySpaceships().keySet();
                                List<PlayerEntity> otherPlayersEntities = new ArrayList<>(otherPlayers.values());
                                Set<Integer> toRemove = new HashSet<>();

                                for (Integer enemyId : enemiesIds) {
                                    if (otherPlayersEntities.stream().noneMatch(p -> p.getId() == enemyId)) {
                                        screen.getEnemySpaceships().get(enemyId).addAction(Actions.removeActor());
                                        toRemove.add(enemyId);
                                    }
                                }
                                screen.getEnemySpaceships().keySet().removeAll(toRemove);

                                for (Integer playerId : otherPlayers.keySet()) {
                                    PlayerEntity otherPlayer = otherPlayers.get(playerId);
                                    SpaceshipEnemy otherPlayerSpaceship = screen.getEnemySpaceships().get(otherPlayer.getId());
                                    if (otherPlayerSpaceship == null) {
                                        screen.addEnemySpaceShip(otherPlayer.getX(), otherPlayer.getY(), otherPlayer.getId());
                                    } else {
                                        otherPlayerSpaceship.setX(otherPlayer.getX());
                                        otherPlayerSpaceship.setY(otherPlayer.getY());
                                        otherPlayerSpaceship.setRotation(otherPlayer.getRotation());
                                        otherPlayerSpaceship.setMotionAngle(otherPlayer.getMotionAngle());
                                        otherPlayerSpaceship.setShieldPower(otherPlayer.getShieldPower());
                                        otherPlayerSpaceship.setThrustersVisible(otherPlayer.isThrustersVisible());

                                        if (otherPlayer.getLasers().size() > 0) {
                                            screen.addLasers(otherPlayer.getLasers());
                                            otherPlayer.getLasers().clear();
                                        }
                                    }
                                }
                            });
                        }

                        //make buffer ready for writing (pos=0, lim=capacity)
                        buffer.clear();

                        //write into buffer if it has space
                        if (buffer.hasRemaining()) {
                            byte thrustersVisible = (byte) (spaceship.areThrustersVisible() ? 1 : 0);

                            FloatBuffer fb = buffer.asFloatBuffer()
                                    .put(spaceship.getX())
                                    .put(spaceship.getY())
                                    .put(spaceship.getRotation())
                                    .put(spaceship.getMotionAngle())
                                    .put(spaceship.getShieldPower())
                                    .put(thrustersVisible)
                                    .put(spaceship.lasersCount());

                            for (LaserEntity newLaser : spaceship.getNewLasers()) {
                                fb.put(BYTE_BUFFER_LASER);
                                fb
                                        .put(newLaser.getX())
                                        .put(newLaser.getY())
                                        .put(newLaser.getRotation())
                                        .put(newLaser.getMotionAngle());
                            }

                            buffer.position(fb.position() * Float.BYTES);

                            List<String> messages = screen.getMessagesToSend();
                            if (!messages.isEmpty()) {
                                fb.put(BYTE_BUFFER_MESSAGE_CONTENT);
                                buffer.position(fb.position() * Float.BYTES);

                                for (String message : messages) {
                                    buffer.put(message.getBytes());
                                }

                                buffer.put(BYTE_BUFFER_MESSAGE_END);
                                messages.clear();
                            }

                            //make buffer ready for reading
                            buffer.flip();

                            //read from buffer -> channel
                            client.write(buffer);
                            //empty buffer
                            buffer.clear();
                        }
                        Thread.sleep(10);
                    }

                    //Thread.sleep(250);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void connectNio2() {
        String host = "172.24.25.33";
        int port = 43;

        Spaceship spaceship = screen.addSpaceShip(400, 300);
        player = new PlayerEntity(spaceship.getX(), spaceship.getY(), spaceship.getRotation(), spaceship.getMotionAngle(), spaceship.getShieldPower());

        try {
            SocketAddress address = new InetSocketAddress(host, port);
            java.nio.channels.SocketChannel client = java.nio.channels.SocketChannel.open(address);

            client.configureBlocking(false);
            ByteBuffer buffer = ByteBuffer.allocate(Float.BYTES * 1000);

            new Thread(() -> {
                try {
                    while (true) {
                        //make buffer ready for writing
                        buffer.clear();
                        //write from channel to buffer
                        int bytesRead = client.read(buffer);
                        List<MessageEntity> chatMessages = new ArrayList<>();
                        if (bytesRead > 0) {
                            //MEASUREMENTS
                            measureTime();

                            //make buffer ready for reading (lim=pos, pos=0)
                            buffer.flip();
                            //clear list of enemies before acquiring them from buffer
                            Map<Integer, PlayerEntity> otherPlayers = new HashMap<>();
                            float nextValue;

                            if (buffer.hasRemaining()) {
                                nextValue = buffer.getFloat();
                                while (buffer.hasRemaining() && nextValue == BYTE_BUFFER_OTHER_PLAYERS) {
                                    int id = (int) buffer.getFloat();
                                    if (id < 0 || id > 100_000) {
                                        buffer.position(buffer.position() - Float.BYTES * 2);
                                        break;
                                    }
                                    PlayerEntity otherPlayer = otherPlayers.get(id);
                                    if (otherPlayer == null) {
                                        otherPlayer = new PlayerEntity();
                                        otherPlayer.setId(id);
                                    }
                                    otherPlayer.setX(buffer.getFloat());
                                    otherPlayer.setY(buffer.getFloat());
                                    otherPlayer.setRotation(buffer.getFloat());
                                    otherPlayer.setMotionAngle(buffer.getFloat());
                                    otherPlayer.setShieldPower((int) buffer.getFloat());
                                    boolean thrustersVisible = buffer.getFloat() == 1;
                                    otherPlayer.setThrustersVisible(thrustersVisible);

                                    List<LaserEntity> lasers = new ArrayList<>();
                                    if (buffer.hasRemaining()) {
                                        nextValue = buffer.getFloat();
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

                                    otherPlayer.getLasers().addAll(lasers);

                                    //System.out.println("Client received object: " + otherPlayer.getId() + ", lasers: " + otherPlayer.getLasers().size());

                                        /*if(otherPlayers.stream().noneMatch(o -> o.getId() == id)) {
                                            otherPlayers.add(otherPlayer);
                                        }*/
                                    otherPlayers.put(id, otherPlayer);
                                    if (buffer.hasRemaining()) {
                                        nextValue = buffer.getFloat();
                                    }
                                }
                                if (nextValue != BYTE_BUFFER_OTHER_PLAYERS) {
                                    buffer.position(buffer.position() - Float.BYTES);
                                }
                            }

                            enemiesCount = otherPlayers.size();

                            if (buffer.hasRemaining()) {
                                nextValue = (int) buffer.getFloat();
                                if (nextValue == BYTE_BUFFER_SOUND_EFFECTS) {
                                    int newAudioSignals = (int) buffer.getFloat();
                                    if (!GlobalConfig.isMuted) {
                                        for (int i = 0; i < newAudioSignals; i++) {
                                            Sound laserSound = Gdx.audio.newSound(Gdx.files.internal("assets/laser.mp3"));
                                            laserSound.play(1.0f);
                                        }
                                    }
                                } else {
                                    buffer.position(buffer.position() - Float.BYTES);
                                }
                            }

                            if (buffer.hasRemaining()) {
                                nextValue = buffer.get();
                                while (nextValue == BYTE_BUFFER_MESSAGE_SENDER && buffer.hasRemaining()) {
                                    List<Byte> senderBytesList = new ArrayList<>();
                                    List<Byte> contentBytesList = new ArrayList<>();
                                    // int messageId = buffer.get();
                                    byte nextByte;
                                    while ((nextByte = buffer.get()) != BYTE_BUFFER_MESSAGE_CONTENT && buffer.hasRemaining()) {
                                        senderBytesList.add(nextByte);
                                    }
                                    while (nextByte != BYTE_BUFFER_MESSAGE_COLOR && buffer.hasRemaining()) {
                                        contentBytesList.add(nextByte);
                                        nextByte = buffer.get();
                                    }
                                    if (!buffer.hasRemaining()) {
                                        break;
                                    }
                                    byte r = buffer.get();
                                    byte g = buffer.get();
                                    byte b = buffer.get();

                                    byte[] senderBytes = ArrayUtils.toPrimitive(senderBytesList.toArray(new Byte[0]));
                                    byte[] contentBytes = ArrayUtils.toPrimitive(contentBytesList.toArray(new Byte[0]));
                                    MessageEntity msg = new MessageEntity(new String(senderBytes), new String(contentBytes), new float[]{r, g, b});
                                    //messageEntities.add(msg);

                                    // messagesIdsToContents.put(messageId, msg);
                                    chatMessages.add(msg);
                                    if (buffer.hasRemaining()) {
                                        nextValue = buffer.get();
                                    }
                                }
                                if (nextValue != BYTE_BUFFER_MESSAGE_SENDER) {
                                    buffer.position(buffer.position() - Byte.BYTES);
                                }

                                //System.out.println("Chat messages: " + screen.getChatMessages().size());
                                //System.out.println("Messages from server: " + chatMessages.size());
                                if (screen.getChatMessages().size() != chatMessages.size()) {
                                    screen.getChatMessages().clear();
                                    screen.getChatMessages().addAll(chatMessages);
                                }
                            }

                            Gdx.app.postRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    Set<Integer> enemiesIds = screen.getEnemySpaceships().keySet();
                                    List<PlayerEntity> otherPlayersEntities = new ArrayList<>(otherPlayers.values());
                                    Set<Integer> toRemove = new HashSet<>();

                                    for (Integer enemyId : enemiesIds) {
                                        if (otherPlayersEntities.stream().noneMatch(p -> p.getId() == enemyId)) {
                                            screen.getEnemySpaceships().get(enemyId).addAction(Actions.removeActor());
                                            toRemove.add(enemyId);
                                        }
                                    }
                                    screen.getEnemySpaceships().keySet().removeAll(toRemove);

                                       /* for (Integer playerId : otherPlayers.keySet()) {
                                            PlayerEntity otherPlayer = otherPlayers.get(playerId);
                                            if (otherPlayer != null && otherPlayer.getLasers().size() > 0) {
                                                System.out.println("[runnable] OtherPlayer: id=" + otherPlayer.getId() + ", lasers=" + otherPlayer.getLasers().size());
                                            }
                                        }*/
                                    for (Integer playerId : otherPlayers.keySet()) {
                                        PlayerEntity otherPlayer = otherPlayers.get(playerId);
                                        SpaceshipEnemy otherPlayerSpaceship = screen.getEnemySpaceships().get(otherPlayer.getId());
                                        if (otherPlayerSpaceship == null) {
                                            //System.out.println("Adding new enemy spaceship");
                                            screen.addEnemySpaceShip(otherPlayer.getX(), otherPlayer.getY(), otherPlayer.getId());
                                        } else if (otherPlayer.isDestroyed()) {
                                            screen.removeDestroyedSpaceship(otherPlayerSpaceship);
                                            if (otherPlayers.size() == 1) {
                                                screen.addWinMessage();
                                            }
                                        } else {
                                            otherPlayerSpaceship.setX(otherPlayer.getX());
                                            otherPlayerSpaceship.setY(otherPlayer.getY());
                                            otherPlayerSpaceship.setRotation(otherPlayer.getRotation());
                                            otherPlayerSpaceship.setMotionAngle(otherPlayer.getMotionAngle());
                                            otherPlayerSpaceship.setShieldPower(otherPlayer.getShieldPower());
                                            otherPlayerSpaceship.setThrustersVisible(otherPlayer.isThrustersVisible());

                                            if (otherPlayer.getLasers().size() > 0) {
                                                //System.out.println("Adding lasers: " + otherPlayer.getLasers().size());
                                                screen.addLasers(otherPlayer.getLasers());
                                                otherPlayer.getLasers().clear();
                                            }
                                        }
                                    }
                                }
                                // }
                            });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
            new Thread(() -> {
                try {
                    while (true) {
                        //make buffer ready for writing (pos=0, lim=capacity)
                        buffer.clear();

                        //write into buffer if it has space
                        if (buffer.position() == 0 && buffer.limit() == buffer.capacity()/*buffer.remaining() >= (7 + spaceship.lasersCount() * 5) * Float.BYTES*/) {
                            byte thrustersVisible = (byte) (spaceship.areThrustersVisible() ? 1 : 0);

                            FloatBuffer fb = buffer.asFloatBuffer()
                                    .put(spaceship.getX())
                                    .put(spaceship.getY())
                                    .put(spaceship.getRotation())
                                    .put(spaceship.getMotionAngle())
                                    .put(spaceship.getShieldPower())
                                    .put(thrustersVisible)
                                    .put(spaceship.lasersCount());

                            for (LaserEntity newLaser : spaceship.getNewLasers()) {
                                fb.put(BYTE_BUFFER_LASER);
                                fb
                                        .put(newLaser.getX())
                                        .put(newLaser.getY())
                                        .put(newLaser.getRotation())
                                        .put(newLaser.getMotionAngle());
                                // System.out.println("Sending new laser, X=" + newLaser.getX() + ", Y=" + newLaser.getY());
                            }

                            buffer.position(fb.position() * Float.BYTES);

                            List<String> messages = screen.getMessagesToSend();
                            if (!messages.isEmpty()) {
                                fb.put(BYTE_BUFFER_MESSAGE_CONTENT);
                                buffer.position(fb.position() * Float.BYTES);

                                for (String message : messages) {
                                    buffer.put(message.getBytes());
                                }

                                buffer.put(BYTE_BUFFER_MESSAGE_END);
                                messages.clear();
                            }

                            //make buffer ready for reading
                            buffer.flip();

                            //read from buffer -> channel
                            //System.out.println("Client sending object");
                            client.write(buffer);

                            //empty buffer
                            buffer.clear();
                        }
                        Thread.sleep(10);
                    }

                    //Thread.sleep(250);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void connect() {
        String host = "172.24.25.33";
        int port = 43;

        Spaceship spaceship = screen.addSpaceShip(400, 300);
        player = new PlayerEntity(spaceship.getX(), spaceship.getY(), spaceship.getRotation(), spaceship.getMotionAngle(), spaceship.getShieldPower());

        float delta = 1 / 60;

        try {
            Socket socket = Gdx.net.newClientSocket(Net.Protocol.TCP, host, port, null);

            new Thread(() -> {
                try {
                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                    //List<Object> objects = new ArrayList<>();
                    Object[] objects = new Object[3];
                    RawMessagesEntity chatMessagesEntity = new RawMessagesEntity();
                    while (true) {
                        // objects.clear();
                        player.setX(spaceship.getX());
                        player.setY(spaceship.getY());
                        player.setRotation(spaceship.getRotation());
                        player.setMotionAngle(spaceship.getMotionAngle());
                        player.setShieldPower(spaceship.getShieldPower());
                        player.setThrustersVisible(spaceship.areThrustersVisible());
                        player.setLasers(spaceship.getNewLasers());
                        player.setNewAudioSignals(player.getLasers().size());

                        //objects.add(player);
                        objects[0] = player;
                        //oos.writeObject(player);

                        List<String> messages = screen.getMessagesToSend();
                        chatMessagesEntity.getRawMessages().clear();
                        chatMessagesEntity.getRawMessages().addAll(messages);

                        // objects.add(chatMessagesEntity);
                        objects[1] = chatMessagesEntity;
                        // oos.writeObject(chatMessagesEntity);
                        messages.clear();

                        oos.writeObject(objects);
                        oos.reset();
                        oos.flush();

                        /*ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                        OtherPlayersEntity otherPlayers = (OtherPlayersEntity) ois.readObject();
                        enemiesCount = otherPlayers.getPlayers().size();
                        ChatMessagesEntity chatMessages = (ChatMessagesEntity) ois.readObject();
                        Integer newAudioSignals = (Integer) ois.readObject();

                        //MEASUREMENTS
                        measureTime();

                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                Set<Integer> enemiesIds = screen.getEnemySpaceships().keySet();
                                List<PlayerEntity> otherPlayersEntities = otherPlayers.getPlayers();
                                Set<Integer> toRemove = new HashSet<>();

                                for (Integer enemyId : enemiesIds) {
                                    if (otherPlayersEntities.stream().noneMatch(p -> p.getId() == enemyId)) {
                                        screen.getEnemySpaceships().get(enemyId).addAction(Actions.removeActor());
                                        toRemove.add(enemyId);
                                    }
                                }
                                screen.getEnemySpaceships().keySet().removeAll(toRemove);

                                for (PlayerEntity otherPlayer : otherPlayersEntities) {
                                    SpaceshipEnemy otherPlayerSpaceship = screen.getEnemySpaceships().get(otherPlayer.getId());
                                    if (otherPlayerSpaceship == null) {
                                        screen.addEnemySpaceShip(otherPlayer.getX(), otherPlayer.getY(), otherPlayer.getId());
                                    } else if (otherPlayer.isDestroyed()) {
                                        screen.removeDestroyedSpaceship(otherPlayerSpaceship);
                                        if (otherPlayersEntities.size() == 1) {
                                            screen.addWinMessage();
                                        }
                                    } else {
                                        otherPlayerSpaceship.setX(otherPlayer.getX());
                                        otherPlayerSpaceship.setY(otherPlayer.getY());
                                        otherPlayerSpaceship.setRotation(otherPlayer.getRotation());
                                        otherPlayerSpaceship.setMotionAngle(otherPlayer.getMotionAngle());
                                        otherPlayerSpaceship.setShieldPower(otherPlayer.getShieldPower());
                                        otherPlayerSpaceship.setThrustersVisible(otherPlayer.isThrustersVisible());

                                        screen.addLasers(otherPlayer.getLasers());
                                    }
                                }

                                if (chatMessages != null) {
                                    List<MessageEntity> chatMessageEntities = chatMessages.getMessageEntities();

                                    if (screen.getChatMessages().size() != chatMessageEntities.size()) {
                                        screen.getChatMessages().clear();
                                        screen.getChatMessages().addAll(chatMessageEntities);
                                    }
                                }

                                if (!GlobalConfig.isMuted) {
                                    if (newAudioSignals > 0) {
                                        System.out.println("new signals: " + newAudioSignals);
                                    }
                                    for (int i = 0; i < newAudioSignals; i++) {
                                        Sound laserSound = Gdx.audio.newSound(Gdx.files.internal("assets/laser.mp3"));
                                        laserSound.play(1.0f);
                                    }
                                }
                            }
                        });*/

                        Thread.sleep(10);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
            new Thread(() -> {
                try {
                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                    while (true) {
                        if (socket.getInputStream().available() > 0) {
                            //MEASUREMENTS
                            measureTime();

                            //List<Object> objects = (List<Object>) ois.readObject();
                            Object[] objects = (Object[]) ois.readObject();
                            // System.out.println("Received object: " + objects.get(0));
                            OtherPlayersEntity otherPlayers = (OtherPlayersEntity) objects[0];//.get(0);
                            ChatMessagesEntity chatMessages = (ChatMessagesEntity) objects[1];//.get(1);
                            Integer newAudioSignals = (Integer) objects[2];//.get(2);
                            /*OtherPlayersEntity otherPlayers = (OtherPlayersEntity) ois.readObject();
                            ChatMessagesEntity chatMessages = (ChatMessagesEntity) ois.readObject();
                            Integer newAudioSignals = (Integer) ois.readObject();*/

                            enemiesCount = otherPlayers.getPlayers().size();

                            Gdx.app.postRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    Set<Integer> enemiesIds = screen.getEnemySpaceships().keySet();
                                    List<PlayerEntity> otherPlayersEntities = otherPlayers.getPlayers();
                                    Set<Integer> toRemove = new HashSet<>();

                                    for (Integer enemyId : enemiesIds) {
                                        if (otherPlayersEntities.stream().noneMatch(p -> p.getId() == enemyId)) {
                                            screen.getEnemySpaceships().get(enemyId).addAction(Actions.removeActor());
                                            toRemove.add(enemyId);
                                        }
                                    }
                                    screen.getEnemySpaceships().keySet().removeAll(toRemove);

                                    for (PlayerEntity otherPlayer : otherPlayersEntities) {
                                        SpaceshipEnemy otherPlayerSpaceship = screen.getEnemySpaceships().get(otherPlayer.getId());
                                        if (otherPlayerSpaceship == null) {
                                            //    System.out.println("[ " + player.getId() + "] Adding spaceship: " + otherPlayer);
                                            screen.addEnemySpaceShip(otherPlayer.getX(), otherPlayer.getY(), otherPlayer.getId());
                                        } else {
                                            //    System.out.println("[ " + player.getId() + "] Updating spaceship: "+ otherPlayer);
                                            otherPlayerSpaceship.setX(otherPlayer.getX());
                                            otherPlayerSpaceship.setY(otherPlayer.getY());
                                            otherPlayerSpaceship.setRotation(otherPlayer.getRotation());
                                            otherPlayerSpaceship.setMotionAngle(otherPlayer.getMotionAngle());
                                            otherPlayerSpaceship.setShieldPower(otherPlayer.getShieldPower());
                                            otherPlayerSpaceship.setThrustersVisible(otherPlayer.isThrustersVisible());

                                            screen.addLasers(otherPlayer.getLasers());
                                        }
                                    }

                                    if (chatMessages != null) {
                                        List<MessageEntity> chatMessageEntities = chatMessages.getMessageEntities();

                                        if (screen.getChatMessages().size() != chatMessageEntities.size()) {
                                            screen.getChatMessages().clear();
                                            screen.getChatMessages().addAll(chatMessageEntities);
                                        }
                                    }

                                    if (!GlobalConfig.isMuted) {
                                        if (newAudioSignals > 0) {
                                            System.out.println("new signals: " + newAudioSignals);
                                        }
                                        for (int i = 0; i < newAudioSignals; i++) {
                                            Sound laserSound = Gdx.audio.newSound(Gdx.files.internal("assets/laser.mp3"));
                                            laserSound.play(1.0f);
                                        }
                                    }
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void connect2() {
        String host = "172.24.25.33";
        int port = 43;

        Spaceship spaceship = screen.addSpaceShip(400, 300);
        player = new PlayerEntity(spaceship.getX(), spaceship.getY(), spaceship.getRotation(), spaceship.getMotionAngle(), spaceship.getShieldPower());

        float delta = 1 / 60;

        try {
            Socket socket = Gdx.net.newClientSocket(Net.Protocol.TCP, host, port, null);

            new Thread(() -> {
                try {
                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                    Object[] objects = new Object[3];
                    RawMessagesEntity chatMessagesEntity = new RawMessagesEntity();
                    while (true) {
                        player.setX(spaceship.getX());
                        player.setY(spaceship.getY());
                        player.setRotation(spaceship.getRotation());
                        player.setMotionAngle(spaceship.getMotionAngle());
                        player.setShieldPower(spaceship.getShieldPower());
                        player.setThrustersVisible(spaceship.areThrustersVisible());
                        player.setLasers(spaceship.getNewLasers());
                        player.setNewAudioSignals(player.getLasers().size());

                        objects[0] = player;
                        //oos.writeObject(player);

                        List<String> messages = screen.getMessagesToSend();
                        chatMessagesEntity.getRawMessages().clear();
                        chatMessagesEntity.getRawMessages().addAll(messages);

                        // objects.add(chatMessagesEntity);
                        objects[1] = chatMessagesEntity;
                        // oos.writeObject(chatMessagesEntity);
                        messages.clear();

                        oos.writeObject(objects);
                        oos.reset();
                        oos.flush();

                        Object[] objectResponse = (Object[]) ois.readObject();
                        OtherPlayersEntity otherPlayers = (OtherPlayersEntity) objectResponse[0];
                        enemiesCount = otherPlayers.getPlayers().size();
                        ChatMessagesEntity chatMessages = (ChatMessagesEntity) objectResponse[1];
                        Integer newAudioSignals = (Integer) objectResponse[2];
                        /*ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                        OtherPlayersEntity otherPlayers = (OtherPlayersEntity) ois.readObject();
                        enemiesCount = otherPlayers.getPlayers().size();
                        ChatMessagesEntity chatMessages = (ChatMessagesEntity) ois.readObject();
                        Integer newAudioSignals = (Integer) ois.readObject();*/

                        //MEASUREMENTS
                        measureTime();

                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                Set<Integer> enemiesIds = screen.getEnemySpaceships().keySet();
                                List<PlayerEntity> otherPlayersEntities = otherPlayers.getPlayers();
                                Set<Integer> toRemove = new HashSet<>();

                                for (Integer enemyId : enemiesIds) {
                                    if (otherPlayersEntities.stream().noneMatch(p -> p.getId() == enemyId)) {
                                        screen.getEnemySpaceships().get(enemyId).addAction(Actions.removeActor());
                                        toRemove.add(enemyId);
                                    }
                                }
                                screen.getEnemySpaceships().keySet().removeAll(toRemove);

                                for (PlayerEntity otherPlayer : otherPlayersEntities) {
                                    SpaceshipEnemy otherPlayerSpaceship = screen.getEnemySpaceships().get(otherPlayer.getId());
                                    if (otherPlayerSpaceship == null) {
                                        screen.addEnemySpaceShip(otherPlayer.getX(), otherPlayer.getY(), otherPlayer.getId());
                                    } else {
                                        otherPlayerSpaceship.setX(otherPlayer.getX());
                                        otherPlayerSpaceship.setY(otherPlayer.getY());
                                        otherPlayerSpaceship.setRotation(otherPlayer.getRotation());
                                        otherPlayerSpaceship.setMotionAngle(otherPlayer.getMotionAngle());
                                        otherPlayerSpaceship.setShieldPower(otherPlayer.getShieldPower());
                                        otherPlayerSpaceship.setThrustersVisible(otherPlayer.isThrustersVisible());

                                        screen.addLasers(otherPlayer.getLasers());
                                    }
                                }

                                if (chatMessages != null) {
                                    List<MessageEntity> chatMessageEntities = chatMessages.getMessageEntities();

                                    if (screen.getChatMessages().size() != chatMessageEntities.size()) {
                                        screen.getChatMessages().clear();
                                        screen.getChatMessages().addAll(chatMessageEntities);
                                    }
                                }

                                if (!GlobalConfig.isMuted) {
                                    if (newAudioSignals > 0) {
                                        System.out.println("new signals: " + newAudioSignals);
                                    }
                                    for (int i = 0; i < newAudioSignals; i++) {
                                        Sound laserSound = Gdx.audio.newSound(Gdx.files.internal("assets/laser.mp3"));
                                        laserSound.play(1.0f);
                                    }
                                }
                            }
                        });

                        Thread.sleep(10);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void connectIO() {
        String host = "172.24.25.33";
        int port = 43;

        Spaceship spaceship = screen.addSpaceShip(400, 300);
        player = new PlayerEntity(spaceship.getX(), spaceship.getY(), spaceship.getRotation(), spaceship.getMotionAngle(), spaceship.getShieldPower());

        try {
            java.net.Socket socket = new java.net.Socket();
            InetSocketAddress address = new InetSocketAddress(host, port);
            socket.connect(address);

            try {
                new Thread(() -> {
                    try {
                        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                        Object[] objects = new Object[2];
                        RawMessagesEntity chatMessagesEntity = new RawMessagesEntity();
                        while (true) {
                            player.setX(spaceship.getX());
                            player.setY(spaceship.getY());
                            player.setRotation(spaceship.getRotation());
                            player.setMotionAngle(spaceship.getMotionAngle());
                            player.setShieldPower(spaceship.getShieldPower());
                            player.setThrustersVisible(spaceship.areThrustersVisible());
                            player.setLasers(spaceship.getNewLasers());
                            player.setDestroyed(spaceship.isDestroyed());
                            player.setNewAudioSignals(player.getLasers().size());

                            List<String> messages = screen.getMessagesToSend();
                            chatMessagesEntity.getRawMessages().clear();
                            chatMessagesEntity.getRawMessages().addAll(messages);
                            messages.clear();

                            objects[0] = player;
                            objects[1] = chatMessagesEntity;

                            oos.writeObject(objects);
                            oos.reset();
                            oos.flush();

                            /*ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                            OtherPlayersEntity otherPlayers = (OtherPlayersEntity) ois.readObject();
                            enemiesCount = otherPlayers.getPlayers().size();
                            ChatMessagesEntity chatMessages = (ChatMessagesEntity) ois.readObject();
                            Integer newAudioSignals = (Integer) ois.readObject();

                            //MEASUREMENTS
                            measureTime();

                            Gdx.app.postRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    Set<Integer> enemiesIds = screen.getEnemySpaceships().keySet();
                                    List<PlayerEntity> otherPlayersEntities = otherPlayers.getPlayers();
                                    Set<Integer> toRemove = new HashSet<>();

                                    for (Integer enemyId : enemiesIds) {
                                        if (otherPlayersEntities.stream().noneMatch(p -> p.getId() == enemyId)) {
                                            screen.getEnemySpaceships().get(enemyId).addAction(Actions.removeActor());
                                            toRemove.add(enemyId);
                                        }
                                    }
                                    screen.getEnemySpaceships().keySet().removeAll(toRemove);

                                    for (PlayerEntity otherPlayer : otherPlayersEntities) {
                                        SpaceshipEnemy otherPlayerSpaceship = screen.getEnemySpaceships().get(otherPlayer.getId());
                                        if (otherPlayerSpaceship == null) {
                                            screen.addEnemySpaceShip(otherPlayer.getX(), otherPlayer.getY(), otherPlayer.getId());
                                        } else {
                                            otherPlayerSpaceship.setX(otherPlayer.getX());
                                            otherPlayerSpaceship.setY(otherPlayer.getY());
                                            otherPlayerSpaceship.setRotation(otherPlayer.getRotation());
                                            otherPlayerSpaceship.setMotionAngle(otherPlayer.getMotionAngle());
                                            otherPlayerSpaceship.setShieldPower(otherPlayer.getShieldPower());
                                            otherPlayerSpaceship.setThrustersVisible(otherPlayer.isThrustersVisible());

                                            screen.addLasers(otherPlayer.getLasers());
                                        }
                                    }

                                    if (chatMessages != null) {
                                        List<MessageEntity> chatMessageEntities = chatMessages.getMessageEntities();
                                        if (screen.getChatMessages().size() != chatMessageEntities.size()) {
                                            screen.getChatMessages().clear();
                                            screen.getChatMessages().addAll(chatMessageEntities);
                                        }
                                    }

                                    if (!GlobalConfig.isMuted) {
                                        for (int i = 0; i < newAudioSignals; i++) {
                                            Sound laserSound = Gdx.audio.newSound(Gdx.files.internal("assets/laser.mp3"));
                                            laserSound.play(1.0f);
                                        }
                                    }
                                }
                            });*/

                            Thread.sleep(10);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
                new Thread(() -> {
                    try {
                        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                        while (true) {
                            if (socket.getInputStream().available() > 0) {
                                //MEASUREMENTS
                                measureTime();

                                Object[] objects = (Object[]) ois.readObject();
                                OtherPlayersEntity otherPlayers = (OtherPlayersEntity) objects[0];

                                // OtherPlayersEntity otherPlayers = (OtherPlayersEntity) ois.readObject();
                                enemiesCount = otherPlayers.getPlayers().size();
                                ChatMessagesEntity chatMessages = (ChatMessagesEntity) objects[1]; //(ChatMessagesEntity) ois.readObject();
                                Integer newAudioSignals = (Integer) objects[2]; //(Integer) ois.readObject();

                                Gdx.app.postRunnable(new Runnable() {
                                    @Override
                                    public void run() {
                                        Set<Integer> enemiesIds = screen.getEnemySpaceships().keySet();
                                        List<PlayerEntity> otherPlayersEntities = otherPlayers.getPlayers();
                                        Set<Integer> toRemove = new HashSet<>();

                                        for (Integer enemyId : enemiesIds) {
                                            if (otherPlayersEntities.stream().noneMatch(p -> p.getId() == enemyId)) {
                                                screen.getEnemySpaceships().get(enemyId).addAction(Actions.removeActor());
                                                toRemove.add(enemyId);
                                            }
                                        }
                                        screen.getEnemySpaceships().keySet().removeAll(toRemove);

                                        for (PlayerEntity otherPlayer : otherPlayersEntities) {
                                            SpaceshipEnemy otherPlayerSpaceship = screen.getEnemySpaceships().get(otherPlayer.getId());
                                            if (otherPlayerSpaceship == null) {
                                                screen.addEnemySpaceShip(otherPlayer.getX(), otherPlayer.getY(), otherPlayer.getId());
                                            } else {
                                                otherPlayerSpaceship.setX(otherPlayer.getX());
                                                otherPlayerSpaceship.setY(otherPlayer.getY());
                                                otherPlayerSpaceship.setRotation(otherPlayer.getRotation());
                                                otherPlayerSpaceship.setMotionAngle(otherPlayer.getMotionAngle());
                                                otherPlayerSpaceship.setShieldPower(otherPlayer.getShieldPower());
                                                otherPlayerSpaceship.setThrustersVisible(otherPlayer.isThrustersVisible());

                                                screen.addLasers(otherPlayer.getLasers());
                                            }
                                        }

                                        if (chatMessages != null) {
                                            List<MessageEntity> chatMessageEntities = chatMessages.getMessageEntities();
                                            if (screen.getChatMessages().size() != chatMessageEntities.size()) {
                                                screen.getChatMessages().clear();
                                                screen.getChatMessages().addAll(chatMessageEntities);
                                            }
                                        }

                                        if (!GlobalConfig.isMuted) {
                                            for (int i = 0; i < newAudioSignals; i++) {
                                                Sound laserSound = Gdx.audio.newSound(Gdx.files.internal("assets/laser.mp3"));
                                                laserSound.play(1.0f);
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException ex) {
            System.err.println("Could not connect to server");
        }
    }

    public void connectIO2() {
        String host = "172.24.25.33";
        int port = 43;

        Spaceship spaceship = screen.addSpaceShip(400, 300);
        player = new PlayerEntity(spaceship.getX(), spaceship.getY(), spaceship.getRotation(), spaceship.getMotionAngle(), spaceship.getShieldPower());

        try {
            java.net.Socket socket = new java.net.Socket();
            InetSocketAddress address = new InetSocketAddress(host, port);
            socket.connect(address);

            try {
                new Thread(() -> {
                    try {
                        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                        Object[] objects = new Object[2];
                        RawMessagesEntity chatMessagesEntity = new RawMessagesEntity();
                        while (true) {
                            player.setX(spaceship.getX());
                            player.setY(spaceship.getY());
                            player.setRotation(spaceship.getRotation());
                            player.setMotionAngle(spaceship.getMotionAngle());
                            player.setShieldPower(spaceship.getShieldPower());
                            player.setThrustersVisible(spaceship.areThrustersVisible());
                            player.setLasers(spaceship.getNewLasers());
                            player.setNewAudioSignals(player.getLasers().size());

                            List<String> messages = screen.getMessagesToSend();
                            chatMessagesEntity.getRawMessages().clear();
                            chatMessagesEntity.getRawMessages().addAll(messages);
                            messages.clear();

                            objects[0] = player;
                            objects[1] = chatMessagesEntity;

                            oos.writeObject(objects);
                            oos.reset();
                            oos.flush();

                            Object[] objectsResponse = (Object[]) ois.readObject();
                            OtherPlayersEntity otherPlayers = (OtherPlayersEntity) objectsResponse[0];
                            enemiesCount = otherPlayers.getPlayers().size();
                            ChatMessagesEntity chatMessages = (ChatMessagesEntity) objectsResponse[1];
                            Integer newAudioSignals = (Integer) objectsResponse[2];

                            //MEASUREMENTS
                            measureTime();

                            Gdx.app.postRunnable(() -> {
                                Set<Integer> enemiesIds = screen.getEnemySpaceships().keySet();
                                List<PlayerEntity> otherPlayersEntities = otherPlayers.getPlayers();
                                Set<Integer> toRemove = new HashSet<>();

                                for (Integer enemyId : enemiesIds) {
                                    if (otherPlayersEntities.stream().noneMatch(p -> p.getId() == enemyId)) {
                                        screen.getEnemySpaceships().get(enemyId).addAction(Actions.removeActor());
                                        toRemove.add(enemyId);
                                    }
                                }
                                screen.getEnemySpaceships().keySet().removeAll(toRemove);

                                for (PlayerEntity otherPlayer : otherPlayersEntities) {
                                    SpaceshipEnemy otherPlayerSpaceship = screen.getEnemySpaceships().get(otherPlayer.getId());
                                    if (otherPlayerSpaceship == null) {
                                        screen.addEnemySpaceShip(otherPlayer.getX(), otherPlayer.getY(), otherPlayer.getId());
                                    } else {
                                        otherPlayerSpaceship.setX(otherPlayer.getX());
                                        otherPlayerSpaceship.setY(otherPlayer.getY());
                                        otherPlayerSpaceship.setRotation(otherPlayer.getRotation());
                                        otherPlayerSpaceship.setMotionAngle(otherPlayer.getMotionAngle());
                                        otherPlayerSpaceship.setShieldPower(otherPlayer.getShieldPower());
                                        otherPlayerSpaceship.setThrustersVisible(otherPlayer.isThrustersVisible());

                                        screen.addLasers(otherPlayer.getLasers());
                                    }
                                }

                                if (chatMessages != null) {
                                    List<MessageEntity> chatMessageEntities = chatMessages.getMessageEntities();
                                    if (screen.getChatMessages().size() != chatMessageEntities.size()) {
                                        screen.getChatMessages().clear();
                                        screen.getChatMessages().addAll(chatMessageEntities);
                                    }
                                }

                                if (!GlobalConfig.isMuted) {
                                    for (int i = 0; i < newAudioSignals; i++) {
                                        Sound laserSound = Gdx.audio.newSound(Gdx.files.internal("assets/laser.mp3"));
                                        laserSound.play(1.0f);
                                    }
                                }
                            });

                            Thread.sleep(10);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException ex) {
            System.err.println("Could not connect to server");
        }
    }

    public void connectNetty() {
        String host = "172.24.25.33";
        int port = 43;
        Spaceship spaceship = screen.addSpaceShip(400, 300);
        this.player = new PlayerEntity(spaceship.getX(), spaceship.getY(), spaceship.getRotation(), spaceship.getMotionAngle(), spaceship.getShieldPower());

        new Thread(() -> {
            EventLoopGroup group = new NioEventLoopGroup();
            try {
                Bootstrap b = new Bootstrap();
                b.group(group)
                        .channel(NioSocketChannel.class)
                        .remoteAddress(new InetSocketAddress(host, port))
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel ch) throws Exception {
                                ChannelPipeline p = ch.pipeline();
                                p.addLast(
                                        new ObjectEncoder(),
                                        new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                        new ClientNettyHandler(screen));
                            }
                        });
                ChannelFuture f = b.connect().sync();

                new Thread(() -> {
                    try {
                        while (true) {
                            Channel channel = f.sync().channel();

                            player.setX(spaceship.getX());
                            player.setY(spaceship.getY());
                            player.setRotation(spaceship.getRotation());
                            player.setMotionAngle(spaceship.getMotionAngle());
                            player.setShieldPower(spaceship.getShieldPower());
                            player.setThrustersVisible(spaceship.areThrustersVisible());
                            player.setLasers(spaceship.getNewLasers());
                            player.setNewAudioSignals(player.getLasers().size());

                            channel.write(player);

                            List<String> messages = screen.getMessagesToSend();

                            if (!messages.isEmpty()) {
                                RawMessagesEntity chatMessagesEntity = new RawMessagesEntity();
                                chatMessagesEntity.getRawMessages().addAll(messages);
                                messages.clear();

                                channel.write(chatMessagesEntity);
                            }

                            channel.flush();
                            Thread.sleep(10);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        f.channel().close();
                    }
                }).start();

                f.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                try {
                    group.shutdownGracefully().sync();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void measureTime() {
        if (ResponseTimeTest.clientsCount == enemiesCount + 1) {
            if (serverResponseTime != 0) {
                long responseTimeFinish = System.nanoTime();
                totalResponseTime += (responseTimeFinish - serverResponseTime) / 1_000_000d;
                serverResponseTime = responseTimeFinish;
                iterationsCount++;
                if (iterationsCount == 5000) {
                    System.out.println(totalResponseTime / iterationsCount);
                }
            } else {
                serverResponseTime = System.nanoTime();
            }
        }
    }

    public static void main(String... args) {
        new SpaceGame().connectNetty();
    }
}
