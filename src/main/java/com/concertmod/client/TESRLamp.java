package com.concertmod.client;

import com.concertmod.block.BlockLamp;
import com.concertmod.tile.TileLamp;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

/** Draws the bright colored glow of a lit lamp / strobe (full-bright, plus a soft halo). */
@SideOnly(Side.CLIENT)
public class TESRLamp extends TileEntitySpecialRenderer<TileLamp> {

    @Override
    public void render(TileLamp te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        World world = te.getWorld();
        if (world == null) {
            return;
        }
        IBlockState state = world.getBlockState(te.getPos());
        if (!(state.getBlock() instanceof BlockLamp) || !state.getValue(BlockLamp.LIT)) {
            return;
        }
        float t = (float) (world.getTotalWorldTime() % 1000000L) + partialTicks;
        int rgb = te.displayColor(t);
        float r = ((rgb >> 16) & 255) / 255.0F;
        float g = ((rgb >> 8) & 255) / 255.0F;
        float b = (rgb & 255) / 255.0F;

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();

        // solid glowing shell just outside the block
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        RenderUtil.box(buf, -0.004F, -0.004F, -0.004F, 1.004F, 1.004F, 1.004F, r, g, b, 0.9F, false);
        tess.draw();

        // soft additive halo
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        GlStateManager.depthMask(false);
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        RenderUtil.box(buf, -0.2F, -0.2F, -0.2F, 1.2F, 1.2F, 1.2F, r, g, b, 0.16F, false);
        tess.draw();
        GlStateManager.depthMask(true);

        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }
}
