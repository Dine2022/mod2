package com.concertmod;

import com.concertmod.command.CommandConcert;
import com.concertmod.network.MessageDeviceSettings;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import com.concertmod.tile.TileConfetti;
import com.concertmod.tile.TileFlame;
import com.concertmod.tile.TileLamp;
import com.concertmod.tile.TileSmoke;
import com.concertmod.tile.TileSpotlight;
import com.concertmod.tile.TileStrobe;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = ConcertMod.MODID, name = ConcertMod.NAME, version = ConcertMod.VERSION, acceptedMinecraftVersions = "[1.12.2]")
public class ConcertMod {
    public static final String MODID = "concertmod";
    public static final String NAME = "Concert Mod";
    public static final String VERSION = "1.1.0";

    public static final int GUI_DEVICE = 0;

    @Mod.Instance(MODID)
    public static ConcertMod instance;

    public static SimpleNetworkWrapper NET;

    public static final CreativeTabs TAB = new CreativeTabs("concertmod") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(Item.getItemFromBlock(ModRegistry.SPOTLIGHT));
        }
    };

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        NET = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
        NET.registerMessage(MessageDeviceSettings.Handler.class, MessageDeviceSettings.class, 0, Side.SERVER);
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());

        GameRegistry.registerTileEntity(TileSpotlight.class, new ResourceLocation(MODID, "spotlight"));
        GameRegistry.registerTileEntity(TileLamp.class, new ResourceLocation(MODID, "lamp"));
        GameRegistry.registerTileEntity(TileStrobe.class, new ResourceLocation(MODID, "strobe"));
        GameRegistry.registerTileEntity(TileConfetti.class, new ResourceLocation(MODID, "confetti_machine"));
        GameRegistry.registerTileEntity(TileSmoke.class, new ResourceLocation(MODID, "smoke_machine"));
        GameRegistry.registerTileEntity(TileFlame.class, new ResourceLocation(MODID, "flame_machine"));
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandConcert());
    }
}
