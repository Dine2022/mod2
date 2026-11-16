package com.concertmod.block;

import com.concertmod.tile.TileSpotlight;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/** Spotlight. Place it on the underside of a block to hang it from the ceiling. */
public class BlockSpotlight extends BlockDevice {
    private static final AxisAlignedBB BOX = new AxisAlignedBB(0.2D, 0.0D, 0.2D, 0.8D, 0.8D, 0.8D);
    private static final AxisAlignedBB BOX_HANGING = new AxisAlignedBB(0.2D, 0.2D, 0.2D, 0.8D, 1.0D, 0.8D);

    public BlockSpotlight() {
        super(Material.IRON);
        setDefaultState(blockState.getBaseState().withProperty(HANGING, Boolean.FALSE));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, HANGING);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(HANGING, (meta & 1) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(HANGING) ? 1 : 0;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ,
                                            int meta, EntityLivingBase placer, EnumHand hand) {
        return getDefaultState().withProperty(HANGING, facing == EnumFacing.DOWN);
    }

    @Override
    public boolean isHanging(IBlockState state) {
        return state.getValue(HANGING);
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileSpotlight();
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileSpotlight && placer != null) {
            ((TileSpotlight) te).yaw = MathHelper.wrapDegrees(placer.rotationYaw);
        }
        super.onBlockPlacedBy(world, pos, state, placer, stack);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return state.getValue(HANGING) ? BOX_HANGING : BOX;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }
}
