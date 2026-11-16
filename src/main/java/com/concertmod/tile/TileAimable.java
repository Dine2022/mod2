package com.concertmod.tile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;

/** A device that points somewhere (spotlight, effect machines). */
public abstract class TileAimable extends TileConcertDevice {
    /** Minecraft convention: 0 = south, 90 = west. */
    public float yaw = 0F;
    /** Positive = pointing down, -90 = straight up. */
    public float pitch = 35F;

    /** Unit vector the device points along. */
    public Vec3d direction() {
        double yr = Math.toRadians(yaw);
        double pr = Math.toRadians(pitch);
        return new Vec3d(-Math.sin(yr) * Math.cos(pr), -Math.sin(pr), Math.cos(yr) * Math.cos(pr));
    }

    @Override
    public boolean onInteract(EntityPlayer player, ItemStack held) {
        if (held.getItem() == Items.STICK) {
            yaw = MathHelper.wrapDegrees(yaw + 15F);
            sync();
            player.sendStatusMessage(new TextComponentTranslation("msg.concertmod.yaw", String.valueOf((int) yaw)), true);
            return true;
        }
        if (held.getItem() == Items.BLAZE_ROD) {
            pitch += 15F;
            if (pitch > 90.1F) {
                pitch = -90F;
            }
            sync();
            player.sendStatusMessage(new TextComponentTranslation("msg.concertmod.pitch", String.valueOf((int) pitch)), true);
            return true;
        }
        return false;
    }

    @Override
    public boolean isInteractItem(ItemStack held) {
        return held.getItem() == Items.STICK || held.getItem() == Items.BLAZE_ROD || super.isInteractItem(held);
    }

    @Override
    public void applySettings(NBTTagCompound t) {
        super.applySettings(t);
        if (t.hasKey("Yaw")) {
            yaw = MathHelper.wrapDegrees(t.getFloat("Yaw"));
        }
        if (t.hasKey("Pitch")) {
            pitch = MathHelper.clamp(t.getFloat("Pitch"), -90.0F, 90.0F);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setFloat("Yaw", yaw);
        tag.setFloat("Pitch", pitch);
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        if (tag.hasKey("Yaw")) {
            yaw = tag.getFloat("Yaw");
        }
        if (tag.hasKey("Pitch")) {
            pitch = tag.getFloat("Pitch");
        }
    }
}
