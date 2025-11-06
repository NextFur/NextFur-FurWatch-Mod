package net.nextfur.fwc.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;

public class ParticleEMP extends TextureSheetParticle {
    private final SpriteSet spriteSet;
    private final double originX;
    private final double originY;
    private final double originZ;

    public ParticleEMP(ClientLevel level, double x, double y, double z, SpriteSet spriteSet) {
        super(level, x, y, z);
        this.spriteSet = spriteSet;
        this.gravity = 0f;
        this.quadSize = 0.2f;

        this.originX = x;
        this.originY = y;
        this.originZ = z;

        this.lifetime = 10 + this.random.nextInt(5);
        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public void tick() {
        this.setSpriteFromAge(spriteSet);

        double intensity = 0.05;
        this.x = originX + (random.nextDouble() - 0.5) * intensity;
        this.y = originY + (random.nextDouble() - 0.5) * intensity;
        this.z = originZ + (random.nextDouble() - 0.5) * intensity;

        this.quadSize *= 0.95f;

        this.alpha = ((float) this.lifetime - this.age) / this.lifetime;

        super.tick();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
}
