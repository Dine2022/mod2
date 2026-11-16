package com.concertmod.tile;

import com.concertmod.client.ClientEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Smoke / fog machine. Stream by default; the color tints the fog.
 * Types: 0 = thick fog (billows and rises), 1 = low fog (hugs the floor), 2 = haze (thin, lingers long).
 */
public class TileSmoke extends TileEffect {
    public static final int FOG_JET = 0;
    public static final int FOG_LOW = 1;
    public static final int FOG_HAZE = 2;

    public int fogType = FOG_JET;

    public TileSmoke() {
        this.burstMode = false;
    }

    @Override
    protected int burstDuration() {
        return 30;
    }

    @Override
    protected int shotInterval() {
        return Math.max(20, Math.round(40.0F / Math.max(0.1F, speed)));
    }

    @Override
    protected void emit(Vec3d origin, Vec3d dir, boolean burst) {
        int rgb = displayColor((float) (world.getTotalWorldTime() % 1000000L));
        if (!rainbow && (rgb & 0xFFFFFF) == 0xFFFFFF) {
            rgb = 0xF2F2F8;
        }
        ClientEffects.fog(world, origin, dir, burst, rgb, fogType);
    }

    @Override
    public void applySettings(NBTTagCompound t) {
        super.applySettings(t);
        if (t.hasKey("FogType")) {
            fogType = MathHelper.clamp(t.getInteger("FogType"), 0, 2);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("FogType", fogType);
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        fogType = MathHelper.clamp(tag.getInteger("FogType"), 0, 2);
    }

    @Override
    protected SoundEvent shotSound() {
        return SoundEvents.BLOCK_FIRE_EXTINGUISH;
    }

    @Override
    protected SoundEvent streamSound() {
        return null;
    }

    @Override
    public int barrelColor() {
        return 0x7A7F8A;
    }
}
