package handlers;

import actor.SpaceshipEnemy;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import entity.ChatMessagesEntity;
import entity.MessageEntity;
import entity.OtherPlayersEntity;
import entity.PlayerEntity;
import global.GlobalConfig;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import screen.LevelScreen;
import test.ResponseTimeTest;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ChannelHandler.Sharable
public class ClientNettyHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private LevelScreen screen;

    private int enemiesCount;
    private long serverResponseTime = 0;
    private double totalResponseTime = 0;
    private long iterationsCount = 0;

    public ClientNettyHandler(LevelScreen screen) {
        this.screen = screen;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("Channel active");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf in) {
        System.out.println("Client received: " + in.toString(CharsetUtil.UTF_8) + " [" + LocalTime.now().withNano(0) + "]");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof OtherPlayersEntity) {
            measureTime();
            OtherPlayersEntity otherPlayers = (OtherPlayersEntity) msg;

            enemiesCount = otherPlayers.getPlayers().size();

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
            });
        } else if (msg instanceof Integer) {
            if (!GlobalConfig.isMuted) {
                int newSignals = (Integer) msg;
                for (int i = 0; i < newSignals; i++) {
                    Sound laserSound = Gdx.audio.newSound(Gdx.files.internal("assets/laser.mp3"));
                    laserSound.play(1.0f);
                }
            }
        } else if (msg instanceof ChatMessagesEntity) {

            ChatMessagesEntity chatMessages = (ChatMessagesEntity) msg;
            List<MessageEntity> chatMessageEntities = chatMessages.getMessageEntities();

            if (screen.getChatMessages().size() != chatMessageEntities.size()) {
                screen.getChatMessages().clear();
                screen.getChatMessages().addAll(chatMessageEntities);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,
                                Throwable cause) {
        cause.printStackTrace();
        ctx.close();
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
}


