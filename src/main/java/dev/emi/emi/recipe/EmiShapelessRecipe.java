package dev.emi.emi.recipe;

import java.util.List;

import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.screen.ScreenHandler;

public class EmiShapelessRecipe extends EmiCraftingRecipe {
	
	public EmiShapelessRecipe(ShapelessRecipe recipe) {
		super(recipe.getIngredients().stream().map(i -> EmiIngredient.of(i)).toList(), EmiStack.of(recipe.getOutput()),
			recipe.getId());
		for (int i = 0; i < input.size(); i++) {
			CraftingInventory inv = new CraftingInventory(new ScreenHandler(null, -1) {

				@Override
				public boolean canUse(PlayerEntity player) {
					return false;
				}

				@Override
				public ItemStack transferSlot(PlayerEntity player, int index) {
					return null;
				}
			}, 1, input.size());
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

	@Override
	public boolean canFit(int width, int height) {
		return input.size() <= width * height;
	}
}
