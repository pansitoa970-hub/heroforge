package com.heroforge;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Paquete cliente -> servidor: "pulse la tecla de poder 1 o 2". */
public record PowerKeyPayload(int slot) implements CustomPacketPayload {
	public static final Type<PowerKeyPayload> TYPE = new Type<>(HeroForge.id("power_key"));
	public static final StreamCodec<RegistryFriendlyByteBuf, PowerKeyPayload> CODEC =
			StreamCodec.composite(ByteBufCodecs.VAR_INT, PowerKeyPayload::slot, PowerKeyPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
