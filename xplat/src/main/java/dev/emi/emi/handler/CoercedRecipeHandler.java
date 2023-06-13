package dev.emi.emi.handler;

import java.util.List;

import com.google.common.collect.Lists;

import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.mixin.accessor.CraftingResultSlotAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;

public class CoercedRecipeHandler<T extends ScreenHandler> implements StandardRecipeHandler<T> {
	private CraftingResultSlot output;
	private RecipeInputInventory inv;

	public CoercedRecipeHandler(CraftingResultSlot output) {
		this.output = output;
		this.inv = ((CraftingResultSlotAccessor) output).getInput();
	}

	@Override
	public Slot getOutputSlot(ScreenHandler handler) {
		return output;
	}

	@Override
	public List<Slot> getInputSources(ScreenHandler handler) {
		MinecraftClient client = MinecraftClient.getInstance();
		List<Slot> slots = Lists.newArrayList();
		if (output != null) {
			for (Slot slot : handler.slots) {
				if (slot.isEnabled() && slot.canTakeItems(client.player) && slot != output) {
					slots.add(slot);
				}
			}
		}
		return slots;
	}

	@Override
	public List<Slot> getCraftingSlots(ScreenHandler handler) {
		List<Slot> slots = Lists.newArrayList();
		int width = inv.getWidth();
		int height = inv.getHeight();
		for (int i = 0; i < 9; i++) {
			slots.add(null);
		}
		for (Slot slot : handler.slots) {
			if (slot.inventory == inv && slot.getIndex() < width * height && slot.getIndex() >= 0) {
				int index = slot.getIndex();
				index = index * 3 / width;
				slots.set(index, slot);
			}
		}
		return slots;
	}

	@Override
	public boolean supportsRecipe(EmiRecipe recipe) {
		if (recipe.getCategory() == VanillaEmiRecipeCategories.CRAFTING && recipe.supportsRecipeTree()) {
			if (recipe instanceof EmiCraftingRecipe crafting) {
				return crafting.canFit(inv.getWidth(), inv.getHeight());
			}
			return true;
		}
		return false;
	}
}
