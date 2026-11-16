package com.concertmod;

import com.concertmod.block.BlockEffectMachine;
import com.concertmod.block.BlockLamp;
import com.concertmod.block.BlockLedScreen;
import com.concertmod.block.BlockSpeaker;
import com.concertmod.block.BlockSpotlight;
import com.concertmod.block.BlockStage;
import com.concertmod.block.BlockTruss;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = ConcertMod.MODID)
public class ModRegistry {
    public static Block SPEAKER;
    public static Block STAGE;
    public static Block TRUSS;
    public static Block LED_SCREEN;
    public static Block SPOTLIGHT;
    public static Block LAMP;
    public static Block STROBE;
    public static Block CONFETTI_MACHINE;
    public static Block SMOKE_MACHINE;
    public static Block FLAME_MACHINE;

    public static final List<Block> ALL = new ArrayList<Block>();

    private static <T extends Block> T name(T block, String name) {
        block.setRegistryName(ConcertMod.MODID, name);
        block.setTranslationKey(ConcertMod.MODID + "." + name);
        block.setCreativeTab(ConcertMod.TAB);
        ALL.add(block);
        return block;
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        ALL.clear();

        SPEAKER = name(new BlockSpeaker(), "speaker");

        STAGE = name(new BlockStage(), "stage_block");
        TRUSS = name(new BlockTruss(), "truss");
        LED_SCREEN = name(new BlockLedScreen(), "led_screen");

        SPOTLIGHT = name(new BlockSpotlight(), "spotlight");
        LAMP = name(new BlockLamp(false), "lamp");
        STROBE = name(new BlockLamp(true), "strobe");
        CONFETTI_MACHINE = name(new BlockEffectMachine(BlockEffectMachine.CONFETTI), "confetti_machine");
        SMOKE_MACHINE = name(new BlockEffectMachine(BlockEffectMachine.SMOKE), "smoke_machine");
        FLAME_MACHINE = name(new BlockEffectMachine(BlockEffectMachine.FLAME), "flame_machine");

        event.getRegistry().registerAll(SPEAKER, STAGE, TRUSS, LED_SCREEN, SPOTLIGHT, LAMP, STROBE,
                CONFETTI_MACHINE, SMOKE_MACHINE, FLAME_MACHINE);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        for (Block b : ALL) {
            ItemBlock item = new ItemBlock(b);
            item.setRegistryName(b.getRegistryName());
            event.getRegistry().register(item);
        }
    }
}
