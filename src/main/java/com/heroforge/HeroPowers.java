package com.heroforge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Logica de servidor de todos los poderes. Slot 1 = tecla R, slot 2 = tecla G (configurables). */
public final class HeroPowers {
	private static final Map<UUID, long[]> COOLDOWNS = new HashMap<>();

	private HeroPowers() {
	}

	// ------------------------------------------------------------------ estado

	public static Hero getHero(ServerPlayer player) {
		return Hero.byId(player.getAttachedOrElse(HeroForge.HERO_ATTACHMENT, ""));
	}

	public static void setHero(ServerPlayer player, Hero hero) {
		clearFlight(player);
		player.setAttached(HeroForge.HERO_ATTACHMENT, hero.id);
	}

	private static void clearFlight(ServerPlayer p) {
		if (!p.isCreative() && !p.isSpectator() && p.getAbilities().mayfly) {
			p.getAbilities().mayfly = false;
			p.getAbilities().flying = false;
			p.onUpdateAbilities();
		}
	}

	public static boolean wearsFullSuit(ServerPlayer p, Hero hero) {
		Map<ArmorType, Item> suit = ModItems.SUITS.get(hero);
		return p.getItemBySlot(EquipmentSlot.HEAD).is(suit.get(ArmorType.HELMET))
				&& p.getItemBySlot(EquipmentSlot.CHEST).is(suit.get(ArmorType.CHESTPLATE))
				&& p.getItemBySlot(EquipmentSlot.LEGS).is(suit.get(ArmorType.LEGGINGS))
				&& p.getItemBySlot(EquipmentSlot.FEET).is(suit.get(ArmorType.BOOTS));
	}

	// ------------------------------------------------------------------ pasivas (traje completo)

	public static void applyPassives(ServerPlayer p) {
		Hero hero = getHero(p);
		if (hero == null) return;
		if (hero == Hero.CENTINELA && !wearsFullSuit(p, hero)) clearFlight(p);
		if (!wearsFullSuit(p, hero)) return;
		switch (hero) {
			case RELAMPAGO -> {
				effect(p, MobEffects.SPEED, 1);
				effect(p, MobEffects.JUMP_BOOST, 0);
			}
			case CENTINELA -> effect(p, MobEffects.SLOW_FALLING, 0);
			case TEJEDOR -> {
				effect(p, MobEffects.JUMP_BOOST, 1);
				effect(p, MobEffects.SPEED, 0);
			}
			case BASTION -> {
				effect(p, MobEffects.STRENGTH, 0);
				effect(p, MobEffects.RESISTANCE, 0);
			}
			case ESPECTRO -> effect(p, MobEffects.NIGHT_VISION, 0, 260);
			case IGNEA -> effect(p, MobEffects.FIRE_RESISTANCE, 0);
			case GLACIAR -> {
				effect(p, MobEffects.WATER_BREATHING, 0);
				effect(p, MobEffects.DOLPHINS_GRACE, 0);
			}
			case VOLTIO -> effect(p, MobEffects.HASTE, 1);
		}
	}

	private static void effect(ServerPlayer p, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> e, int amp) {
		effect(p, e, amp, 60);
	}

	private static void effect(ServerPlayer p, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> e, int amp, int ticks) {
		p.addEffect(new MobEffectInstance(e, ticks, amp, true, false, true));
	}

	// ------------------------------------------------------------------ habilidades activas

	public static void useAbility(ServerPlayer p, int slot) {
		if (slot != 1 && slot != 2) return;
		Hero hero = getHero(p);
		if (hero == null) {
			p.sendOverlayMessage(Component.translatable("msg.heroforge.no_serum"));
			return;
		}
		ServerLevel level = (ServerLevel) p.level();
		long now = level.getGameTime();
		long[] cd = COOLDOWNS.computeIfAbsent(p.getUUID(), k -> new long[3]);
		if (now < cd[slot]) {
			long secs = (cd[slot] - now + 19) / 20;
			p.sendOverlayMessage(Component.translatable("msg.heroforge.cooldown", secs));
			return;
		}
		cd[slot] = now + (slot == 1 ? hero.cooldown1 : hero.cooldown2);

		switch (hero) {
			case RELAMPAGO -> relampago(p, level, slot);
			case CENTINELA -> centinela(p, level, slot);
			case TEJEDOR -> tejedor(p, level, slot);
			case BASTION -> bastion(p, level, slot);
			case ESPECTRO -> espectro(p, level, slot);
			case IGNEA -> ignea(p, level, slot);
			case GLACIAR -> glaciar(p, level, slot);
			case VOLTIO -> voltio(p, level, slot);
		}
	}

