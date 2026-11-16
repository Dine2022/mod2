package com.concertmod.tile;

import com.concertmod.client.ClientEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;

/** Confetti cannon. White color (default) = multicolored confetti. */
public class TileConfetti extends TileEffect {

    public TileConfetti() {
        this.burstMode = true;
    }

    @Override
    protected int burstDuration() {
        return 1;
    }

    @Override
    protected void emit(Vec3d origin, Vec3d dir, boolean burst) {
        boolean multi = rainbow || (color & 0xFFFFFF) == 0xFFFFFF;
        ClientEffects.confetti(world, origin, dir, burst, color, multi);
    }

    @Override
    protected SoundEvent shotSound() {
        return SoundEvents.ENTITY_FIREWORK_BLAST;
    }

    @Override
    protected SoundEvent streamSound() {
        return null;
    }

    @Override
    public int barrelColor() {
        return 0xE0508C;
    }
}
