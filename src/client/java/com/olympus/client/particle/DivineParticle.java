package com.olympus.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/**
 * One configurable particle class for every god's signature particle. Each {@link Style} tunes
 * gravity, drag, size, spin, lifetime and whether it glows in the dark.
 */
public class DivineParticle extends SingleQuadParticle {
	public record Style(float gravity, float friction, float size, int lifetime, int lifetimeJitter, float spin, boolean emissive, boolean shrink, boolean flicker) {
		/** Glowing motes that hang in the air and fade (light, magic). */
		public static final Style MOTE = new Style(-0.002f, 0.92f, 0.12f, 30, 15, 0.0f, true, true, false);
		/** Fluttering things that fall slowly and spin (feathers, leaves, petals). */
		public static final Style FLUTTER = new Style(0.012f, 0.95f, 0.16f, 50, 20, 0.12f, false, false, false);
		/** Hot sparks that arc and fall (forge, war). */
		public static final Style SPARK = new Style(0.04f, 0.94f, 0.08f, 18, 10, 0.0f, true, true, true);
		/** Electric crackle: short, bright, jittery. */
		public static final Style CRACKLE = new Style(0.0f, 0.80f, 0.14f, 8, 6, 0.6f, true, false, true);
		/** Drops that fall quickly (water, wine). */
		public static final Style DROP = new Style(0.06f, 0.96f, 0.10f, 26, 10, 0.0f, false, false, false);
		/** Smoky shade that rises and grows dim (Hades). */
		public static final Style SMOKE = new Style(-0.006f, 0.90f, 0.22f, 40, 20, 0.05f, false, false, false);
		/** Wind wisps that swirl and vanish (Hermes). */
		public static final Style WISP = new Style(0.0f, 0.88f, 0.18f, 16, 8, 0.25f, true, true, false);
	}

	private static final double NEAR_FADE = 2.2;

	private final SpriteSet sprites;
	private final Style style;
	private final float baseSize;
	private final float spin;

	protected DivineParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, SpriteSet sprites, Style style) {
		super(level, x, y, z, sprites.first());
		this.sprites = sprites;
		this.style = style;
		// Use the exact velocity the server sent (vanilla's constructor would add jitter).
		this.xd = xd;
		this.yd = yd;
		this.zd = zd;
		this.gravity = style.gravity();
		this.friction = style.friction();
		this.lifetime = style.lifetime() + (style.lifetimeJitter() > 0 ? this.random.nextInt(style.lifetimeJitter()) : 0);
		this.baseSize = style.size() * (0.75f + this.random.nextFloat() * 0.5f);
		this.quadSize = this.baseSize;
		this.spin = style.spin() * (this.random.nextBoolean() ? 1 : -1) * (0.5f + this.random.nextFloat());
		this.roll = this.random.nextFloat() * Mth.TWO_PI;
		this.oRoll = this.roll;
		this.hasPhysics = style.gravity() > 0;
		this.setSpriteFromAge(sprites);
	}

	@Override
	public void tick() {
		super.tick();
		if (this.removed) {
			return;
		}
		this.setSpriteFromAge(this.sprites);
		this.oRoll = this.roll;
		this.roll += this.spin;
		float life = (float) this.age / this.lifetime;
		if (style.shrink()) {
			this.quadSize = this.baseSize * (1.0f - life * life);
		}
		float fade = life > 0.6f ? 1.0f - (life - 0.6f) / 0.4f : 1.0f;
		if (style.flicker() && this.random.nextFloat() < 0.15f) {
			fade *= 0.4f;
		}
		// Never block the player's view: particles fade out as they get close to the camera.
		var camera = net.minecraft.client.Minecraft.getInstance().gameRenderer.getMainCamera().position();
		double dx = this.x - camera.x, dy = this.y - camera.y, dz = this.z - camera.z;
		double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (dist < NEAR_FADE) {
			fade *= (float) Math.max(0.0, (dist - 1.0) / (NEAR_FADE - 1.0));
		}
		this.setAlpha(Mth.clamp(fade, 0.0f, 1.0f));
	}

	@Override
	protected Layer getLayer() {
		return Layer.TRANSLUCENT;
	}

	@Override
	public int getLightColor(float partialTick) {
		return style.emissive() ? 0xF000F0 : super.getLightColor(partialTick);
	}

	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprites;
		private final Style style;

		public Provider(SpriteSet sprites, Style style) {
			this.sprites = sprites;
			this.style = style;
		}

		@Override
		public Particle createParticle(SimpleParticleType options, ClientLevel level, double x, double y, double z, double xd, double yd, double zd, RandomSource random) {
			return new DivineParticle(level, x, y, z, xd, yd, zd, sprites, style);
		}
	}
}
