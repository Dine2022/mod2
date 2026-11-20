package com.concertmod.item;

import com.concertmod.ConcertMod;
import com.concertmod.radio.RadioNetwork;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Handheld radio controller:
 *  - Right-click in air: toggle its radio channel ON/OFF from anywhere on the map.
 *  - Shift + Right-click in air: switch channel (+1, 1..64).
 *  - Shift + Right-click on a transmitter, receiver, or concert device: bind it to this remote's channel.
 */
public class ItemRadioRemote extends Item {
    private static final String TAG_CHANNEL = "RadioChannel";

    public ItemRadioRemote() {
        setMaxStackSize(1);
        setCreativeTab(ConcertMod.TAB);
    }

    public static int getChannel(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null && tag.hasKey(TAG_CHANNEL)) {
            return RadioNetwork.clampChannel(tag.getInteger(TAG_CHANNEL));
        }
        return RadioNetwork.MIN_CHANNEL;
    }

    public static void setChannel(ItemStack stack, int channel) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        tag.setInteger(TAG_CHANNEL, RadioNetwork.clampChannel(channel));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote) {
            int ch = getChannel(stack);
            if (player.isSneaking()) {
                ch = (ch >= RadioNetwork.MAX_CHANNEL) ? RadioNetwork.MIN_CHANNEL : (ch + 1);
                setChannel(stack, ch);
                world.playSound(null, player.posX, player.posY, player.posZ,
                        SoundEvents.UI_BUTTON_CLICK, SoundCategory.PLAYERS, 0.5F, 1.3F);
                player.sendStatusMessage(new TextComponentTranslation("msg.concertmod.remote_channel", String.valueOf(ch)), true);
            } else {
                boolean active = RadioNetwork.toggleRemote(world, ch);
                world.playSound(null, player.posX, player.posY, player.posZ,
                        SoundEvents.UI_BUTTON_CLICK, SoundCategory.PLAYERS, 0.6F, active ? 1.5F : 0.8F);
                player.sendStatusMessage(new TextComponentTranslation(
                        active ? "msg.concertmod.remote_on" : "msg.concertmod.remote_off", String.valueOf(ch)), true);
            }
        }
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        int ch = getChannel(stack);
        tooltip.add(I18n.format("tooltip.concertmod.radio_channel", ch));
        tooltip.add(I18n.format("tooltip.concertmod.remote_use"));
        tooltip.add(I18n.format("tooltip.concertmod.remote_switch"));
        tooltip.add(I18n.format("tooltip.concertmod.remote_bind"));
    }
}
