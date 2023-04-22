package dev.emi.emi.recipe.special;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.compress.utils.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.EmiPatternCraftingRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.GeneratedSlotWidget;
import dev.emi.emi.api.widget.SlotWidget;
import net.minecraft.item.DyeItem;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

public class EmiArmorDyeRecipe extends EmiPatternCraftingRecipe {
	public static final List<Item> DYEABLE_ITEMS = EmiPort.getItemRegistry().stream()
		.filter(i -> i instanceof DyeableItem).collect(Collectors.toList());
	private static final List<DyeItem> DYES = Stream.of(DyeColor.values()).map(c -> DyeItem.byColor(c)).collect(Collectors.toList());
	private final Item armor;

	public EmiArmorDyeRecipe(Item armor, Identifier id) {
		super(List.of(
			EmiIngredient.of(DYES.stream().map(i -> (EmiIngredient) EmiStack.of(i)).collect(Collectors.toList())),
			EmiStack.of(armor)), EmiStack.of(armor), id);
		this.armor = armor;
	}

	@Override
	public SlotWidget getInputWidget(int slot, int x, int y) {
		if (slot == 0) {
			return new SlotWidget(EmiStack.of(armor), x, y);
		} else {
			final int s = slot - 1;
			return new GeneratedSlotWidget(r -> {
				List<DyeItem> dyes = getDyes(r);
				if (s < dyes.size()) {
					return EmiStack.of(dyes.get(s));
				}
				return EmiStack.EMPTY;
			}, unique, x, y);
		}
	}

	@Override
	public SlotWidget getOutputWidget(int x, int y) {
		return new GeneratedSlotWidget(r -> {
			return EmiStack.of(DyeableItem.blendAndSetColor(new ItemStack(armor), getDyes(r)));
		}, unique, x, y);
	}
	
	private List<DyeItem> getDyes(Random random) {
		List<DyeItem> dyes = Lists.newArrayList();
		int amount = 1 + random.nextInt(8);
		for (int i = 0; i < amount; i++) {
			dyes.add(DYES.get(random.nextInt(DYES.size())));
		}
		return dyes;
	}
}
