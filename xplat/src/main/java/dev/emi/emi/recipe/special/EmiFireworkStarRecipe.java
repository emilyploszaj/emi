package dev.emi.emi.recipe.special;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import com.google.common.collect.Lists;

import dev.emi.emi.api.recipe.EmiPatternCraftingRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.GeneratedSlotWidget;
import dev.emi.emi.api.widget.SlotWidget;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;


public class EmiFireworkStarRecipe extends EmiPatternCraftingRecipe {
	private static final List<DyeItem> DYES = Stream.of(DyeColor.values()).map(DyeItem::byColor).toList();

	private static final List<Item> SHAPES = List.of(Items.FIRE_CHARGE, Items.FEATHER, Items.GOLD_NUGGET, Items.SKELETON_SKULL, Items.WITHER_SKELETON_SKULL, Items.CREEPER_HEAD, Items.PLAYER_HEAD, Items.DRAGON_HEAD, Items.ZOMBIE_HEAD);

	private static final List<Item> EFFECTS = List.of(Items.DIAMOND, Items.GLOWSTONE_DUST);

	public EmiFireworkStarRecipe(ResourceLocation id) {
		super(List.of(
				EmiIngredient.of(DYES.stream().map(i -> (EmiIngredient) EmiStack.of(i)).collect(Collectors.toList())),
						EmiIngredient.of(SHAPES.stream().map(i -> (EmiIngredient) EmiStack.of(i)).collect(Collectors.toList())),
						EmiIngredient.of(EFFECTS.stream().map(i -> (EmiIngredient) EmiStack.of(i)).collect(Collectors.toList())),
						EmiStack.of(Items.GUNPOWDER)),
				EmiStack.of(Items.FIREWORK_STAR), id);
	}

	@Override
	public SlotWidget getInputWidget(int slot, int x, int y) {
		if (slot == 0) {
			return new SlotWidget(EmiStack.of(Items.GUNPOWDER), x, y);
		} else {
			final int s = slot - 1;
			return new GeneratedSlotWidget(r -> {
				List<Item> items = getItems(r);
				if (s < items.size()) {
					return EmiStack.of(items.get(s));
				}
				return EmiStack.EMPTY;
			}, unique, x, y);
		}
	}

	@Override
	public SlotWidget getOutputWidget(int x, int y) {
		return new GeneratedSlotWidget(this::getFireworkStar, unique, x, y);
	}
	private List<DyeItem> getDyes(Random random, int max) {
		List<DyeItem> dyes = Lists.newArrayList();
		int amount = 1 + random.nextInt(max);
		for (int i = 0; i < amount; i++) {
			dyes.add(DYES.get(random.nextInt(DYES.size())));
		}
		return dyes;
	}

	private List<Item> getItems(Random random) {
		List<Item> items = Lists.newArrayList();
		int amount = random.nextInt(4);
		if (amount < 2) {
			items.add(EFFECTS.get(amount));
		} else if (amount == 2) {
			items.add(EFFECTS.get(0));
			items.add(EFFECTS.get(1));
		}
		amount = random.nextInt(10);
		if (amount < 9) {
			items.add(SHAPES.get(amount));
		}

		items.addAll(getDyes(random, 8-items.size()));

		return items;
	}

	private EmiStack getFireworkStar(Random random) {
		ItemStack stack = new ItemStack(Items.FIREWORK_STAR);
		List<Item> items = getItems(random);
		FireworkExplosion.Shape type = FireworkExplosion.Shape.SMALL_BALL;
		IntList colors = new IntArrayList();

		boolean flicker = false, trail = false;

		for (Item item : items) {
			if (Items.GLOWSTONE_DUST.equals(item)) {
				flicker = true;
			} else if (Items.DIAMOND.equals(item)) {
				trail = true;
			} else if (Items.FIRE_CHARGE.equals(item)) {
				type = FireworkExplosion.Shape.LARGE_BALL;
			} else if (Items.GOLD_NUGGET.equals(item)) {
				type = FireworkExplosion.Shape.STAR;
			} else if (Items.FEATHER.equals(item)) {
				type = FireworkExplosion.Shape.BURST;
			} else if (SHAPES.contains(item)) {
				type = FireworkExplosion.Shape.CREEPER;
			} else {
				DyeItem dyeItem = (DyeItem) item;
				colors.add(dyeItem.getDyeColor().getFireworkColor());
			}
		}

		stack.set(DataComponents.FIREWORK_EXPLOSION, new FireworkExplosion(type, colors, IntList.of(), trail, flicker));
		return EmiStack.of(stack);
	}
}

