package dev.emi.emi.handler;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import com.google.common.collect.Lists;

import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.mixin.accessor.CraftingResultSlotAccessor;

public class CoercedRecipeHandler<T extends AbstractContainerMenu> implements StandardRecipeHandler<T> {
	private ResultSlot output;
	private CraftingContainer inv;

	public CoercedRecipeHandler(ResultSlot output) {
		this.output = output;
		this.inv = ((CraftingResultSlotAccessor) output).getInput();
	}

	@Override
	public Slot getOutputSlot(AbstractContainerMenu handler) {
		return output;
	}

	@Override
	public List<Slot> getInputSources(AbstractContainerMenu handler) {
		Minecraft client = Minecraft.getInstance();
		List<Slot> slots = Lists.newArrayList();
		if (output != null) {
			for (Slot slot : handler.slots) {
				if (slot.isActive() && slot.mayPickup(client.player) && slot != output) {
					slots.add(slot);
				}
			}
		}
		return slots;
	}

	@Override
	public List<Slot> getCraftingSlots(AbstractContainerMenu handler) {
		List<Slot> slots = Lists.newArrayList();
		int width = inv.getWidth();
		int height = inv.getHeight();
		for (int i = 0; i < 9; i++) {
			slots.add(null);
		}
		for (Slot slot : handler.slots) {
			if (slot.container == inv && slot.getContainerSlot() < width * height && slot.getContainerSlot() >= 0) {
				int index = slot.getContainerSlot();
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
