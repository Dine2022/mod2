package com.concertmod.client;

import com.concertmod.block.BlockDevice;
import com.concertmod.tile.TileEffect;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

/** Draws the aimable barrel of confetti / smoke / flame machines. */
@SideOnly(Side.CLIENT)
public class TESREffect extends TileEntitySpecialRenderer<TileEffect> {

    @Override
    public void render(TileEffect te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        int rgb = te.barrelColor();
        float r = ((rgb >> 16) & 255) / 255.0F;
        float g = ((rgb >> 8) & 255) / 255.0F;
        float b = (rgb & 255) / 255.0F;

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        IBlockState st = te.getWorld().getBlockState(te.getPos());
        boolean hanging = st.getBlock() instanceof BlockDevice && ((BlockDevice) st.getBlock()).isHanging(st);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y + (hanging ? 0.6D : 0.4D), z + 0.5D);
        GlStateManager.rotate(-te.yaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(te.pitch, 1.0F, 0.0F, 0.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.disableBlend();

        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        RenderUtil.box(buf, -0.11F, -0.11F, -0.05F, 0.11F, 0.11F, 0.42F, r, g, b, 1.0F, true);
        RenderUtil.box(buf, -0.14F, -0.14F, 0.36F, 0.14F, 0.14F, 0.44F, r * 0.45F, g * 0.45F, b * 0.45F, 1.0F, true);
        RenderUtil.box(buf, -0.07F, -0.07F, 0.43F, 0.07F, 0.07F, 0.45F, 0.05F, 0.05F, 0.06F, 1.0F, false);
        tess.draw();

        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }
}
