package com.ricardo.jugador2.npc;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.ReferenceCountUtil;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;

import java.lang.reflect.Field;

/**
 * Conexión sin red: el NPC no tiene cliente, así que no envía nada a ningún lado.
 * Algunas rutas de envío de Minecraft escriben directamente en el canal de Netty, por eso el canal
 * se sustituye por uno que descarta todo lo que recibe.
 */
public final class FakeConnection extends Connection {
    public FakeConnection() {
        super(PacketFlow.SERVERBOUND);
        attachDiscardingChannel();
    }

    @Override
    public void send(Packet<?> packet) {
    }

    @Override
    public void send(Packet<?> packet, PacketSendListener listener) {
    }

    private void attachDiscardingChannel() {
        try {
            Field channel = Connection.class.getDeclaredField("channel");
            channel.setAccessible(true);
            channel.set(this, new EmbeddedChannel(new DiscardOutbound()));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("No se pudo preparar el canal falso del NPC", e);
        }
    }

    private static final class DiscardOutbound extends ChannelOutboundHandlerAdapter {
        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
            ReferenceCountUtil.release(msg);
            promise.setSuccess();
        }
    }
}
