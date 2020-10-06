package game;

import actor.Spaceship;
import actor.SpaceshipEnemy;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.Socket;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.DefaultSerializers;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import entity.LaserEntity;
import entity.OtherPlayersEntity;
import entity.PlayerEntity;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import netty.OtherPlayersEntityDecoder;
import netty.PlayerEntityEncoder;
import screen.LevelScreen;
import test.ResponseTimeTest;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("Duplicates")
public class SpaceGame extends BaseGame {
    private static final int BUFFER_SIZE = Float.BYTES * 1000;
    private static final int BUFFER_WRITE_SIZE = Float.BYTES * 100;

    public static final int GAME_WIDTH = 1200;
    public static final int GAME_HEIGHT = 900;

    private static final float BYTE_BUFFER_LASER = -1.0f;

    public LevelScreen screen;
    private PlayerEntity player;

    long serverResponseTime = 0;
    double totalResponseTime = 0;
    long iterationsCount = 0;
    private int enemiesCount;

    @Override
    public void create() {
        super.create();
        screen = new LevelScreen();
        setScreen(screen);

        //connectKryo();
        //connectUDP();
        //connectNioUDP();
        connectNetty();
        //connect();
    }

    @Override
    public void update(float delta) {
        getScreen().render(delta);
    }

    private void connectUDP() {
        // #1
        String host = "172.24.25.33";
        int port = 43;
        byte[] dataReceived = new byte[BUFFER_SIZE];
        InetAddress serverAddress;

        // #2
        Spaceship spaceship = screen.addSpaceShip(400, 300);
        player = new PlayerEntity(spaceship.getX(), spaceship.getY(), spaceship.getRotation(), spaceship.getMotionAngle(), spaceship.getShieldPower());

        // #3
        try {
            serverAddress = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return;
        }

        new Thread(() -> {
            try (DatagramSocket ds = new DatagramSocket()) {
                while (true) {
                    // #4
                    player.setX(spaceship.getX());
                    player.setY(spaceship.getY());
                    player.setRotation(spaceship.getRotation());
                    player.setMotionAngle(spaceship.getMotionAngle());
                    player.setShieldPower(spaceship.getShieldPower());
                    player.setThrustersVisible(spaceship.areThrustersVisible());
                    player.setLasers(spaceship.getNewLasers());

                    // #5
                    ByteArrayOutputStream outputStream = new
                            ByteArrayOutputStream(BUFFER_WRITE_SIZE);
                    ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(outputStream));
                    oos.writeObject(player);
                    oos.flush();

                    // #6
                    byte[] sendData = outputStream.toByteArray();
                    DatagramPacket packet = new DatagramPacket(sendData, sendData.length, serverAddress, port);
                    ds.send(packet);

                    // #7
                    packet = new DatagramPacket(dataReceived, dataReceived.length);
                    ds.receive(packet);

                    // #8
                    measure();

                    // #9
                    ByteArrayInputStream inputStream = new
                            ByteArrayInputStream(dataReceived);
                    ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(inputStream));
                    OtherPlayersEntity otherPlayers = (OtherPlayersEntity) ois.readObject();
                    enemiesCount = otherPlayers.getPlayers().size();

                    // #10
                    Gdx.app.postRunnable(() -> {
                        for (PlayerEntity otherPlayer : otherPlayers.getPlayers()) {
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
                    });

                    // #11
                    Thread.sleep(10);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void connectKryo() {
        String host = "172.24.25.33";
        int port = 43;
        Spaceship spaceship = screen.addSpaceShip(400, 300);
        player = new PlayerEntity(spaceship.getX(), spaceship.getY(), spaceship.getRotation(), spaceship.getMotionAngle(), spaceship.getShieldPower());

        // #1
        Client client = new Client();

        try {
            client.start();

            // #2
            Kryo kryo = client.getKryo();
            kryo.register(PlayerEntity.class);
            kryo.register(LaserEntity.class);
            kryo.register(OtherPlayersEntity.class);
            kryo.register(List.class);
            kryo.register(ArrayList.class);
            kryo.register(Collections.EMPTY_LIST.getClass(), new
                    DefaultSerializers.CollectionsEmptyListSerializer());

            // #3
            client.addListener(new Listener() {

                @Override
                public void connected(Connection connection) {
                    System.out.println("Client connected");
                    player.setId(connection.getID());
                }

                @Override
                public void received(Connection connection, Object object) {
                    measure();
                    if (object instanceof OtherPlayersEntity) {
                        OtherPlayersEntity otherPlayers = (OtherPlayersEntity) object;
                        enemiesCount = otherPlayers.getPlayers().size();
                        Gdx.app.postRunnable(() -> {
                            for (PlayerEntity otherPlayer : otherPlayers.getPlayers()) {
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
                        });
                    }
                }
            });

            // #4
            client.connect(5000, host, 42, port);

            // #5
            new Thread(() -> {
                try {
                    while (true) {
                        player.setX(spaceship.getX());
                        player.setY(spaceship.getY());
                        player.setRotation(spaceship.getRotation());
                        player.setMotionAngle(spaceship.getMotionAngle());
                        player.setShieldPower(spaceship.getShieldPower());
                        player.setThrustersVisible(spaceship.areThrustersVisible());
                        player.setLasers(spaceship.getNewLasers());

                        client.sendUDP(player);
                        Thread.sleep(10);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    client.close();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
            client.close();
        }
    }

    private void connectNioUDP() {
        // #1
        String host = "172.24.25.33";
        int port = 43;
        Spaceship spaceship = screen.addSpaceShip(400, 300);

        try {
            // #2
            DatagramChannel client = DatagramChannel.open();

            InetSocketAddress address = new InetSocketAddress(host, port);
            client.configureBlocking(false);

            ByteBuffer buffer = ByteBuffer.allocate(Float.BYTES * 1000);

            new Thread(() -> {
                try {
                    while (true) {
                        // #3
                        // make buffer ready for writing
                        buffer.clear();
                        // write from channel to buffer
                        client.receive(buffer);

                        // #4
                        measure();

                        // #5
                        // make buffer ready for reading (lim=pos, pos=0)
                        buffer.flip();
                        List<PlayerEntity> otherPlayers = new ArrayList<>();

                        // #6
                        while (buffer.hasRemaining()) {
                            int id = (int) buffer.getFloat();
                            PlayerEntity otherPlayer = new PlayerEntity(
                                    buffer.getFloat(),
                                    buffer.getFloat(),
                                    buffer.getFloat(),
                                    buffer.getFloat(),
                                    (int) buffer.getFloat()
                            );
                            boolean thrustersVisible = buffer.getFloat() == 1;
                            otherPlayer.setThrustersVisible(thrustersVisible);
                            otherPlayer.setId(id);

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
                            otherPlayer.setLasers(lasers);
                            otherPlayers.add(otherPlayer);
                        }
                        enemiesCount = otherPlayers.size();

                        // #7
                        Gdx.app.postRunnable(() -> {
                            for (PlayerEntity otherPlayer : otherPlayers) {
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
                        });

                        // #8
                        //make buffer ready for writing (pos=0, lim=capacity)
                        buffer.clear();

                        byte thrustersVisible = (byte) (spaceship.areThrustersVisible() ? 1 : 0);
                        FloatBuffer fb = buffer.asFloatBuffer()
                                .put(spaceship.getX())
                                .put(spaceship.getY())
                                .put(spaceship.getRotation())
                                .put(spaceship.getMotionAngle())
                                .put(spaceship.getShieldPower());
                        fb.put(thrustersVisible);

                        for (LaserEntity newLaser : spaceship.getNewLasers()) {
                            fb.put(BYTE_BUFFER_LASER);
                            fb
                                    .put(newLaser.getX())
                                    .put(newLaser.getY())
                                    .put(newLaser.getRotation())
                                    .put(newLaser.getMotionAngle());
                        }

                        // #9
                        //make buffer ready for reading
                        buffer.position(fb.position() * Float.BYTES);
                        buffer.flip();
                        client.send(buffer, address);

                        // #10
                        Thread.sleep(10);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (
                IOException ex) {
            ex.printStackTrace();
        }

    }

    public void connectNetty() {
        String host = "172.24.25.33";
        int port = 43;
        Spaceship spaceship = screen.addSpaceShip(400, 300);
        player = new PlayerEntity(spaceship.getX(), spaceship.getY(), spaceship.getRotation(), spaceship.getMotionAngle(), spaceship.getShieldPower());

        EventLoopGroup group = new NioEventLoopGroup();
        new Thread(() -> {
            try {
                Bootstrap b = new Bootstrap();
                InetSocketAddress serverAddress = new InetSocketAddress(host, port);

                b.group(group)
                        .channel(NioDatagramChannel.class)
                        .option(ChannelOption.SO_BROADCAST, true)
                        .handler(new ChannelInitializer<NioDatagramChannel>() {
                            @Override
                            public void initChannel(final NioDatagramChannel ch) {
                                ChannelPipeline p = ch.pipeline();
                                p.addLast(new PlayerEntityEncoder(serverAddress));
                                p.addLast(new OtherPlayersEntityDecoder(screen));
                            }
                        }).localAddress(0);

                io.netty.channel.Channel channel = b.bind().syncUninterruptibly().channel();
                System.out.println("Netty monitor listening on " + channel.localAddress());

                new Thread(() -> {
                    try {
                        while (true) {
                            player.setX(spaceship.getX());
                            player.setY(spaceship.getY());
                            player.setRotation(spaceship.getRotation());
                            player.setMotionAngle(spaceship.getMotionAngle());
                            player.setShieldPower(spaceship.getShieldPower());
                            player.setThrustersVisible(spaceship.areThrustersVisible());
                            player.setLasers(spaceship.getNewLasers());

                            channel.writeAndFlush(player);
                            Thread.sleep(10);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();

                channel.closeFuture().await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println("Client is shutting down...");
                group.shutdownGracefully();
            }
        }).start();
    }

    public void connect() {
        String host = "172.24.25.33";
        int port = 43;

        Spaceship spaceship = screen.addSpaceShip(400, 300);
        player = new PlayerEntity(spaceship.getX(), spaceship.getY(), spaceship.getRotation(), spaceship.getMotionAngle(), spaceship.getShieldPower());

        try {
            Socket socket = Gdx.net.newClientSocket(Net.Protocol.TCP, host, port, null);

            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                        while (true) {
                            player.setX(spaceship.getX());
                            player.setY(spaceship.getY());
                            player.setRotation(spaceship.getRotation());
                            player.setMotionAngle(spaceship.getMotionAngle());
                            player.setShieldPower(spaceship.getShieldPower());
                            player.setThrustersVisible(spaceship.areThrustersVisible());
                            player.setLasers(spaceship.getNewLasers());

                            oos.writeObject(player);
                            oos.flush();
                            oos.reset();

                            OtherPlayersEntity otherPlayers = (OtherPlayersEntity) ois.readObject();

                            measure();

                            enemiesCount = otherPlayers.getPlayers().size();

                            Gdx.app.postRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    for (PlayerEntity otherPlayer : otherPlayers.getPlayers()) {
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

                            Thread.sleep(10);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void measure() {
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
}
