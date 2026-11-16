package com.concertmod.client;

import com.concertmod.tile.TileSpotlight;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

/** Draws the rotating spotlight head and its light beam. Motion is computed from world time, so nothing is sent over the network. */
@SideOnly(Side.CLIENT)
public class TESRSpotlight extends TileEntitySpecialRenderer<TileSpotlight> {
    private static final float MAX_LEN = 28.0F;

    @Override
    public boolean isGlobalRenderer(TileSpotlight te) {
        return true;
    }

    @Override
    public void render(TileSpotlight te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        World world = te.getWorld();
        if (world == null) {
            return;
        }
        float t = (float) (world.getTotalWorldTime() % 1000000L) + partialTicks;
        float w = t * te.speed;

        float yaw = te.yaw;
        float pitch = te.pitch;
        switch (te.mode) {
            case SWEEP:
                yaw += 50.0F * MathHelper.sin(w * 0.06F);
                break;
            case CIRCLE:
                yaw += 25.0F * MathHelper.cos(w * 0.08F);
                pitch += 25.0F * MathHelper.sin(w * 0.08F);
                break;
            case SPIN:
                yaw += (w * 3.0F) % 360.0F;
                break;
            default:
                break;
        }
        pitch = MathHelper.clamp(pitch, -90.0F, 90.0F);

        boolean on = te.isActive();
        if (on && te.strobe) {
            on = ((long) (w / 1.5F)) % 2L == 0L;
        }
        int rgb = te.displayColor(t);
        float cr = ((rgb >> 16) & 255) / 255.0F;
        float cg = ((rgb >> 8) & 255) / 255.0F;
        float cb = (rgb & 255) / 255.0F;

        float beamLen = 0F;
        boolean hit = false;
        if (on) {
            double yawRad = Math.toRadians(yaw);
            double pitchRad = Math.toRadians(pitch);
            Vec3d dir = new Vec3d(-Math.sin(yawRad) * Math.cos(pitchRad), -Math.sin(pitchRad), Math.cos(yawRad) * Math.cos(pitchRad));
            BlockPos p = te.getPos();
            Vec3d start = new Vec3d(p.getX() + 0.5D, p.getY() + 0.5D, p.getZ() + 0.5D);
            Vec3d from = start.add(dir.scale(0.9D));
            Vec3d to = start.add(dir.scale(MAX_LEN));
            RayTraceResult res = world.rayTraceBlocks(from, to, false, true, false);
            if (res != null && res.hitVec != null) {
                beamLen = (float) res.hitVec.distanceTo(start);
                hit = true;
            } else {
                beamLen = MAX_LEN;
            }
        }

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y + 0.5D, z + 0.5D);
        GlStateManager.rotate(-yaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.disableBlend();

        // head: dark body + lens
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        RenderUtil.box(buf, -0.17F, -0.17F, -0.22F, 0.17F, 0.17F, 0.18F, 0.16F, 0.16F, 0.18F, 1.0F, true);
        if (on) {
            RenderUtil.box(buf, -0.12F, -0.12F, 0.18F, 0.12F, 0.12F, 0.20F, cr, cg, cb, 1.0F, false);
        } else {
            RenderUtil.box(buf, -0.12F, -0.12F, 0.18F, 0.12F, 0.12F, 0.20F, 0.35F, 0.35F, 0.38F, 1.0F, false);
        }
        tess.draw();

        if (on && beamLen > 0.5F) {
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            GlStateManager.depthMask(false);
            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            RenderUtil.cone(buf, 0.2F, beamLen, 0.10F, 0.10F + beamLen * 0.085F, 0.20F, hit ? 0.06F : 0.0F, rgb);
            RenderUtil.cone(buf, 0.2F, beamLen, 0.06F, 0.05F + beamLen * 0.040F, 0.30F, hit ? 0.10F : 0.0F, rgb);
            tess.draw();
            GlStateManager.depthMask(true);
            GlStateManager.disableBlend();
        }

        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }
}
