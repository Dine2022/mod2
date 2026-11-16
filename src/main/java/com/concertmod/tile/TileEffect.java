package com.concertmod.tile;

import com.concertmod.block.BlockDevice;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;

/**
 * Base of the effect machines (confetti, smoke, flame).
 *
 * Two modes:
 *  - burst mode ("shots"): one shot when switched on, then a new shot every shotInterval() ticks while it stays on;
 *  - stream mode: the effect runs continuously while the machine is on.
 *
 * The server only counts shots (and syncs them); all particles are spawned on the client.
 */
public abstract class TileEffect extends TileAimable implements ITickable {
    public boolean burstMode = true;
    /** Incremented by the server on every shot, the client spawns a burst when it changes. */
    public int shots = 0;

    private int cooldown = 0;       // server
    private int pendingBursts = 0;  // client
    private int burstTicks = 0;     // client

    protected TileEffect() {
        this.pitch = -90F; // straight up by default
    }

    /** How many ticks one shot keeps emitting on the client. */
    protected abstract int burstDuration();

    /** Ticks between two shots while the machine stays on in burst mode. */
    protected int shotInterval() {
        return Math.max(4, Math.round(20.0F / Math.max(0.1F, speed)));
    }

    /** Spawn particles for one client tick. */
    protected abstract void emit(Vec3d origin, Vec3d dir, boolean burst);

    @Nullable
    protected abstract SoundEvent shotSound();

    @Nullable
    protected abstract SoundEvent streamSound();

    /** Color of the barrel drawn by the renderer. */
    public abstract int barrelColor();

    @Override
    public void update() {
        if (world == null) {
            return;
        }
        if (world.isRemote) {
            clientTick();
        } else {
            serverTick();
        }
    }

    private void serverTick() {
        if (cooldown > 0) {
            cooldown--;
        }
        if (!isActive()) {
            return;
        }
        if (burstMode) {
            if (cooldown <= 0) {
                fireShot();
                cooldown = shotInterval();
            }
        } else if (world.getTotalWorldTime() % 12L == 0L) {
            SoundEvent s = streamSound();
            if (s != null) {
                world.playSound(null, pos, s, SoundCategory.BLOCKS, 0.6F, 1.0F);
            }
        }
    }

    /** Server side: fire one shot right now (used by the tick, the menu and /concert fire). */
    public void fireShot() {
        if (world == null || world.isRemote) {
            return;
        }
        shots++;
        sync();
        SoundEvent s = shotSound();
        if (s != null) {
            world.playSound(null, pos, s, SoundCategory.BLOCKS, 0.9F, 1.0F);
        }
    }

    private boolean isHangingMounted() {
        IBlockState s = world.getBlockState(pos);
        return s.getBlock() instanceof BlockDevice && ((BlockDevice) s.getBlock()).isHanging(s);
    }

    private void clientTick() {
        if (pendingBursts > 0) {
            pendingBursts--;
            burstTicks = Math.max(burstTicks, burstDuration());
        }
        boolean stream = !burstMode && isActive();
        if (!stream && burstTicks <= 0) {
            return;
        }
        Vec3d dir = direction();
        double baseY = pos.getY() + (isHangingMounted() ? 0.6D : 0.4D);
        Vec3d origin = new Vec3d(pos.getX() + 0.5D + dir.x * 0.55D, baseY + dir.y * 0.55D, pos.getZ() + 0.5D + dir.z * 0.55D);
        emit(origin, dir, burstTicks > 0);
        if (burstTicks > 0) {
            burstTicks--;
        }
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        int before = shots;
        super.onDataPacket(net, pkt);
        if (shots != before) {
            pendingBursts = Math.min(pendingBursts + 1, 3);
        }
    }

    @Override
    public boolean isInteractItem(ItemStack held) {
        return held.getItem() == Items.GUNPOWDER || super.isInteractItem(held);
    }

    @Override
    public boolean onInteract(EntityPlayer player, ItemStack held) {
        if (held.getItem() == Items.GUNPOWDER) {
            burstMode = !burstMode;
            sync();
            player.sendStatusMessage(new TextComponentTranslation("msg.concertmod.machine_mode",
                    new TextComponentTranslation(burstMode ? "mode.concertmod.shots" : "mode.concertmod.stream")), true);
            return true;
        }
        return super.onInteract(player, held);
    }

    @Override
    public void applySettings(NBTTagCompound t) {
        super.applySettings(t);
        if (t.hasKey("BurstMode")) {
            burstMode = t.getBoolean("BurstMode");
        }
        if (t.getBoolean("Fire")) {
            fireShot();
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setBoolean("BurstMode", burstMode);
        tag.setInteger("Shots", shots);
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        if (tag.hasKey("BurstMode")) {
            burstMode = tag.getBoolean("BurstMode");
        }
        shots = tag.getInteger("Shots");
    }
}
