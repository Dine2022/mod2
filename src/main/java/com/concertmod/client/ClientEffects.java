package com.concertmod.client;

import com.concertmod.ConcertMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

/** Client-only particle spawning for the effect machines. */
@SideOnly(Side.CLIENT)
public final class ClientEffects {
    private static final Random R = new Random();
    private static final double MAX_DIST = 96.0D;

    private ClientEffects() {
    }

    private static boolean tooFar(Vec3d o) {
        Minecraft mc = Minecraft.getMinecraft();
        return mc.player == null || mc.player.getDistanceSq(o.x, o.y, o.z) > MAX_DIST * MAX_DIST;
    }

    private static TextureAtlasSprite sprite(String name) {
        return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(ConcertMod.MODID + ":blocks/" + name);
    }

    private static double rnd() {
        return R.nextDouble() - 0.5D;
    }

    private static void add(ParticleStage p) {
        Minecraft.getMinecraft().effectRenderer.addEffect(p);
    }

    /** Confetti. Pointing down (ceiling mount) it rains down in a wide cone. */
    public static void confetti(World world, Vec3d o, Vec3d dir, boolean burst, int color, boolean multi) {
        if (tooFar(o)) {
            return;
        }
        TextureAtlasSprite sp = sprite("confetti");
        boolean down = dir.y < -0.8D;
        int n;
        double base;
        double spread;
        if (burst) {
            n = 120;
            base = down ? 0.5D : 0.95D;
            spread = down ? 1.0D : 0.5D;
        } else {
            n = down ? 7 : 5;
            base = down ? 0.2D : 0.6D;
            spread = down ? 1.1D : 0.3D;
        }
        double area = down ? 0.7D : 0.0D;
        for (int i = 0; i < n; i++) {
            double v = base * (0.55D + R.nextDouble() * 0.7D);
            double vx = (dir.x + rnd() * spread) * v;
            double vy = (dir.y + rnd() * spread) * v;
            double vz = (dir.z + rnd() * spread) * v;
            float r;
            float g;
            float b;
            if (multi) {
                int c = MathHelper.hsvToRGB(R.nextFloat(), 0.85F, 1.0F);
                r = ((c >> 16) & 255) / 255.0F;
                g = ((c >> 8) & 255) / 255.0F;
                b = (c & 255) / 255.0F;
            } else {
                float k = 0.75F + R.nextFloat() * 0.25F;
                r = ((color >> 16) & 255) / 255.0F * k;
                g = ((color >> 8) & 255) / 255.0F * k;
                b = (color & 255) / 255.0F * k;
            }
            add(new ParticleStage(world, o.x + rnd() * area * 2.0D, o.y, o.z + rnd() * area * 2.0D, vx, vy, vz,
                    ParticleStage.Kind.CONFETTI, sp, r, g, b));
        }
    }

    /**
     * Fog. type 0 = thick fog (dense, billows and rises), 1 = low fog (spreads along the floor),
     * 2 = haze (thin, lingers for a long time).
     */
    public static void fog(World world, Vec3d o, Vec3d dir, boolean burst, int rgb, int type) {
        if (tooFar(o)) {
            return;
        }
        TextureAtlasSprite sp = sprite("fog");
        float r = ((rgb >> 16) & 255) / 255.0F;
        float g = ((rgb >> 8) & 255) / 255.0F;
        float b = (rgb & 255) / 255.0F;

        if (type == 1) {
            // low fog: spreads out sideways close to the floor
            int n = burst ? 6 : 3;
            for (int i = 0; i < n; i++) {
                double ang = R.nextDouble() * Math.PI * 2.0D;
                double v = 0.10D + R.nextDouble() * 0.12D;
                float shade = 0.92F + R.nextFloat() * 0.08F;
                add(new ParticleStage(world, o.x + rnd() * 0.3D, o.y - 0.7D + rnd() * 0.2D, o.z + rnd() * 0.3D,
                        Math.cos(ang) * v, 0.0D, Math.sin(ang) * v, ParticleStage.Kind.FOG, sp, r * shade, g * shade, b * shade)
                        .setup(170 + R.nextInt(70), 9.0F + R.nextFloat() * 5.0F, 0.55F, 0.0F, 0.6F));
            }
            return;
        }

        if (type == 2) {
            // haze: very thin, huge and slow, hangs in the air
            for (int i = 0; i < 3; i++) {
                double v = 0.04D + R.nextDouble() * 0.06D;
                add(new ParticleStage(world, o.x + rnd() * 0.5D, o.y + rnd() * 0.5D, o.z + rnd() * 0.5D,
                        (dir.x + rnd() * 1.4D) * v, (dir.y + rnd() * 1.4D) * v, (dir.z + rnd() * 1.4D) * v,
                        ParticleStage.Kind.FOG, sp, r, g, b)
                        .setup(260 + R.nextInt(120), 16.0F + R.nextFloat() * 8.0F, 0.11F, -0.01F, 0.6F));
            }
            return;
        }

        // thick fog: dense billowing jet
        int n = burst ? 10 : 5;
        for (int i = 0; i < n; i++) {
            double v = (burst ? 0.42D : 0.30D) * (0.6D + R.nextDouble() * 0.8D);
            double vx = (dir.x + rnd() * 0.45D) * v;
            double vy = (dir.y + rnd() * 0.45D) * v;
            double vz = (dir.z + rnd() * 0.45D) * v;
            float shade = 0.92F + R.nextFloat() * 0.08F;
            add(new ParticleStage(world, o.x + rnd() * 0.25D, o.y + rnd() * 0.25D, o.z + rnd() * 0.25D, vx, vy, vz,
                    ParticleStage.Kind.FOG, sp, r * shade, g * shade, b * shade));
        }
    }

    /** Flame jet: dense orange puffs flying along the barrel (about 6 blocks), plus vanilla flames, lava sparks and smoke. */
    public static void flames(World world, Vec3d o, Vec3d dir, boolean burst) {
        if (tooFar(o)) {
            return;
        }
        TextureAtlasSprite sp = sprite("fog");
        int n = burst ? 10 : 7;
        for (int i = 0; i < n; i++) {
            double v = (0.5D + R.nextDouble() * 0.3D) + (burst ? 0.1D : 0.0D);
            double vx = (dir.x + rnd() * 0.10D) * v;
            double vy = (dir.y + rnd() * 0.10D) * v;
            double vz = (dir.z + rnd() * 0.10D) * v;
            add(new ParticleStage(world, o.x + rnd() * 0.12D, o.y + rnd() * 0.12D, o.z + rnd() * 0.12D, vx, vy, vz,
                    ParticleStage.Kind.FLAME, sp, 1.0F, 0.9F, 0.5F));
        }
        for (int i = 0; i < 2; i++) {
            double v = 0.4D + R.nextDouble() * 0.3D;
            world.spawnParticle(EnumParticleTypes.FLAME, o.x, o.y, o.z, (dir.x + rnd() * 0.12D) * v, (dir.y + rnd() * 0.12D) * v, (dir.z + rnd() * 0.12D) * v);
        }
        if (R.nextInt(3) == 0) {
            world.spawnParticle(EnumParticleTypes.LAVA, o.x + dir.x * 2.0D, o.y + dir.y * 2.0D, o.z + dir.z * 2.0D, 0.0D, 0.0D, 0.0D);
        }
        if (R.nextInt(2) == 0) {
            world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, o.x + dir.x * 5.0D, o.y + dir.y * 5.0D, o.z + dir.z * 5.0D, 0.0D, 0.03D, 0.0D);
        }
    }
}
