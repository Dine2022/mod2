package com.concertmod;

import com.concertmod.client.GuiDevice;
import com.concertmod.tile.TileConcertDevice;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

/** The control menu is a pure client-side screen, so there is no server container. */
public class GuiHandler implements IGuiHandler {

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id == ConcertMod.GUI_DEVICE) {
            TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
            if (te instanceof TileConcertDevice) {
                return new GuiDevice((TileConcertDevice) te);
            }
        }
        return null;
    }
}
