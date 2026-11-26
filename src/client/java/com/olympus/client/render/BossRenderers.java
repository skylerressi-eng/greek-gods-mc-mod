package com.olympus.client.render;

import com.olympus.registry.ModEntities;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.BlazeRenderer;
import net.minecraft.client.renderer.entity.ElderGuardianRenderer;
import net.minecraft.client.renderer.entity.HoglinRenderer;
import net.minecraft.client.renderer.entity.WitchRenderer;
import net.minecraft.client.renderer.entity.WitherSkeletonRenderer;
import net.minecraft.client.renderer.entity.ZoglinRenderer;
import net.minecraft.client.renderer.entity.state.GuardianRenderState;
import net.minecraft.client.renderer.entity.state.HoglinRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.client.renderer.entity.state.WitchRenderState;

/**
 * Placeholder boss renderers: vanilla models, tinted (and scaled via the SCALE attribute).
 * This is the ONE place to swap in real Blockbench models later: replace a line with your
 * own renderer class for that boss.
 */
public final class BossRenderers {
	private BossRenderers() {
	}

	public static void register() {
		EntityRendererRegistry.register(ModEntities.TYPHON, ctx -> new BlazeRenderer(ctx) {
			@Override
			protected int getModelTint(LivingEntityRenderState state) {
				return 0xFF8FB8FF; // storm blue
			}
		});
		EntityRendererRegistry.register(ModEntities.CETUS, ctx -> new ElderGuardianRenderer(ctx) {
			@Override
			protected int getModelTint(GuardianRenderState state) {
				return 0xFF4F8FD8; // deep-sea blue
			}
		});
		EntityRendererRegistry.register(ModEntities.CERBERUS, ctx -> new HoglinRenderer(ctx) {
			@Override
			protected int getModelTint(HoglinRenderState state) {
				return 0xFF5A2020; // underworld black-red
			}
		});
		EntityRendererRegistry.register(ModEntities.MEDUSA, ctx -> new WitchRenderer(ctx) {
			@Override
			protected int getModelTint(WitchRenderState state) {
				return 0xFF7FD87F; // gorgon green
			}
		});
		EntityRendererRegistry.register(ModEntities.COLCHIAN_DRAGON, ctx -> new ZoglinRenderer(ctx) {
			@Override
			protected int getModelTint(HoglinRenderState state) {
				return 0xFFC8D85A; // golden-green scales
			}
		});
		EntityRendererRegistry.register(ModEntities.KRONOS, ctx -> new WitherSkeletonRenderer(ctx) {
			@Override
			protected int getModelTint(SkeletonRenderState state) {
				return 0xFFD8B45A; // titan gold
			}
		});
	}
}
