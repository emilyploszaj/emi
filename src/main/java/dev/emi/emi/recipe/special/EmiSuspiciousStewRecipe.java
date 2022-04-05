package dev.emi.emi.recipe.special;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.GeneratedSlotWidget;
import dev.emi.emi.api.widget.SlotWidget;
import net.minecraft.block.FlowerBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SuspiciousStewItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class EmiSuspiciousStewRecipe extends EmiPatternCraftingRecipe {
	private static final List<Item> FLOWERS = Registry.ITEM.getEntries().stream().map(e -> e.getValue())
		.filter(i -> i instanceof BlockItem bi && bi.getBlock() instanceof FlowerBlock).collect(Collectors.toList());

	public EmiSuspiciousStewRecipe(Identifier id) {
		super(List.of(
				EmiStack.of(Items.BOWL),
				EmiStack.of(Items.RED_MUSHROOM),
				EmiStack.of(Items.BROWN_MUSHROOM),
				EmiIngredient.of(FLOWERS.stream().map(i -> (EmiIngredient) EmiStack.of(i)).collect(Collectors.toList()))),
			EmiStack.of(Items.SUSPICIOUS_STEW), id);
	}

	@Override
	public SlotWidget getInputWidget(int slot, int x, int y) {
		if (slot == 0) {
			return new SlotWidget(EmiStack.of(Items.BOWL), x, y);
		} else if (slot == 1) {
			return new SlotWidget(EmiStack.of(Items.RED_MUSHROOM), x, y);
		} else if (slot == 2) {
			return new SlotWidget(EmiStack.of(Items.BROWN_MUSHROOM), x, y);
		} else if (slot == 3) {
			return new GeneratedSlotWidget(r -> EmiStack.of(getFlower(r)), unique, x, y);
		}
		return new SlotWidget(EmiStack.EMPTY, x, y);
	}

	@Override
	public SlotWidget getOutputWidget(int x, int y) {
		return new GeneratedSlotWidget(r -> {
			FlowerBlock block = (FlowerBlock) ((BlockItem) getFlower(r)).getBlock();
			ItemStack stack = new ItemStack(Items.SUSPICIOUS_STEW);
			SuspiciousStewItem.addEffectToStew(stack, block.getEffectInStew(), block.getEffectInStewDuration());
			return EmiStack.of(stack);
		}, unique, x, y);
	}
	
	private Item getFlower(Random random) {
		return FLOWERS.get(random.nextInt(FLOWERS.size()));
	}
}
