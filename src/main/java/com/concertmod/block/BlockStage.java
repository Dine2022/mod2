package com.concertmod.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

/** Stage floor block. */
public class BlockStage extends Block {
    public BlockStage() {
        super(Material.WOOD);
        setHardness(1.5F);
        setResistance(10F);
        setSoundType(SoundType.WOOD);
    }
}
