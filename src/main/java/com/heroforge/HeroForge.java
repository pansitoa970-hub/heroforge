package com.heroforge;

import com.mojang.serialization.Codec;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class HeroForge implements ModInitializer {
	public static final String MOD_ID = "heroforge";

	/** Heroe del jugador (id del Hero, vacio si no tiene suero). Se guarda y sobrevive a la muerte. */
	public static final AttachmentType<String> HERO_ATTACHMENT = AttachmentRegistry.create(
			id("hero"),
			builder -> builder.persistent(Codec.STRING).copyOnDeath().initializer(() -> ""));

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		ModBlocks.init();
		ModItems.init();

		// Mineral en el mundo (feature definida en data/heroforge/worldgen)
		BiomeModifications.addFeature(
				BiomeSelectors.foundInOverworld(),
				GenerationStep.Decoration.UNDERGROUND_ORES,
				ResourceKey.create(Registries.PLACED_FEATURE, id("hero_ore")));

		// Red: el cliente avisa que tecla de poder se pulso
		PayloadTypeRegistry.playC2S().register(PowerKeyPayload.TYPE, PowerKeyPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(PowerKeyPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			context.server().execute(() -> HeroPowers.useAbility(player, payload.slot()));
		});

		// Pasivas del traje completo (cada segundo)
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTickCount() % 20 != 0) return;
			for (ServerPlayer p : server.getPlayerList().getPlayers()) {
				HeroPowers.applyPassives(p);
			}
		});
	}
}
