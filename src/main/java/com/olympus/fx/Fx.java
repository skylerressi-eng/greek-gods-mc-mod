package com.olympus.fx;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/** Server-side particle and sound shapes. Everything is broadcast to nearby players. */
public final class Fx {
	private Fx() {
	}

	public static void sound(ServerLevel level, Vec3 pos, SoundEvent sound, float volume, float pitch) {
		level.playSound(null, pos.x, pos.y, pos.z, sound, SoundSource.PLAYERS, volume, pitch);
	}

	public static void sound(Entity at, SoundEvent sound, float volume, float pitch) {
		sound((ServerLevel) at.level(), at.position(), sound, volume, pitch);
	}

	/** Random puff around a point. */
	public static void burst(ServerLevel level, ParticleOptions particle, Vec3 pos, int count, double spread, double speed) {
		level.sendParticles(particle, pos.x, pos.y, pos.z, count, spread, spread, spread, speed);
	}

	/** Flat ring of particles on the horizontal plane, optionally moving outward. */
	public static void ring(ServerLevel level, ParticleOptions particle, Vec3 center, double radius, int points, double outwardSpeed) {
		for (int i = 0; i < points; i++) {
			double a = (Math.PI * 2 * i) / points;
			double dx = Math.cos(a);
			double dz = Math.sin(a);
			// count = 0 makes (dx,dy,dz) a velocity instead of a spread
			level.sendParticles(particle, center.x + dx * radius, center.y, center.z + dz * radius, 0, dx, 0.0, dz, outwardSpeed);
		}
	}

	/** Several stacked rings, for "shockwave" moments. */
	public static void shockwave(ServerLevel level, ParticleOptions particle, Vec3 center, double radius) {
		for (int r = 1; r <= 3; r++) {
			ring(level, particle, center.add(0, 0.1 * r, 0), radius * r / 3.0, (int) (12 + radius * 6 * r / 3.0), 0.15);
		}
	}

	/** Rising double helix around an entity: "divine power gathers". */
	public static void helix(ServerLevel level, ParticleOptions particle, Vec3 base, double radius, double height, int points) {
		for (int i = 0; i < points; i++) {
			double t = (double) i / points;
			double a = t * Math.PI * 6;
			double y = base.y + t * height;
			level.sendParticles(particle, base.x + Math.cos(a) * radius, y, base.z + Math.sin(a) * radius, 1, 0, 0, 0, 0);
			level.sendParticles(particle, base.x - Math.cos(a) * radius, y, base.z - Math.sin(a) * radius, 1, 0, 0, 0, 0);
		}
	}

	/** Vertical column of particles: claims, smites. */
	public static void pillar(ServerLevel level, ParticleOptions particle, Vec3 base, double height, int count) {
		for (int i = 0; i < count; i++) {
			double y = base.y + height * i / count;
			level.sendParticles(particle, base.x, y, base.z, 2, 0.15, 0.05, 0.15, 0.01);
		}
	}

	/** Straight line between two points. */
	public static void line(ServerLevel level, ParticleOptions particle, Vec3 from, Vec3 to, double step) {
		Vec3 delta = to.subtract(from);
		double length = delta.length();
		if (length < 1.0E-4) {
			return;
		}
		Vec3 dir = delta.scale(1.0 / length);
		for (double d = 0; d <= length; d += step) {
			Vec3 p = from.add(dir.scale(d));
			level.sendParticles(particle, p.x, p.y, p.z, 1, 0, 0, 0, 0);
		}
	}

	/** Sphere shell around a point (used for area-of-effect reveals). */
	public static void sphere(ServerLevel level, ParticleOptions particle, Vec3 center, double radius, int points) {
		double golden = Math.PI * (3.0 - Math.sqrt(5.0));
		for (int i = 0; i < points; i++) {
			double y = 1 - (i / (double) (points - 1)) * 2;
			double r = Math.sqrt(1 - y * y);
			double theta = golden * i;
			level.sendParticles(particle, center.x + Mth.cos((float) theta) * r * radius, center.y + y * radius,
				center.z + Mth.sin((float) theta) * r * radius, 1, 0, 0, 0, 0);
		}
	}
}
