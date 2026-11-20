package com.concertmod.block;

import com.concertmod.ConcertMod;
import com.concertmod.item.ItemRadioRemote;
import com.concertmod.tile.TileRadioTransmitter;
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

/** Wireless redstone transmitter block: sends its redstone power state over a radio channel. */
public class BlockRadioTransmitter extends Block {
    public static final PropertyBool LIT = PropertyBool.create("lit");

    public BlockRadioTransmitter() {
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
        return new TileRadioTransmitter();
    }

    @Override
    public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EnumFacing side) {
        return true;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        updatePower(world, pos);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
        updatePower(world, pos);
    }

    private void updatePower(World world, BlockPos pos) {
        if (world.isRemote) {
            return;
        }
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileRadioTransmitter) {
            ((TileRadioTransmitter) te).setPowered(world.isBlockPowered(pos));
        }
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileRadioTransmitter && !world.isRemote) {
            ((TileRadioTransmitter) te).setPowered(false);
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (hand != EnumHand.MAIN_HAND) {
            return true;
        }
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileRadioTransmitter)) {
            return false;
        }
        TileRadioTransmitter tx = (TileRadioTransmitter) te;
        ItemStack held = player.getHeldItem(hand);
        if (held.getItem() instanceof ItemRadioRemote && player.isSneaking()) {
            if (!world.isRemote) {
                int ch = ItemRadioRemote.getChannel(held);
                tx.setChannel(ch);
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
