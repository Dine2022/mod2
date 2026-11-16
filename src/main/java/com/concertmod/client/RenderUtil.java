package com.concertmod.client;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Tiny helpers for drawing untextured colored geometry (GL_QUADS, POSITION_COLOR). */
@SideOnly(Side.CLIENT)
public final class RenderUtil {
    private RenderUtil() {
    }

    private static void face(BufferBuilder b, float[] p, float r, float g, float bl, float a, float k) {
        for (int i = 0; i < 12; i += 3) {
            b.pos(p[i], p[i + 1], p[i + 2]).color(r * k, g * k, bl * k, a).endVertex();
        }
    }

    /** Box with 6 faces. With shade=true the faces get different brightness to look 3D without lighting. */
    public static void box(BufferBuilder b, float x0, float y0, float z0, float x1, float y1, float z1,
                           float r, float g, float bl, float a, boolean shade) {
        float top = 1.0F;
        float bottom = shade ? 0.5F : 1.0F;
        float ns = shade ? 0.8F : 1.0F;
        float ew = shade ? 0.6F : 1.0F;
        face(b, new float[]{x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1}, r, g, bl, a, top);
        face(b, new float[]{x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1}, r, g, bl, a, bottom);
        face(b, new float[]{x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0}, r, g, bl, a, ns);
        face(b, new float[]{x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1}, r, g, bl, a, ns);
        face(b, new float[]{x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0}, r, g, bl, a, ew);
        face(b, new float[]{x1, y0, z0, x1, y0, z1, x1, y1, z1, x1, y1, z0}, r, g, bl, a, ew);
    }

    /** Truncated cone along +Z from z0 to z1, alpha fades from a0 to a1. */
    public static void cone(BufferBuilder b, float z0, float z1, float r0, float r1, float a0, float a1, int rgb) {
        float cr = ((rgb >> 16) & 255) / 255.0F;
        float cg = ((rgb >> 8) & 255) / 255.0F;
        float cb = (rgb & 255) / 255.0F;
        int n = 16;
        for (int i = 0; i < n; i++) {
            double ang0 = Math.PI * 2.0D * i / n;
            double ang1 = Math.PI * 2.0D * (i + 1) / n;
            float c0 = (float) Math.cos(ang0);
            float s0 = (float) Math.sin(ang0);
            float c1 = (float) Math.cos(ang1);
            float s1 = (float) Math.sin(ang1);
            b.pos(c0 * r0, s0 * r0, z0).color(cr, cg, cb, a0).endVertex();
            b.pos(c1 * r0, s1 * r0, z0).color(cr, cg, cb, a0).endVertex();
            b.pos(c1 * r1, s1 * r1, z1).color(cr, cg, cb, a1).endVertex();
            b.pos(c0 * r1, s0 * r1, z1).color(cr, cg, cb, a1).endVertex();
        }
    }
}