	private static void relampago(ServerPlayer p, ServerLevel level, int slot) {
		if (slot == 1) {
			effect(p, MobEffects.SPEED, 4, 120);
			effect(p, MobEffects.JUMP_BOOST, 2, 120);
			burst(level, p, ParticleTypes.ELECTRIC_SPARK, 40, SoundEvents.LIGHTNING_BOLT_THUNDER, 0.3f);
		} else {
			push(p, p.getLookAngle().multiply(3.0, 0.4, 3.0));
			p.resetFallDistance();
			burst(level, p, ParticleTypes.CLOUD, 30, SoundEvents.PLAYER_ATTACK_SWEEP, 1.4f);
		}
	}

	private static void centinela(ServerPlayer p, ServerLevel level, int slot) {
		if (slot == 1) {
			boolean on = !p.getAbilities().mayfly;
			p.getAbilities().mayfly = on;
			p.getAbilities().flying = on;
			p.onUpdateAbilities();
			burst(level, p, ParticleTypes.CLOUD, 20, SoundEvents.ELYTRA_FLYING, 1.0f);
		} else {
			push(p, new Vec3(p.getDeltaMovement().x, 1.8, p.getDeltaMovement().z));
			p.resetFallDistance();
			burst(level, p, ParticleTypes.CLOUD, 30, SoundEvents.FIREWORK_ROCKET_LAUNCH, 1.0f);
		}
	}

