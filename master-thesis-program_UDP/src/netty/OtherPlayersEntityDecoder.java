package netty;

import actor.SpaceshipEnemy;
import com.badlogic.gdx.Gdx;
import entity.LaserEntity;
import entity.OtherPlayersEntity;
import entity.PlayerEntity;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import screen.LevelScreen;
import test.ResponseTimeTest;

import java.util.ArrayList;
import java.util.List;

public class OtherPlayersEntityDecoder extends MessageToMessageDecoder<DatagramPacket> {
    private final LevelScreen screen;
    private long serverResponseTime = 0;
    private double totalResponseTime = 0;
    private long iterationsCount = 0;
    private int enemiesCount = 0;

    public OtherPlayersEntityDecoder(LevelScreen screen) {
        this.screen = screen;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket, List<Object> out) throws Exception {
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
        ByteBuf buf = datagramPacket.content();

        OtherPlayersEntity otherPlayers = new OtherPlayersEntity();
        for (int i = 0; i < buf.readableBytes(); ) {
            float id = buf.getFloat(i);
            float x = buf.getFloat(i + 4);
            float y = buf.getFloat(i + 8);
            float rotation = buf.getFloat(i + 12);
            float motionAngle = buf.getFloat(i + 16);
            float shieldPower = buf.getFloat(i + 20);
            boolean thrustersVisible = buf.getFloat(i + 24) == 1;

            PlayerEntity enemy = new PlayerEntity(x, y, rotation, motionAngle, (int) shieldPower);
            enemy.setThrustersVisible(thrustersVisible);
            enemy.setId((int) id);

            i += 28;

            List<LaserEntity> lasers = new ArrayList<>();
            while (i < buf.readableBytes() && buf.getFloat(i) == -1) {
                float laserX = buf.getFloat(i + 4);
                float laserY = buf.getFloat(i + 8);
                float laserRot = buf.getFloat(i + 12);
                float laserMotAngle = buf.getFloat(i + 16);
                lasers.add(new LaserEntity(laserX, laserY, laserRot, laserMotAngle));

                i += 20;
            }
            enemy.setLasers(lasers);
            otherPlayers.getPlayers().add(enemy);
        }

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

    }
}
