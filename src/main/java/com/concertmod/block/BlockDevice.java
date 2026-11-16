package com.concertmod.block;

import com.concertmod.ConcertMod;
import com.concertmod.tile.TileConcertDevice;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

/** Base class for blocks with a controllable device tile entity (redstone + control menu). */
public abstract class BlockDevice extends Block {
    /** True when the device is mounted on a ceiling (placed on the underside of a block). */
    public static final PropertyBool HANGING = PropertyBool.create("hanging");

    protected BlockDevice(Material material) {
        super(material);
        setHardness(1.5F);
        setResistance(10F);
        setSoundType(SoundType.METAL);
    }

    public boolean isHanging(IBlockState state) {
        return false;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public abstract TileEntity createTileEntity(World world, IBlockState state);

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        updatePower(world, pos, true);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
        updatePower(world, pos, false);
    }

    protected void updatePower(World world, BlockPos pos, boolean forceSync) {
        if (world.isRemote) {
            return;
        }
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileConcertDevice) {
            TileConcertDevice dev = (TileConcertDevice) te;
            boolean p = world.isBlockPowered(pos);
            if (dev.powered != p || forceSync) {
                dev.powered = p;
                dev.sync();
            }
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileConcertDevice)) {
            return false;
        }
        if (hand != EnumHand.MAIN_HAND) {
            return true;
        }
        TileConcertDevice dev = (TileConcertDevice) te;
        ItemStack held = player.getHeldItem(hand);

        // shift + empty hand: quick on/off
        if (held.isEmpty() && player.isSneaking()) {
            if (!world.isRemote) {
                dev.commandOn = !dev.commandOn;
                dev.sync();
                player.sendStatusMessage(new TextComponentTranslation(dev.commandOn ? "msg.concertmod.on" : "msg.concertmod.off"), true);
            }
            return true;
        }
        // stick / blaze rod / glowstone / gunpowder: quick settings
        if (dev.isInteractItem(held)) {
            if (!world.isRemote) {
                dev.onInteract(player, held);
            }
            return true;
        }
        // empty hand: open the control menu (client side)
        if (held.isEmpty()) {
            if (world.isRemote) {
                player.openGui(ConcertMod.instance, ConcertMod.GUI_DEVICE, world, pos.getX(), pos.getY(), pos.getZ());
            }
            return true;
        }
        return false;
    }
}
