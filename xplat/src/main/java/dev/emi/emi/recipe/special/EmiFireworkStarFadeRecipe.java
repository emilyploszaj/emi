package dev.emi.emi.recipe.special;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
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
import it.unimi.dsi.fastutil.ints.IntLists;

public class EmiFireworkStarFadeRecipe extends EmiPatternCraftingRecipe {
	private static final List<DyeItem> DYES = Stream.of(DyeColor.values()).map(DyeItem::byColor).toList();

	public EmiFireworkStarFadeRecipe(ResourceLocation id) {
		super(List.of(
			EmiIngredient.of(DYES.stream().map(i -> (EmiIngredient) EmiStack.of(i)).collect(Collectors.toList())),
			EmiStack.of(Items.FIREWORK_STAR)), EmiStack.of(Items.FIREWORK_STAR), id);
	}

	@Override
	public SlotWidget getInputWidget(int slot, int x, int y) {
		return new GeneratedSlotWidget(r -> {
			EmiStack fireworkStar = getFireworkStar(r, false);
			List<DyeItem> dyeItems = getDyes(r, 8);
			final int s = slot - 1;
			if (slot == 0) {
				return fireworkStar;
			}
			if (s < dyeItems.size()) {
				return EmiStack.of(dyeItems.get(s));
			}
			return EmiStack.EMPTY;
		}, unique, x, y);
	}

	@Override
	public SlotWidget getOutputWidget(int x, int y) {
		return new GeneratedSlotWidget(r -> getFireworkStar(r, true), unique, x, y);
	}

	private List<DyeItem> getDyes(Random random, int max) {
		List<DyeItem> dyes = Lists.newArrayList();
		int amount = 1 + random.nextInt(max);
		for (int i = 0; i < amount; i++) {
			dyes.add(DYES.get(random.nextInt(DYES.size())));
		}
		return dyes;
	}

	private EmiStack getFireworkStar(Random random, Boolean faded) {
		ItemStack stack = new ItemStack(Items.FIREWORK_STAR);
		int items = 0;

		int amount = random.nextInt(5);

		FireworkExplosion.Shape type = FireworkExplosion.Shape.values()[random.nextInt(FireworkExplosion.Shape.values().length)];

		if (!(amount == 0)) {
			items++;
		}

		amount = random.nextInt(4);

		boolean flicker = false, trail = false;

		if (amount == 0) {
			flicker = true;
			items++;
		} else if (amount == 1) {
			trail = true;
			items++;
		} else if (amount == 2){
			flicker = true;
			trail = true;
			items = items + 2;
		}

		List<DyeItem> dyeItems = getDyes(random, 8 - items);
		IntList colors = new IntArrayList();
		for (DyeItem dyeItem : dyeItems) {
			colors.add(dyeItem.getDyeColor().getFireworkColor());
		}

		IntList fadedColors;

		if (faded) {
			List<DyeItem> dyeItemsFaded = getDyes(random, 8);
			fadedColors = new IntArrayList();
			for (DyeItem dyeItem : dyeItemsFaded) {
				fadedColors.add(dyeItem.getDyeColor().getFireworkColor());
			}
		} else {
			fadedColors = IntLists.emptyList();
		}

		FireworkExplosion component = new FireworkExplosion(type, colors, fadedColors, trail, flicker);

		stack.set(DataComponents.FIREWORK_EXPLOSION, component);
		return EmiStack.of(stack);
	}
}
