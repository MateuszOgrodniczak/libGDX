package netty;

import entity.PlayerEntity;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

public class NettyBroadcaster {
    private final int port;
    private final Map<SocketAddress, PlayerEntity> addressToPlayer = new HashMap<>();//Collections.synchronizedMap(new HashMap<>());

    public NettyBroadcaster(int port) {
        this.port = port;
    }

    private void start() {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            PlayerEntityDecoder decoder = new PlayerEntityDecoder(addressToPlayer);

            b.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new ChannelInitializer<NioDatagramChannel>() {
                        @Override
                        public void initChannel(final NioDatagramChannel ch) {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(decoder);
                        }
                    });

            Channel channel = b.bind(43).syncUninterruptibly().channel();
            System.out.println(NettyBroadcaster.class.getName() +
                    " started and listen on " + channel.localAddress());

            while (true) {
            }

        } finally {
            System.out.println("server.Server is shutting down...");
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        new NettyBroadcaster(43).start();
    }
}
