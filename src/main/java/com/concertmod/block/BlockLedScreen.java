package com.concertmod.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

/** LED screen tile: animated texture (all tiles stay in sync) and emits light. */
public class BlockLedScreen extends Block {
    public BlockLedScreen() {
        super(Material.GLASS);
        setHardness(1.0F);
        setSoundType(SoundType.GLASS);
        setLightLevel(0.9F);
    }
}
