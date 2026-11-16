package com.concertmod.tile;

import com.concertmod.block.BlockLamp;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ITickable;

/** Colored stage lamp. Server side toggles the LIT block state (light level + glow), strobe flickers it. */
public class TileLamp extends TileConcertDevice implements ITickable {

    @Override
    public void update() {
        if (world == null || world.isRemote) {
            return;
        }
        IBlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof BlockLamp)) {
            return;
        }
        boolean lit = isActive();
        if (lit && strobe) {
            long half = Math.max(1L, Math.round(3.0F / Math.max(0.1F, speed)));
            lit = (world.getTotalWorldTime() / half) % 2L == 0L;
        }
        if (state.getValue(BlockLamp.LIT) != lit) {
            world.setBlockState(pos, state.withProperty(BlockLamp.LIT, lit), 2);
        }
    }
}
