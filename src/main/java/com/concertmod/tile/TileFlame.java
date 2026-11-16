package com.concertmod.tile;

import com.concertmod.client.ClientEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;

/**
 * Flame machine. Real stage flame projectors fire short bursts (0.5-3 s), so by default it shoots a ~1.3 s jet
 * every few seconds while on. Visual only: it does not hurt entities and does not set blocks on fire.
 */
public class TileFlame extends TileEffect {

    public TileFlame() {
        this.burstMode = true;
    }

    @Override
    protected int burstDuration() {
        return 26;
    }

    @Override
    protected int shotInterval() {
        return Math.max(40, Math.round(60.0F / Math.max(0.1F, speed)));
    }

    @Override
    protected void emit(Vec3d origin, Vec3d dir, boolean burst) {
        ClientEffects.flames(world, origin, dir, burst);
    }

    @Override
    protected SoundEvent shotSound() {
        return SoundEvents.ITEM_FIRECHARGE_USE;
    }

    @Override
    protected SoundEvent streamSound() {
        return SoundEvents.BLOCK_FIRE_AMBIENT;
    }

    @Override
    public int barrelColor() {
        return 0xC03020;
    }
}
