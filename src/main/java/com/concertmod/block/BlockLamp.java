package com.concertmod.block;

import com.concertmod.tile.TileLamp;
import com.concertmod.tile.TileStrobe;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/** Lamp / strobe: a cube that glows (see TESRLamp) and emits light while LIT. */
public class BlockLamp extends BlockDevice {
    public static final PropertyBool LIT = PropertyBool.create("lit");

    private final boolean strobeType;

    public BlockLamp(boolean strobeType) {
        super(Material.GLASS);
        this.strobeType = strobeType;
        setSoundType(SoundType.GLASS);
        setHardness(1.0F);
        setDefaultState(blockState.getBaseState().withProperty(LIT, Boolean.FALSE));
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return strobeType ? new TileStrobe() : new TileLamp();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, LIT);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(LIT, (meta & 1) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(LIT) ? 1 : 0;
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.getValue(LIT) ? 15 : 0;
    }
}