	private static void tejedor(ServerPlayer p, ServerLevel level, int slot) {
		BlockHitResult hit = rayBlock(p, slot == 1 ? 30 : 45);
		if (hit == null) return;
		Vec3 target = hit.getLocation();
		if (slot == 1) {
			BlockPos front = hit.getBlockPos().relative(hit.getDirection());
			if (level.getBlockState(front).isAir()) level.setBlock(front, Blocks.COBWEB.defaultBlockState(), 3);
			for (LivingEntity e : near(level, p, target, 3.0)) {
				e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 4));
			}
			burst(level, p, ParticleTypes.ITEM_COBWEB, 12, SoundEvents.COBWEB_PLACE, 1.0f);
		} else {
			Vec3 pull = target.subtract(p.getEyePosition()).normalize().scale(2.2).add(0, 0.35, 0);
			push(p, pull);
			p.resetFallDistance();
			burst(level, p, ParticleTypes.ITEM_COBWEB, 10, SoundEvents.FISHING_BOBBER_RETRIEVE, 1.0f);
		}
	}

	private static void bastion(ServerPlayer p, ServerLevel level, int slot) {
		if (slot == 1) {
			for (LivingEntity e : near(level, p, p.position(), 6.0)) {
				e.hurtServer(level, level.damageSources().playerAttack(p), 9.0f);
				Vec3 away = e.position().subtract(p.position()).normalize();
				e.push(away.x * 1.2, 0.6, away.z * 1.2);
			}
			burst(level, p, ParticleTypes.EXPLOSION, 8, SoundEvents.GENERIC_EXPLODE.value(), 0.8f);
		} else {
			push(p, p.getLookAngle().multiply(1.6, 0, 1.6).add(0, 1.4, 0));
			p.resetFallDistance();
			burst(level, p, ParticleTypes.POOF, 20, SoundEvents.IRON_GOLEM_ATTACK, 1.0f);
		}
	}

	private static void espectro(ServerPlayer p, ServerLevel level, int slot) {
		if (slot == 1) {
			p.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 240, 0, false, false));
			burst(level, p, ParticleTypes.SMOKE, 30, SoundEvents.ENDERMAN_TELEPORT, 1.0f);
		} else {
			BlockHitResult hit = rayBlock(p, 12);
			Vec3 dest;
			if (hit != null) dest = hit.getLocation().subtract(p.getLookAngle().scale(0.8));
			else dest = p.position().add(p.getLookAngle().multiply(10, 0, 10));
			burst(level, p, ParticleTypes.PORTAL, 40, SoundEvents.ENDERMAN_TELEPORT, 1.0f);
			p.teleportTo(dest.x, dest.y, dest.z);
			p.resetFallDistance();
			burst(level, p, ParticleTypes.PORTAL, 40, SoundEvents.ENDERMAN_TELEPORT, 1.2f);
		}
	}

	private static void ignea(ServerPlayer p, ServerLevel level, int slot) {
		if (slot == 1) {
			Vec3 look = p.getLookAngle();
			SmallFireball fb = new SmallFireball(level, p, look.scale(1.2));
			fb.setPos(p.getX() + look.x, p.getEyeY() - 0.2 + look.y, p.getZ() + look.z);
			level.addFreshEntity(fb);
			level.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0f, 1.0f);
		} else {
			for (LivingEntity e : near(level, p, p.position(), 5.0)) {
				e.igniteForSeconds(6);
				e.hurtServer(level, level.damageSources().playerAttack(p), 4.0f);
			}
			burst(level, p, ParticleTypes.FLAME, 60, SoundEvents.FIRECHARGE_USE, 1.0f);
		}
	}

	private static void glaciar(ServerPlayer p, ServerLevel level, int slot) {
		if (slot == 1) {
			for (LivingEntity e : near(level, p, p.position(), 7.0)) {
				e.setTicksFrozen(e.getTicksRequiredToFreeze() + 120);
				e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 120, 3));
				e.hurtServer(level, level.damageSources().playerAttack(p), 5.0f);
			}
			burst(level, p, ParticleTypes.SNOWFLAKE, 80, SoundEvents.GLASS_BREAK, 0.8f);
		} else {
			for (int i = -1; i <= 1; i++) {
				Snowball sb = new Snowball(level, p, new ItemStack(Items.SNOWBALL));
				sb.shootFromRotation(p, p.getXRot(), p.getYRot() + i * 8f, 0f, 2.2f, 1f);
				level.addFreshEntity(sb);
			}
			level.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.SNOW_GOLEM_SHOOT, SoundSource.PLAYERS, 1.0f, 1.0f);
		}
	}

	private static void voltio(ServerPlayer p, ServerLevel level, int slot) {
		if (slot == 1) {
			BlockHitResult hit = rayBlock(p, 40);
			if (hit == null) return;
			@SuppressWarnings("unchecked")
			EntityType<LightningBolt> boltType = (EntityType<LightningBolt>) BuiltInRegistries.ENTITY_TYPE
					.getValue(Identifier.withDefaultNamespace("lightning_bolt"));
			LightningBolt bolt = boltType.create(level, EntitySpawnReason.TRIGGERED);
			if (bolt != null) {
				Vec3 t = hit.getLocation();
				bolt.setPos(t.x, t.y, t.z);
				level.addFreshEntity(bolt);
			}
		} else {
			effect(p, MobEffects.SPEED, 2, 80);
			for (LivingEntity e : near(level, p, p.position(), 8.0)) {
				e.hurtServer(level, level.damageSources().playerAttack(p), 6.0f);
				level.sendParticles(ParticleTypes.ELECTRIC_SPARK, e.getX(), e.getY() + 1, e.getZ(), 20, 0.3, 0.5, 0.3, 0.2);
			}
			burst(level, p, ParticleTypes.ELECTRIC_SPARK, 60, SoundEvents.LIGHTNING_BOLT_IMPACT, 1.5f);
		}
	}

	// ------------------------------------------------------------------ utilidades

	private static void push(ServerPlayer p, Vec3 v) {
		p.setDeltaMovement(v);
		p.connection.send(new ClientboundSetEntityMotionPacket(p));
	}

	private static BlockHitResult rayBlock(ServerPlayer p, double range) {
		Vec3 from = p.getEyePosition();
		Vec3 to = from.add(p.getLookAngle().scale(range));
		BlockHitResult r = p.level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p));
		return r.getType() == HitResult.Type.MISS ? null : r;
	}

	private static java.util.List<LivingEntity> near(ServerLevel level, ServerPlayer p, Vec3 center, double radius) {
		return level.getEntitiesOfClass(LivingEntity.class,
				new net.minecraft.world.phys.AABB(center, center).inflate(radius), e -> e != p && e.isAlive());
	}

	private static void burst(ServerLevel level, ServerPlayer p, ParticleOptions particle, int count, SoundEvent sound, float pitch) {
		level.sendParticles(particle, p.getX(), p.getY() + 1.0, p.getZ(), count, 0.6, 0.8, 0.6, 0.05);
		level.playSound(null, p.getX(), p.getY(), p.getZ(), sound, SoundSource.PLAYERS, 1.0f, pitch);
	}
}
