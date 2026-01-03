package net.nextfur.fwc.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;

public class ParticleSpurs extends TextureSheetParticle {
    private final SpriteSet spriteSet;

    private final float baseSize;

    public ParticleSpurs(ClientLevel level, double x, double y, double z, SpriteSet spriteSet) {
        super(level, x, y, z);
        this.spriteSet = spriteSet;

        this.lifetime = 80 + this.random.nextInt(40);
        this.gravity = 0.001f;
        this.hasPhysics = true;

        this.baseSize = 0.12f + this.random.nextFloat() * 0.05f;
        this.quadSize = this.baseSize;

        this.xd = (this.random.nextDouble() - 0.5) * 0.05;
        this.yd = (this.random.nextDouble() - 0.5) * 0.05;
        this.zd = (this.random.nextDouble() - 0.5) * 0.05;

        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        float pulse = Mth.sin(this.age * 0.15f);
        this.quadSize = this.baseSize + (pulse * 0.03f);

        this.setSpriteFromAge(spriteSet);

        if (!this.onGround) {
            this.xd += (this.random.nextFloat() - 0.5f) * 0.004f;
            this.yd += (this.random.nextFloat() - 0.5f) * 0.004f;
            this.zd += (this.random.nextFloat() - 0.5f) * 0.004f;
        }

        this.move(this.xd, this.yd, this.zd);

        if (this.onGround) {
            this.xd *= 0.6d;
            this.zd *= 0.6d;
        } else {
            this.xd *= 0.96d;
            this.yd *= 0.96d;
            this.zd *= 0.96d;

            this.yd -= (double) this.gravity;
        }

        float lifecoef = (float) this.age / (float) this.lifetime;
        if (lifecoef < 0.1f) {
            this.alpha = lifecoef * 10.0f;
        } else if (lifecoef > 0.7f) {
            this.alpha = 1.0f - ((lifecoef - 0.7f) * 3.33f);
        } else {
            this.alpha = 1.0f;
        }

        super.tick();
    }


    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
}