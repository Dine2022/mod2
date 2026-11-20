package com.concertmod.block;

import com.concertmod.ConcertMod;
import com.concertmod.item.ItemRadioRemote;
import com.concertmod.tile.TileRadioReceiver;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;

/** Wireless redstone receiver block: emits full redstone power (15) when its radio channel is active. */
public class BlockRadioReceiver extends Block {
    public static final PropertyBool LIT = PropertyBool.create("lit");

    public BlockRadioReceiver() {
        super(Material.IRON);
        setHardness(1.5F);
        setResistance(10F);
        setSoundType(SoundType.METAL);
        setDefaultState(blockState.getBaseState().withProperty(LIT, Boolean.FALSE));
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
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileRadioReceiver();
    }

    @Override
    public boolean canProvidePower(IBlockState state) {
        return true;
    }

    @Override
    public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EnumFacing side) {
        return true;
    }

    @Override
    public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return blockState.getValue(LIT) ? 15 : 0;
    }

    @Override
    public int getStrongPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return blockState.getValue(LIT) ? 15 : 0;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileRadioReceiver) {
                ((TileRadioReceiver) te).refreshFromNetwork();
            }
        }
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileRadioReceiver) {
                ((TileRadioReceiver) te).refreshFromNetwork();
            }
        }
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        boolean wasLit = state.getValue(LIT);
        super.breakBlock(world, pos, state);
        if (wasLit) {
            world.notifyNeighborsOfStateChange(pos, this, false);
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (hand != EnumHand.MAIN_HAND) {
            return true;
        }
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileRadioReceiver)) {
            return false;
        }
        TileRadioReceiver rx = (TileRadioReceiver) te;
        ItemStack held = player.getHeldItem(hand);
        if (held.getItem() instanceof ItemRadioRemote && player.isSneaking()) {
            if (!world.isRemote) {
                int ch = ItemRadioRemote.getChannel(held);
                rx.setChannel(ch);
                player.sendStatusMessage(new TextComponentTranslation("msg.concertmod.radio_bound", String.valueOf(ch)), true);
            }
            return true;
        }
        if (held.isEmpty()) {
            if (world.isRemote) {
                player.openGui(ConcertMod.instance, ConcertMod.GUI_RADIO, world, pos.getX(), pos.getY(), pos.getZ());
            }
            return true;
        }
        return false;
    }
}
