package com.heroforge;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/** Inyeccion de poder: al usarla (click derecho) el jugador adquiere el heroe del suero. */
public class SerumItem extends Item {
	private final Hero hero;

	public SerumItem(Hero hero, Properties properties) {
		super(properties);
		this.hero = hero;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (!level.isClientSide() && player instanceof ServerPlayer sp) {
			HeroPowers.setHero(sp, hero);
			player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 100, 0));
			player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));
			player.displayClientMessage(Component.translatable("msg.heroforge.serum_ok", hero.displayName()), true);
			player.getItemInHand(hand).consume(1, player);
		}
		return InteractionResult.SUCCESS;
	}
}
