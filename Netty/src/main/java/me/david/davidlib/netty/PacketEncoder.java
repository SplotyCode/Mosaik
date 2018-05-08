package me.david.davidlib.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;
import me.david.davidlib.netty.packets.SerializePacket;

@AllArgsConstructor
public class PacketEncoder extends MessageToByteEncoder<SerializePacket> {

    private PacketRegistry<SerializePacket> packetRegistry;

    @Override
    protected void encode(ChannelHandlerContext ctx, SerializePacket packet, ByteBuf output) throws Exception {
        int id = packetRegistry.getIdByPacket(packet);
        if(id == -1) {
            throw new NullPointerException("Coud not find id to packet: " + packet.getClass().getSimpleName());
        }
        PacketSerializer ps = new PacketSerializer(output);
        ps.writeVarInt(id);
        packet.write(ps);
    }

}
