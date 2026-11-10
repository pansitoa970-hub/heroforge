package com.heroforge.client;

import com.heroforge.HeroForge;
import com.heroforge.PowerKeyPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;

public class HeroForgeClient extends Object implements ClientModInitializer {
	// Codigos de tecla GLFW escritos directamente (R = 82, G = 71)
	private static final int KEY_R = 82;
	private static final int KEY_G = 71;

	@Override
	public void onInitializeClient() {
		KeyMapping.Category category = KeyMapping.Category.register(HeroForge.id("main"));

		KeyMapping power1 = KeyMappingHelper.registerKeyMapping(
				new KeyMapping("key.heroforge.power1", KEY_R, category));
		KeyMapping power2 = KeyMappingHelper.registerKeyMapping(
				new KeyMapping("key.heroforge.power2", KEY_G, category));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) return;
			while (power1.consumeClick()) ClientPlayNetworking.send(new PowerKeyPayload(1));
			while (power2.consumeClick()) ClientPlayNetworking.send(new PowerKeyPayload(2));
		});
	}
}
