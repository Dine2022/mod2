package com.concertmod.tile;

import com.concertmod.radio.IRadioListener;
import com.concertmod.radio.RadioNetwork;
import com.concertmod.util.ConcertColors;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;

/** Common state of every controllable stage device (spotlight, lamp, strobe, effect machines). */
public abstract class TileConcertDevice extends TileEntity implements IRadioListener {
    public int color = 0xFFFFFF;
    /** Turned on with a command or by shift+click. */
    public boolean commandOn = false;
    /** Powered by redstone. */
    public boolean powered = false;
    /** Bound radio channel (0 = unbound, 1..64 = active channel). */
    public int radioChannel = 0;
    /** Powered wirelessly by the bound radio channel. */
    public boolean radioPowered = false;
    public boolean rainbow = false;
    public boolean strobe = false;
    public float speed = 1.0F;

    public boolean isActive() {
        return powered || commandOn || radioPowered;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (world != null && !world.isRemote && radioChannel >= RadioNetwork.MIN_CHANNEL) {
            RadioNetwork.registerListener(world, pos, radioChannel);
            radioPowered = RadioNetwork.isChannelActive(world, radioChannel);
        }
    }

    @Override
    public void invalidate() {
        if (world != null && !world.isRemote && radioChannel >= RadioNetwork.MIN_CHANNEL) {
            RadioNetwork.unregisterListener(world, pos, radioChannel);
        }
        super.invalidate();
    }

    @Override
    public void onChunkUnload() {
        if (world != null && !world.isRemote && radioChannel >= RadioNetwork.MIN_CHANNEL) {
            RadioNetwork.unregisterListener(world, pos, radioChannel);
        }
        super.onChunkUnload();
    }

    @Override
    public void onRadioStateChanged(boolean active) {
        if (world == null || world.isRemote) {
            return;
        }
        if (radioPowered != active) {
            radioPowered = active;
            sync();
        }
    }

    public void setRadioChannel(int newChannel) {
        int normalized = (newChannel <= 0) ? 0 : RadioNetwork.clampChannel(newChannel);
        if (world != null && !world.isRemote && radioChannel >= RadioNetwork.MIN_CHANNEL) {
            RadioNetwork.unregisterListener(world, pos, radioChannel);
        }
        radioChannel = normalized;
        if (world != null && !world.isRemote) {
            if (radioChannel >= RadioNetwork.MIN_CHANNEL) {
                RadioNetwork.registerListener(world, pos, radioChannel);
                radioPowered = RadioNetwork.isChannelActive(world, radioChannel);
            } else {
                radioPowered = false;
            }
        }
        sync();
    }

    /** Color to draw at the given time (in ticks, with partial ticks). */
    public int displayColor(float time) {
        if (rainbow) {
            float hue = (time * speed * 0.01F) % 1.0F;
            if (hue < 0F) {
                hue += 1.0F;
            }
            return MathHelper.hsvToRGB(hue, 1.0F, 1.0F);
        }
        return color;
    }

    public void cycleColor() {
        color = ConcertColors.next(color);
        rainbow = false;
        sync();
    }

    /** Saves the tile and sends the new state to clients. */
    public void sync() {
        markDirty();
        if (world != null) {
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    /** Special handling of right click with an item in hand. Return true if handled. */
    public boolean onInteract(EntityPlayer player, ItemStack held) {
        return false;
    }

    /** True if right-clicking with this item does something special (instead of opening the control menu). */
    public boolean isInteractItem(ItemStack held) {
        return false;
    }

    /** Applies settings sent by the control menu (server side). */
    public void applySettings(NBTTagCompound t) {
        if (t.hasKey("Color")) {
            color = t.getInteger("Color") & 0xFFFFFF;
        }
        if (t.hasKey("CommandOn")) {
            commandOn = t.getBoolean("CommandOn");
        }
        if (t.hasKey("Rainbow")) {
            rainbow = t.getBoolean("Rainbow");
        }
        if (t.hasKey("Strobe")) {
            strobe = t.getBoolean("Strobe");
        }
        if (t.hasKey("Speed")) {
            speed = MathHelper.clamp(t.getFloat("Speed"), 0.1F, 10.0F);
        }
        if (t.hasKey("RadioChannel")) {
            setRadioChannel(t.getInteger("RadioChannel"));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("Color", color);
        tag.setBoolean("CommandOn", commandOn);
        tag.setBoolean("Powered", powered);
        tag.setInteger("RadioChannel", radioChannel);
        tag.setBoolean("RadioPowered", radioPowered);
        tag.setBoolean("Rainbow", rainbow);
        tag.setBoolean("Strobe", strobe);
        tag.setFloat("Speed", speed);
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        if (tag.hasKey("Color")) {
            color = tag.getInteger("Color");
        }
        commandOn = tag.getBoolean("CommandOn");
        powered = tag.getBoolean("Powered");
        if (tag.hasKey("RadioChannel")) {
            int ch = tag.getInteger("RadioChannel");
            radioChannel = (ch <= 0) ? 0 : RadioNetwork.clampChannel(ch);
        }
        radioPowered = tag.getBoolean("RadioPowered");
        rainbow = tag.getBoolean("Rainbow");
        if (tag.hasKey("Strobe")) {
            strobe = tag.getBoolean("Strobe");
        }
        if (tag.hasKey("Speed")) {
            speed = tag.getFloat("Speed");
        }
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }
}
