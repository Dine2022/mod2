package com.concertmod.network;

import com.concertmod.tile.TileConcertDevice;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/** Client -> server: settings chosen in the control menu for the device at pos. */
public class MessageDeviceSettings implements IMessage {
    private BlockPos pos;
    private NBTTagCompound data;

    public MessageDeviceSettings() {
    }

    public MessageDeviceSettings(BlockPos pos, NBTTagCompound data) {
        this.pos = pos;
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos = BlockPos.fromLong(buf.readLong());
        this.data = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.pos.toLong());
        ByteBufUtils.writeTag(buf, this.data);
    }

    public static class Handler implements IMessageHandler<MessageDeviceSettings, IMessage> {
        @Override
        public IMessage onMessage(final MessageDeviceSettings msg, final MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    if (msg.pos == null || msg.data == null) {
                        return;
                    }
                    if (player.getDistanceSq(msg.pos) > 144.0D) { // must be within 12 blocks
                        return;
                    }
                    World world = player.world;
                    if (!world.isBlockLoaded(msg.pos)) {
                        return;
                    }
                    TileEntity te = world.getTileEntity(msg.pos);
                    if (te instanceof TileConcertDevice) {
                        TileConcertDevice dev = (TileConcertDevice) te;
                        dev.applySettings(msg.data);
                        dev.sync();
                    }
                }
            });
            return null;
        }
    }
}
