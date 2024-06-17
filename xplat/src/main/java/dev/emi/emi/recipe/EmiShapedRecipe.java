package dev.emi.emi.recipe;

import java.util.List;

import com.google.common.collect.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.runtime.EmiLog;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.ShapedRecipe;

public class EmiShapedRecipe extends EmiCraftingRecipe {

	public EmiShapedRecipe(ShapedRecipe recipe) {
		super(padIngredients(recipe), EmiStack.of(EmiPort.getOutput(recipe)), EmiPort.getId(recipe), false);
		setRemainders(input, recipe);
	}

	public static void setRemainders(List<EmiIngredient> input, CraftingRecipe recipe) {
		try {
			CraftingInventory inv = EmiUtil.getCraftingInventory();
			for (int i = 0; i < input.size(); i++) {
				if (input.get(i).isEmpty()) {
					continue;
				}
				for (int j = 0; j < input.size(); j++) {
					if (j == i) {
						continue;
					}
					if (!input.get(j).isEmpty()) {
						inv.setStack(j, input.get(j).getEmiStacks().get(0).getItemStack().copy());
					}
				}
				List<EmiStack> stacks = input.get(i).getEmiStacks();
				for (EmiStack stack : stacks) {
					inv.setStack(i, stack.getItemStack().copy());
					ItemStack remainder = recipe.getRemainder(inv).get(i);
					if (!remainder.isEmpty()) {
						stack.setRemainder(EmiStack.of(remainder));
					}
				}
				inv.clear();
			}
		} catch (Exception e) {
			EmiLog.error("Exception thrown setting remainders for " + EmiPort.getId(recipe));
			e.printStackTrace();
		}
	}

	private static List<EmiIngredient> padIngredients(ShapedRecipe recipe) {
		List<EmiIngredient> list = Lists.newArrayList();
		int i = 0;
		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 3; x++) {
				if (x >= recipe.getWidth() || y >= recipe.getHeight() || i >= recipe.getIngredients().size()) {
					list.add(EmiStack.EMPTY);
				} else {
					list.add(EmiIngredient.of(recipe.getIngredients().get(i++)));
				}
			}
		}
		return list;
	}
}
