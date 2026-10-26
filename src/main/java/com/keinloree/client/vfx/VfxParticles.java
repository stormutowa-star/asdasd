package com.keinloree.client.vfx;

import com.keinloree.registry.ModParticles;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;

/** Particulas propias: luz completa y mezcla aditiva (el humo, mezcla normal). */
public final class VfxParticles {
    private static final int FULL_LIGHT = 15728880;

    /** Mezcla aditiva sobre el atlas de particulas. */
    static final ParticleRenderType ADDITIVE = new ParticleRenderType() {
        @Override
        public BufferBuilder begin(Tesselator t, TextureManager tm) {
            RenderSystem.depthMask(false);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,
                    com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE);
            return t.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public String toString() {
            return "KEINLOREE_ADDITIVE";
        }
    };

    private VfxParticles() {
    }

    public static void onRegister(RegisterParticleProvidersEvent e) {
        e.registerSpriteSet(ModParticles.EMBER.get(), s -> new Provider(s, Kind.EMBER));
        e.registerSpriteSet(ModParticles.GLYPH.get(), s -> new Provider(s, Kind.GLYPH));
        e.registerSpriteSet(ModParticles.STAR.get(), s -> new Provider(s, Kind.STAR));
        e.registerSpriteSet(ModParticles.SPARK.get(), s -> new Provider(s, Kind.SPARK));
        e.registerSpriteSet(ModParticles.SMOKE.get(), s -> new Provider(s, Kind.SMOKE));
        e.registerSpriteSet(ModParticles.SHARD.get(), s -> new Provider(s, Kind.SHARD));
    }

    enum Kind { EMBER, GLYPH, STAR, SPARK, SMOKE, SHARD }

    private record Provider(SpriteSet sprites, Kind kind) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType o, ClientLevel level, double x, double y, double z,
                                       double vx, double vy, double vz) {
            return new Glow(level, x, y, z, vx, vy, vz, sprites, kind);
        }
    }

    /** Particula con luz completa. */
    private static final class Glow extends TextureSheetParticle {
        private final Kind kind;
        private final float baseSize;

        Glow(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SpriteSet s, Kind kind) {
            super(level, x, y, z, vx, vy, vz);
            this.kind = kind;
            this.pickSprite(s);
            this.xd = vx + (this.random.nextDouble() - 0.5D) * 0.02D;
            this.yd = vy + (this.random.nextDouble() - 0.5D) * 0.02D;
            this.zd = vz + (this.random.nextDouble() - 0.5D) * 0.02D;
            this.rCol = this.gCol = this.bCol = 1.0F;
            switch (kind) {
                case EMBER -> { this.lifetime = 14 + this.random.nextInt(14); this.quadSize = 0.12F; this.gravity = -0.01F; }
                case GLYPH -> { this.lifetime = 20 + this.random.nextInt(16); this.quadSize = 0.16F; this.gravity = 0.02F; }
                case STAR -> { this.lifetime = 14 + this.random.nextInt(10); this.quadSize = 0.2F; this.gravity = 0.0F; }
                case SPARK -> { this.lifetime = 8 + this.random.nextInt(8); this.quadSize = 0.14F; this.gravity = 0.15F; }
                case SMOKE -> { this.lifetime = 30 + this.random.nextInt(20); this.quadSize = 0.35F; this.gravity = -0.01F; }
                case SHARD -> { this.lifetime = 16 + this.random.nextInt(10); this.quadSize = 0.14F; this.gravity = 0.25F; }
            }
            this.baseSize = this.quadSize;
            this.friction = 0.92F;
            this.hasPhysics = false;
        }

        @Override
        public void tick() {
            super.tick();
            if (kind == Kind.SHARD) {
                this.oRoll = this.roll;
                this.roll += 0.25F;
            }
        }

        @Override
        public float getQuadSize(float pt) {
            float t = Mth.clamp((this.age + pt) / this.lifetime, 0.0F, 1.0F);
            return kind == Kind.SMOKE ? this.baseSize * (1.0F + t) : this.baseSize * (1.0F - t * t);
        }

        @Override
        public int getLightColor(float pt) {
            return FULL_LIGHT;
        }

        @Override
        public ParticleRenderType getRenderType() {
            return kind == Kind.SMOKE ? ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT : ADDITIVE;
        }
    }
}
