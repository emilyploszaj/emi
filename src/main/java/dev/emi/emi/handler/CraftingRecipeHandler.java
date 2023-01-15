package dev.emi.emi.handler;

import java.util.List;

import com.google.common.collect.Lists;

import org.jetbrains.annotations.Nullable;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.slot.Slot;

public class CraftingRecipeHandler implements StandardRecipeHandler<CraftingScreenHandler> {

	@Override
	public List<Slot> getInputSources(CraftingScreenHandler handler) {
		List<Slot> list = Lists.newArrayList();
		for (int i = 1; i < 10; i++) { 
			list.add(handler.getSlot(i));
		}
		int invStart = 10;
		for (int i = invStart; i < invStart + 36; i++) { 
			list.add(handler.getSlot(i));
		}
		return list;
	}
	
	@Override
	public List<Slot> getCraftingSlots(CraftingScreenHandler handler) {
		List<Slot> list = Lists.newArrayList();
		for (int i = 1; i < 10; i++) { 
			list.add(handler.getSlot(i));
		}
		return list;
	}

	@Override
	public @Nullable Slot getOutputSlot(CraftingScreenHandler handler) {
		return handler.slots.get(0);
	}

	@Override
	public boolean supportsRecipe(EmiRecipe recipe) {
		return recipe.getCategory() == VanillaEmiRecipeCategories.CRAFTING && recipe.supportsRecipeTree();
	}
}
