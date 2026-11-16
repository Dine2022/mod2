package com.concertmod.block;

import com.concertmod.tile.TileAimable;
import com.concertmod.tile.TileConfetti;
import com.concertmod.tile.TileFlame;
import com.concertmod.tile.TileSmoke;
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
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * Confetti cannon / smoke machine / flame machine. The barrel is drawn by the TESR and can be aimed.
 * Placed on the floor it points up; placed on the underside of a block (ceiling) it points down.
 */
public class BlockEffectMachine extends BlockDevice {
    public static final int CONFETTI = 0;
    public static final int SMOKE = 1;
    public static final int FLAME = 2;

    private static final AxisAlignedBB BOX = new AxisAlignedBB(0.15D, 0.0D, 0.15D, 0.85D, 0.75D, 0.85D);
    private static final AxisAlignedBB BOX_HANGING = new AxisAlignedBB(0.15D, 0.25D, 0.15D, 0.85D, 1.0D, 0.85D);

    private final int kind;

    public BlockEffectMachine(int kind) {
        super(Material.IRON);
        this.kind = kind;
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
        switch (kind) {
            case SMOKE:
                return new TileSmoke();
            case FLAME:
                return new TileFlame();
            default:
                return new TileConfetti();
        }
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileAimable) {
            ((TileAimable) te).pitch = state.getValue(HANGING) ? 90.0F : -90.0F;
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
