package handlers;

import entity.*;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.time.LocalTime;
import java.util.*;

@ChannelHandler.Sharable
public class ServerNettyHandler extends
        ChannelInboundHandlerAdapter {
    private static Map<String, Integer> connectionIdsToPlayerIds = new HashMap<>();
    private static Map<Integer, PlayerEntity> players = new HashMap<>();
    private static Map<Integer, Integer> playerIdToIncomingAudioSignals = new HashMap<>();
    private static List<MessageEntity> chatMessages = Collections.synchronizedList(new ArrayList<>());
    private static int playersCount = 0;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("Accepted new connection from: " + ctx.channel().remoteAddress());

        String id = ctx.channel().id().asShortText();
        connectionIdsToPlayerIds.put(id, ++playersCount);

        players.put(playersCount, null);
        playerIdToIncomingAudioSignals.put(playersCount, 0);

        LocalTime time = LocalTime.now().withNano(0);
        chatMessages.add(new MessageEntity("SERVER", "[" + time + "]: Player" + playersCount + " connected", new float[]{0, 1, 0}));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        String connectionId = ctx.channel().id().asShortText();
        int playerId = connectionIdsToPlayerIds.get(connectionId);

        if (msg instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) msg;
            player.setId(playerId);
            players.put(playerId, player);

            OtherPlayersEntity otherPlayersEntity = new OtherPlayersEntity();
            for (Integer clientId : players.keySet()) {
                PlayerEntity otherPlayer = players.get(clientId);
                if (otherPlayer != null && player != otherPlayer) {
                    otherPlayersEntity.getPlayers().add(otherPlayer);
                    int newSounds = playerIdToIncomingAudioSignals.get(otherPlayer.getId());
                    playerIdToIncomingAudioSignals.put(otherPlayer.getId(), newSounds + player.getNewAudioSignals());
                }
            }

            // if(otherPlayersEntity.getPlayers().size() > 0) {
            ctx.write(otherPlayersEntity);
            //  }
            int newSignals = playerIdToIncomingAudioSignals.get(playerId);
            if (newSignals > 0) {
                ctx.write(playerIdToIncomingAudioSignals.get(playerId));
                playerIdToIncomingAudioSignals.put(playerId, 0);
            }
        } else if (msg instanceof RawMessagesEntity) {
            if (chatMessages.size() >= 100) {
                chatMessages = chatMessages.subList(60, chatMessages.size() - 1);
            }

            RawMessagesEntity newMessages = (RawMessagesEntity) msg;

            for (String newMessage : newMessages.getRawMessages()) {
                LocalTime time = LocalTime.now().withNano(0);
                chatMessages.add(new MessageEntity("Player " + playerId, "[" + time + "]: " + newMessage, new float[]{1, 1, 1}));
            }
        }

        if (!chatMessages.isEmpty()) {
            ChatMessagesEntity messagesToSend = new ChatMessagesEntity();
            messagesToSend.getMessageEntities().addAll(chatMessages);

            ctx.write(messagesToSend);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        int playerId = connectionIdsToPlayerIds.get(ctx.channel().id().asShortText());

        players.put(playerId, null);
        playerIdToIncomingAudioSignals.remove(playerId);

        LocalTime time = LocalTime.now().withNano(0);
        chatMessages.add(new MessageEntity("SERVER", "[" + time + "]: Player" + playerId + " disconnected", new float[]{1, 0, 0}));

        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,
                                Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}


