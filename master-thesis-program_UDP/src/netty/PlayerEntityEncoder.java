package netty;

import entity.LaserEntity;
import entity.PlayerEntity;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.net.InetSocketAddress;
import java.util.List;

public class PlayerEntityEncoder extends MessageToMessageEncoder<PlayerEntity> {
    private static final float BYTE_BUFFER_LASER = -1.0f;

    private final InetSocketAddress address;

    public PlayerEntityEncoder(InetSocketAddress address) {
        this.address = address;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, PlayerEntity playerEntity, List<Object> out) throws Exception {

        ByteBuf buf = channelHandlerContext.alloc().buffer();
        buf.writeFloat(playerEntity.getX());
        buf.writeFloat(playerEntity.getY());
        buf.writeFloat(playerEntity.getRotation());
        buf.writeFloat(playerEntity.getMotionAngle());
        buf.writeFloat(playerEntity.getShieldPower());
        buf.writeFloat(playerEntity.isThrustersVisible() ? 1 : 0);

        for (LaserEntity laserEntity : playerEntity.getLasers()) {
            buf.writeFloat(BYTE_BUFFER_LASER);
            buf.writeFloat(laserEntity.getX());
            buf.writeFloat(laserEntity.getY());
            buf.writeFloat(laserEntity.getRotation());
            buf.writeFloat(laserEntity.getMotionAngle());
        }
        out.add(new DatagramPacket(buf, address));
    }
}
