package com.concertmod.tile;

import com.concertmod.block.BlockRadioReceiver;
import com.concertmod.radio.IRadioListener;
import com.concertmod.radio.RadioNetwork;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;

/** Listens to a radio channel and emits a full redstone signal (15) when active. */
public class TileRadioReceiver extends TileRadioBase implements IRadioListener {
    public boolean active = false;

    @Override
    public boolean isTransmitter() {
        return false;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (world != null && !world.isRemote) {
            RadioNetwork.registerListener(world, pos, channel);
            world.scheduleUpdate(pos, getBlockType(), 1);
        }
    }

    @Override
    public void invalidate() {
        if (world != null && !world.isRemote) {
            RadioNetwork.unregisterListener(world, pos, channel);
        }
        super.invalidate();
    }

    @Override
    public void onChunkUnload() {
        if (world != null && !world.isRemote) {
            RadioNetwork.unregisterListener(world, pos, channel);
        }
        super.onChunkUnload();
    }

    @Override
    public void onRadioStateChanged(boolean newActive) {
        if (world == null || world.isRemote) {
            return;
        }
        applyActiveState(newActive);
    }

    public void refreshFromNetwork() {
        if (world == null || world.isRemote) {
            return;
        }
        applyActiveState(RadioNetwork.isChannelActive(world, channel));
    }

    private void applyActiveState(boolean newActive) {
        IBlockState state = world.getBlockState(pos);
        boolean stateLit = state.getBlock() instanceof BlockRadioReceiver && state.getValue(BlockRadioReceiver.LIT);
        if (active == newActive && stateLit == newActive) {
            return;
        }
        active = newActive;
        if (state.getBlock() instanceof BlockRadioReceiver) {
            world.setBlockState(pos, state.withProperty(BlockRadioReceiver.LIT, active), 3);
            world.notifyNeighborsOfStateChange(pos, state.getBlock(), false);
        }
        sync();
    }

    @Override
    public void setChannel(int newChannel) {
        int clamped = RadioNetwork.clampChannel(newChannel);
        if (clamped == channel) {
            return;
        }
        if (world != null && !world.isRemote) {
            RadioNetwork.unregisterListener(world, pos, channel);
        }
        channel = clamped;
        if (world != null && !world.isRemote) {
            RadioNetwork.registerListener(world, pos, channel);
            applyActiveState(RadioNetwork.isChannelActive(world, channel));
        }
        sync();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setBoolean("Active", active);
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        active = tag.getBoolean("Active");
    }
}
