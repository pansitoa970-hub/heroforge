package com.heroforge;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ModBlocks {
	public static final Block HERO_ORE = register("hero_ore", key -> new Block(
			BlockBehaviour.Properties.of().strength(3.5f, 3.5f).requiresCorrectToolForDrops()
					.sound(SoundType.AMETHYST).lightLevel(s -> 5).setId(key)));

	public static final Block HERO_TABLE = register("hero_table", key -> new HeroTableBlock(
			BlockBehaviour.Properties.of().strength(3.0f, 6.0f).requiresCorrectToolForDrops()
					.sound(SoundType.METAL).lightLevel(s -> 7).setId(key)));

	private static Block register(String name, java.util.function.Function<ResourceKey<Block>, Block> factory) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, HeroForge.id(name));
		Block block = Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(key));
		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, HeroForge.id(name));
		Registry.register(BuiltInRegistries.ITEM, itemKey,
				new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix()));
		return block;
	}

	public static void init() {
	}
}
