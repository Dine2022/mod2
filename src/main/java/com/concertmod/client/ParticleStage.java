package com.concertmod.client;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

/** Confetti flake, fog puff or flame puff. Uses a sprite from the block atlas (registered in ClientEvents), always full-bright. */
@SideOnly(Side.CLIENT)
public class ParticleStage extends Particle {
    public enum Kind {
        CONFETTI, FOG, FLAME
    }

    private static final Random R = new Random();

    private final Kind kind;
    private float baseScale = 1.0F;
    private float baseAlpha = 1.0F;
    private float growth = 0.0F;
    private final float spin;

    public ParticleStage(World world, double x, double y, double z, double vx, double vy, double vz,
                         Kind kind, TextureAtlasSprite sprite, float r, float g, float b) {
        super(world, x, y, z);
        this.motionX = vx;
        this.motionY = vy;
        this.motionZ = vz;
        this.kind = kind;
        setParticleTexture(sprite);
        setRBGColorF(r, g, b);
        this.particleAngle = R.nextFloat() * 6.2831855F;
        this.prevParticleAngle = this.particleAngle;
        if (kind == Kind.CONFETTI) {
            this.spin = (R.nextFloat() - 0.5F) * 0.6F;
            setup(70 + R.nextInt(60), 0.9F + R.nextFloat() * 0.4F, 1.0F, 0.5F, 0.0F);
        } else if (kind == Kind.FOG) {
            this.spin = (R.nextFloat() - 0.5F) * 0.04F;
            setup(110 + R.nextInt(60), 10.0F + R.nextFloat() * 6.0F, 0.6F, -0.05F, 1.2F);
        } else {
            this.spin = (R.nextFloat() - 0.5F) * 0.3F;
            setup(10 + R.nextInt(8), 3.0F + R.nextFloat() * 2.0F, 0.9F, -0.04F, 0.0F);
        }
    }

    /** Overrides the defaults of the particle kind. */
    public ParticleStage setup(int maxAge, float scale, float alpha, float gravity, float growth) {
        this.particleMaxAge = maxAge;
        this.particleScale = scale;
        this.baseScale = scale;
        this.baseAlpha = alpha;
        this.particleGravity = gravity;
        this.growth = growth;
        setAlphaF(alpha);
        return this;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        float life = this.particleMaxAge > 0 ? (float) this.particleAge / (float) this.particleMaxAge : 1.0F;
        if (life > 1.0F) {
            life = 1.0F;
        }
        this.prevParticleAngle = this.particleAngle;
        this.particleAngle += this.spin;

        if (kind == Kind.FOG) {
            this.particleScale = baseScale * (1.0F + life * growth);
            float fadeIn = Math.min(1.0F, this.particleAge / 8.0F);
            setAlphaF(baseAlpha * fadeIn * (1.0F - life * life));
            this.motionX += (R.nextFloat() - 0.5F) * 0.004F;
            this.motionZ += (R.nextFloat() - 0.5F) * 0.004F;
        } else if (kind == Kind.FLAME) {
            if (life < 0.35F) {
                float f = life / 0.35F;
                setRBGColorF(1.0F, 0.95F - 0.35F * f, 0.55F - 0.45F * f); // yellow -> orange
            } else {
                float f = (life - 0.35F) / 0.65F;
                setRBGColorF(1.0F - 0.45F * f, 0.60F - 0.50F * f, 0.10F - 0.08F * f); // orange -> dark red
            }
            float s = life < 0.25F ? (0.5F + 2.0F * life) : (1.0F - (life - 0.25F) * 0.85F);
            this.particleScale = baseScale * s;
            setAlphaF(life < 0.6F ? baseAlpha : baseAlpha * (1.0F - life) / 0.4F);
        } else {
            this.motionX += (R.nextFloat() - 0.5F) * 0.012F;
            this.motionY += (R.nextFloat() - 0.5F) * 0.006F;
            this.motionZ += (R.nextFloat() - 0.5F) * 0.012F;
            if (this.onGround) {
                this.particleAge += 4; // flakes that landed disappear quickly
            }
            if (life > 0.85F) {
                setAlphaF((1.0F - life) / 0.15F);
            }
        }
    }

    @Override
    public int getBrightnessForRender(float partialTick) {
        return 15728880; // full bright
    }

    @Override
    public int getFXLayer() {
        return 1; // block atlas
    }
}
