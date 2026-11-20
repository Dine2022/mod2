package com.concertmod;

import com.concertmod.client.GuiDevice;
import com.concertmod.client.GuiRadio;
import com.concertmod.tile.TileConcertDevice;
import com.concertmod.tile.TileRadioBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

/** Pure client-side control menus (no server containers). */
public class GuiHandler implements IGuiHandler {

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
        if (id == ConcertMod.GUI_DEVICE && te instanceof TileConcertDevice) {
            return new GuiDevice((TileConcertDevice) te);
        }
        if (id == ConcertMod.GUI_RADIO && te instanceof TileRadioBase) {
            return new GuiRadio((TileRadioBase) te);
        }
        return null;
    }
}
