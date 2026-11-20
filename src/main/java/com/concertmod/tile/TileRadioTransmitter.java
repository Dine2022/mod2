package com.concertmod.tile;

import com.concertmod.block.BlockRadioTransmitter;
import com.concertmod.radio.RadioNetwork;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;

/** Broadcasts its redstone power state on the configured radio channel. */
public class TileRadioTransmitter extends TileRadioBase {
    public boolean powered = false;

    @Override
    public boolean isTransmitter() {
        return true;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (world != null && !world.isRemote && powered) {
            RadioNetwork.setTransmitterState(world, pos, channel, true);
        }
    }

    @Override
    public void invalidate() {
        if (world != null && !world.isRemote) {
            RadioNetwork.setTransmitterState(world, pos, channel, false);
        }
        super.invalidate();
    }

    @Override
    public void onChunkUnload() {
        if (world != null && !world.isRemote) {
            RadioNetwork.setTransmitterState(world, pos, channel, false);
        }
        super.onChunkUnload();
    }

    public void setPowered(boolean newPowered) {
        if (world == null || world.isRemote) {
            return;
        }
        if (powered != newPowered) {
            powered = newPowered;
            RadioNetwork.setTransmitterState(world, pos, channel, powered);
            IBlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof BlockRadioTransmitter && state.getValue(BlockRadioTransmitter.LIT) != powered) {
                world.setBlockState(pos, state.withProperty(BlockRadioTransmitter.LIT, powered), 3);
            }
            sync();
        } else if (powered) {
            RadioNetwork.setTransmitterState(world, pos, channel, true);
        }
    }

    @Override
    public void setChannel(int newChannel) {
        int clamped = RadioNetwork.clampChannel(newChannel);
        if (clamped == channel) {
            return;
        }
        if (world != null && !world.isRemote && powered) {
            RadioNetwork.setTransmitterState(world, pos, channel, false);
        }
        channel = clamped;
        if (world != null && !world.isRemote && powered) {
            RadioNetwork.setTransmitterState(world, pos, channel, true);
        }
        sync();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setBoolean("Powered", powered);
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        powered = tag.getBoolean("Powered");
    }
}
