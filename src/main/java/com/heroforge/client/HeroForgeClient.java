package com.heroforge.client;

import com.heroforge.HeroForge;
import com.heroforge.PowerKeyPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class HeroForgeClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		KeyMapping.Category category = KeyMapping.Category.register(HeroForge.id("main"));

		KeyMapping power1 = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.heroforge.power1", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, category));
		KeyMapping power2 = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.heroforge.power2", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, category));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) return;
			while (power1.consumeClick()) ClientPlayNetworking.send(new PowerKeyPayload(1));
			while (power2.consumeClick()) ClientPlayNetworking.send(new PowerKeyPayload(2));
		});
	}
}
