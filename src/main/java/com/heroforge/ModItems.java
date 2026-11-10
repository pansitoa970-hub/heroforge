package com.heroforge;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.EnumMap;
import java.util.Map;

public class ModItems {
	public static final TagKey<Item> REPAIRS_HERO_SUIT =
			TagKey.create(Registries.ITEM, HeroForge.id("repairs_hero_suit"));

	public static final Item RAW_HERONITE = simple("raw_heronite");
	public static final Item HERONITE_INGOT = simple("heronite_ingot");

	public static final Map<Hero, Item> SERUMS = new EnumMap<>(Hero.class);
	public static final Map<Hero, Map<ArmorType, Item>> SUITS = new EnumMap<>(Hero.class);

	static {
		for (Hero hero : Hero.values()) {
			// Suero
			ResourceKey<Item> serumKey = itemKey(hero.id + "_serum");
			SERUMS.put(hero, Registry.register(BuiltInRegistries.ITEM, serumKey,
					new SerumItem(hero, new Item.Properties().setId(serumKey).stacksTo(1))));

			// Traje (4 piezas)
			ResourceKey<EquipmentAsset> asset = ResourceKey.create(EquipmentAssets.ROOT_ID, HeroForge.id(hero.id));
			ArmorMaterial material = new ArmorMaterial(
					38,
					Map.of(ArmorType.HELMET, 3, ArmorType.CHESTPLATE, 8, ArmorType.LEGGINGS, 6, ArmorType.BOOTS, 3,
							ArmorType.BODY, 11),
					18, SoundEvents.ARMOR_EQUIP_NETHERITE, 2.0f, 0.1f, REPAIRS_HERO_SUIT, asset);
			Map<ArmorType, Item> pieces = new EnumMap<>(ArmorType.class);
			register(pieces, hero, material, ArmorType.HELMET, "helmet");
			register(pieces, hero, material, ArmorType.CHESTPLATE, "chestplate");
			register(pieces, hero, material, ArmorType.LEGGINGS, "leggings");
			register(pieces, hero, material, ArmorType.BOOTS, "boots");
			SUITS.put(hero, pieces);
		}
	}

	private static void register(Map<ArmorType, Item> out, Hero hero, ArmorMaterial material, ArmorType type, String suffix) {
		ResourceKey<Item> key = itemKey(hero.id + "_" + suffix);
		out.put(type, Registry.register(BuiltInRegistries.ITEM, key,
				new Item(new Item.Properties().humanoidArmor(material, type).setId(key))));
	}

	private static ResourceKey<Item> itemKey(String name) {
		return ResourceKey.create(Registries.ITEM, HeroForge.id(name));
	}

	private static Item simple(String name) {
		ResourceKey<Item> key = itemKey(name);
		return Registry.register(BuiltInRegistries.ITEM, key, new Item(new Item.Properties().setId(key)));
	}

	public static final CreativeModeTab TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
			HeroForge.id("main"),
			FabricCreativeModeTab.builder()
					.title(Component.translatable("itemGroup.heroforge"))
					.icon(() -> new ItemStack(HERONITE_INGOT))
					.displayItems((params, output) -> {
						output.accept(ModBlocks.HERO_ORE);
						output.accept(ModBlocks.HERO_TABLE);
						output.accept(RAW_HERONITE);
						output.accept(HERONITE_INGOT);
						for (Hero hero : Hero.values()) {
							output.accept(SERUMS.get(hero));
							for (Item piece : SUITS.get(hero).values()) {
								output.accept(piece);
							}
						}
					})
					.build());

	public static void init() {
	}
}
