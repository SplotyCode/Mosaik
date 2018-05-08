package me.david.davidlib.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.AllArgsConstructor;
import me.david.davidlib.netty.packets.SerializePacket;

import java.util.List;

@AllArgsConstructor
public class PacketDecoder extends ByteToMessageDecoder {

    private PacketRegistry<SerializePacket> packetRegistry;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf bytebuf, List<Object> output) throws Exception {
        PacketSerializer ps = new PacketSerializer(bytebuf);
        int id = ps.readVarInt();
        SerializePacket packet = packetRegistry.createPacket(id);
        if(packet == null) {
            throw new NullPointerException("Cloud not find that Packet");
        }
        packet.read(ps);
        output.add(packet);
    }
}
