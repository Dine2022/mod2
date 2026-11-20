package com.concertmod.network;

import com.concertmod.radio.RadioNetwork;
import com.concertmod.tile.TileRadioBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/** Client -> server: updates the radio channel of a transmitter or receiver block. */
public class MessageRadioChannel implements IMessage {
    private BlockPos pos;
    private int channel;

    public MessageRadioChannel() {
    }

    public MessageRadioChannel(BlockPos pos, int channel) {
        this.pos = pos;
        this.channel = channel;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos = BlockPos.fromLong(buf.readLong());
        this.channel = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.pos.toLong());
        buf.writeInt(this.channel);
    }

    public static class Handler implements IMessageHandler<MessageRadioChannel, IMessage> {
        @Override
        public IMessage onMessage(final MessageRadioChannel msg, final MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    if (msg.pos == null) {
                        return;
                    }
                    if (player.getDistanceSq(msg.pos) > 144.0D) {
                        return;
                    }
                    World world = player.world;
                    if (!world.isBlockLoaded(msg.pos)) {
                        return;
                    }
                    TileEntity te = world.getTileEntity(msg.pos);
                    if (te instanceof TileRadioBase) {
                        ((TileRadioBase) te).setChannel(RadioNetwork.clampChannel(msg.channel));
                    }
                }
            });
            return null;
        }
    }
}
