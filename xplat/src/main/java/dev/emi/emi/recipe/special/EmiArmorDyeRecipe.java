package dev.emi.emi.recipe.special;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import com.google.common.collect.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.EmiPatternCraftingRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.GeneratedSlotWidget;
import dev.emi.emi.api.widget.SlotWidget;

public class EmiArmorDyeRecipe extends EmiPatternCraftingRecipe {
	private static final List<Item> DYES = EmiPort.getDyeItems();
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
				List<Item> dyes = getDyes(r);
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
			return EmiStack.of(DyedItemColor.applyDyes(new ItemStack(armor), getDyes(r).stream().map(EmiPort::getDyeColor).toList()));
		}, unique, x, y);
	}
	
	private List<Item> getDyes(Random random) {
		List<Item> dyes = Lists.newArrayList();
		int amount = 1 + random.nextInt(8);
		for (int i = 0; i < amount; i++) {
			dyes.add(DYES.get(random.nextInt(DYES.size())));
		}
		return dyes;
	}
}
