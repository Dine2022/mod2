package com.concertmod.tile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.Locale;

public class TileSpotlight extends TileAimable {

    public enum Motion {
        STATIC, SWEEP, CIRCLE, SPIN;

        public Motion next() {
            return values()[(ordinal() + 1) % values().length];
        }

        public static Motion byName(String s) {
            for (Motion m : values()) {
                if (m.name().equalsIgnoreCase(s)) {
                    return m;
                }
            }
            return null;
        }

        public String key() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public Motion mode = Motion.STATIC;

    @Override
    public boolean onInteract(EntityPlayer player, ItemStack held) {
        if (held.getItem() == Items.GLOWSTONE_DUST) {
            mode = mode.next();
            sync();
            player.sendStatusMessage(new TextComponentTranslation("msg.concertmod.mode",
                    new TextComponentTranslation("mode.concertmod." + mode.key())), true);
            return true;
        }
        return super.onInteract(player, held);
    }

    @Override
    public boolean isInteractItem(ItemStack held) {
        return held.getItem() == Items.GLOWSTONE_DUST || super.isInteractItem(held);
    }

    @Override
    public void applySettings(NBTTagCompound t) {
        super.applySettings(t);
        if (t.hasKey("Mode")) {
            mode = Motion.values()[MathHelper.clamp(t.getInteger("Mode"), 0, Motion.values().length - 1)];
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("Mode", mode.ordinal());
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        int m = tag.getInteger("Mode");
        mode = Motion.values()[MathHelper.clamp(m, 0, Motion.values().length - 1)];
    }
}
