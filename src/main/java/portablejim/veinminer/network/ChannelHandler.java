package portablejim.veinminer.network;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.FMLIndexedMessageToMessageCodec;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.common.network.FMLOutboundHandler.OutboundTarget;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.NetHandlerPlayServer;
import portablejim.veinminer.lib.ModInfo;
import portablejim.veinminer.network.packet.IPacket;
import portablejim.veinminer.network.packet.PacketClientPresent;
import portablejim.veinminer.network.packet.PacketMinerActivate;

import java.util.EnumMap;

/**
 * Created with IntelliJ IDEA.
 * User: james
 * Date: 21/02/14
 * Time: 9:21 AM
 * To change this template use File | Settings | File Templates.
 */
public class ChannelHandler extends FMLIndexedMessageToMessageCodec<IPacket> {
    private EnumMap<Side, FMLEmbeddedChannel> channels;
    public ChannelHandler() {
        channels = NetworkRegistry.INSTANCE.newChannel(ModInfo.CHANNEL, this);

        addDiscriminator(0, PacketClientPresent.class);
        addDiscriminator(1, PacketMinerActivate.class);
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, IPacket msg, ByteBuf target) throws Exception {
        msg.writeBytes(target);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf source, IPacket msg) {
        msg.readBytes(source);
        if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
            NetHandlerPlayServer handler = (NetHandlerPlayServer) ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
            msg.handleServerSide(handler.playerEntity);
        }
    }

    public void sendToServer(IPacket packet) {
        channels.get(Side.CLIENT).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(OutboundTarget.TOSERVER);
        channels.get(Side.CLIENT).writeOutbound(packet);
    }
}