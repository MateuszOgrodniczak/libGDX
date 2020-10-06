package netty;

import entity.LaserEntity;
import entity.OtherPlayersEntity;
import entity.PlayerEntity;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlayerEntityDecoder extends MessageToMessageDecoder<DatagramPacket> {
    private int clientId = 1;
    private final Map<SocketAddress, PlayerEntity> addressToPlayer;
    private SocketAddress privateNetworkIP = new InetSocketAddress("192.168.56.1", 43);


    public PlayerEntityDecoder(Map<SocketAddress, PlayerEntity> addressToPlayer) {
        this.addressToPlayer = addressToPlayer;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket, List<Object> list) throws Exception {
        InetSocketAddress clientAddress = datagramPacket.sender();
        if (clientAddress.equals(privateNetworkIP)) {
            return;
        }
        PlayerEntity player = addressToPlayer.get(clientAddress);
        if (player == null) {
            player = new PlayerEntity();
            player.setId(clientId++);
        }

        ByteBuf buf = datagramPacket.content();
        float x = buf.getFloat(0);
        float y = buf.getFloat(4);
        float rotation = buf.getFloat(8);
        float motionAngle = buf.getFloat(12);
        float shieldPower = buf.getFloat(16);
        boolean thrustersVisible = buf.getFloat(20) == 1;

        player.setX(x);
        player.setY(y);
        player.setRotation(rotation);
        player.setMotionAngle(motionAngle);
        player.setShieldPower((int) shieldPower);
        player.setThrustersVisible(thrustersVisible);

        List<LaserEntity> lasers = new ArrayList<>();
        for (int i = 24; i < buf.readableBytes(); i += 20) {
            float bufferLaserEntityFlag = buf.getFloat(i);
            if (bufferLaserEntityFlag != -1) {
                break;
            }
            float laserX = buf.getFloat(i + 4);
            float laserY = buf.getFloat(i + 8);
            float laserRot = buf.getFloat(i + 12);
            float laserMotAngle = buf.getFloat(i + 16);
            lasers.add(new LaserEntity(laserX, laserY, laserRot, laserMotAngle));
        }
        player.setLasers(lasers);
        addressToPlayer.put(clientAddress, player);

        OtherPlayersEntity otherPlayers = new OtherPlayersEntity();
        for (SocketAddress address : addressToPlayer.keySet()) {
            if (address.equals(clientAddress)) {
                continue;
            }
            PlayerEntity playerEntity = addressToPlayer.get(address);
            otherPlayers.getPlayers().add(playerEntity);
        }
        ByteBuf writeBuf = channelHandlerContext.alloc().buffer();

        for (PlayerEntity otherPlayer : otherPlayers.getPlayers()) {
            writeBuf.writeFloat(otherPlayer.getId());
            writeBuf.writeFloat(otherPlayer.getX());
            writeBuf.writeFloat(otherPlayer.getY());
            writeBuf.writeFloat(otherPlayer.getRotation());
            writeBuf.writeFloat(otherPlayer.getMotionAngle());
            writeBuf.writeFloat(otherPlayer.getShieldPower());
            writeBuf.writeFloat(otherPlayer.isThrustersVisible() ? 1 : 0);

            for (LaserEntity laser : otherPlayer.getLasers()) {
                writeBuf.writeFloat(-1.0f);
                writeBuf.writeFloat(laser.getX());
                writeBuf.writeFloat(laser.getY());
                writeBuf.writeFloat(laser.getRotation());
                writeBuf.writeFloat(laser.getMotionAngle());
            }
        }
        channelHandlerContext.channel().writeAndFlush(new DatagramPacket(writeBuf, clientAddress));
    }
}
