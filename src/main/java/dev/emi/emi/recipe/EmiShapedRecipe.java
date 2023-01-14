package dev.emi.emi.recipe;

import java.util.List;

import org.apache.commons.compress.utils.Lists;

import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.screen.ScreenHandler;

public class EmiShapedRecipe extends EmiCraftingRecipe {

	public EmiShapedRecipe(ShapedRecipe recipe) {
		super(padIngredients(recipe), EmiStack.of(recipe.getOutput()),
			recipe.getId(), false);
		for (int i = 0; i < input.size(); i++) {
			if (input.get(i).isEmpty()) {
				continue;
			}
			CraftingInventory inv = new CraftingInventory(new ScreenHandler(null, -1) {

				@Override
				public boolean canUse(PlayerEntity player) {
					return false;
				}

				@Override
				public ItemStack quickMove(PlayerEntity player, int index) {
					return null;
				}
			}, 3, 3);
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
