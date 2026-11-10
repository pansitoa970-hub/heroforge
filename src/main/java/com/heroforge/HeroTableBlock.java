package com.heroforge;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Mesa de Forja Heroica: cuadricula 3x3 propia (misma interfaz que la mesa de crafteo). */
public class HeroTableBlock extends CraftingTableBlock {
	public HeroTableBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
		return new SimpleMenuProvider((containerId, inventory, player) -> {
			ContainerLevelAccess access = ContainerLevelAccess.create(level, pos);
			return new CraftingMenu(containerId, inventory, access) {
				@Override
				public boolean stillValid(Player p) {
					return stillValid(access, p, ModBlocks.HERO_TABLE);
				}
			};
		}, Component.translatable("block.heroforge.hero_table"));
	}
}
