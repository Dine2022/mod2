package com.concertmod.client;

import com.concertmod.ConcertMod;
import com.concertmod.ModRegistry;
import com.concertmod.tile.TileConfetti;
import com.concertmod.tile.TileFlame;
import com.concertmod.tile.TileLamp;
import com.concertmod.tile.TileSmoke;
import com.concertmod.tile.TileSpotlight;
import com.concertmod.tile.TileStrobe;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = ConcertMod.MODID)
public class ClientEvents {

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        for (Block block : ModRegistry.ALL) {
            Item item = Item.getItemFromBlock(block);
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(block.getRegistryName(), "inventory"));
        }
        ClientRegistry.bindTileEntitySpecialRenderer(TileSpotlight.class, new TESRSpotlight());
        ClientRegistry.bindTileEntitySpecialRenderer(TileLamp.class, new TESRLamp());
        ClientRegistry.bindTileEntitySpecialRenderer(TileStrobe.class, new TESRLamp());
        ClientRegistry.bindTileEntitySpecialRenderer(TileConfetti.class, new TESREffect());
        ClientRegistry.bindTileEntitySpecialRenderer(TileSmoke.class, new TESREffect());
        ClientRegistry.bindTileEntitySpecialRenderer(TileFlame.class, new TESREffect());
    }

    /** Particle sprites live in the block atlas. */
    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        event.getMap().registerSprite(new ResourceLocation(ConcertMod.MODID, "blocks/confetti"));
        event.getMap().registerSprite(new ResourceLocation(ConcertMod.MODID, "blocks/fog"));
    }
}
