package dev.emi.emi.handler;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.EmiRecipeHandler;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

public class InventoryRecipeHandler implements EmiRecipeHandler<PlayerScreenHandler> {
	public static final Text TOO_SMALL = EmiPort.translatable("emi.too_small");

	@Override
	public List<Slot> getInputSources(PlayerScreenHandler handler) {
		List<Slot> list = Lists.newArrayList();
		for (int i = 1; i < 5; i++) { 
			list.add(handler.getSlot(i));
		}
		int invStart = 9;
		for (int i = invStart; i < invStart + 36; i++) { 
			list.add(handler.getSlot(i));
		}
		return list;
	}
	
	@Override
	public List<Slot> getCraftingSlots(PlayerScreenHandler handler) {
		List<Slot> list = Lists.newArrayList();
		// This is like, bad, right? There has to be a better way to do this
		list.add(handler.getSlot(1));
		list.add(handler.getSlot(2));
		list.add(null);
		list.add(handler.getSlot(3));
		list.add(handler.getSlot(4));
		list.add(null);
		list.add(null);
		list.add(null);
		list.add(null);
		return list;
	}

	@Override
	public List<Slot> getCraftingSlots(EmiRecipe recipe, HandledScreen<PlayerScreenHandler> screen) {
		PlayerScreenHandler handler = screen.getScreenHandler();
		if (recipe instanceof EmiCraftingRecipe craf && craf.shapeless) {
			List<Slot> list = Lists.newArrayList();
			list.add(handler.getSlot(1));
			list.add(handler.getSlot(2));
			list.add(handler.getSlot(3));
			list.add(handler.getSlot(4));
			return list;
		}
		return getCraftingSlots(handler);
	}

	@Override
	public @Nullable Slot getOutputSlot(PlayerScreenHandler handler) {
		return handler.slots.get(0);
	}

	@Override
	public boolean supportsRecipe(EmiRecipe recipe) {
		return recipe.getCategory() == VanillaEmiRecipeCategories.CRAFTING && recipe.supportsRecipeTree();
	}

	@Override
	public boolean canCraft(EmiRecipe recipe, EmiPlayerInventory inventory, HandledScreen<PlayerScreenHandler> screen) {
		ScreenHandler sh = screen.getScreenHandler();
		if (sh instanceof AbstractRecipeScreenHandler<?> arsh) {
			if (recipe instanceof EmiCraftingRecipe crafting) {
				return crafting.canFit(arsh.getCraftingWidth(), arsh.getCraftingHeight())
					&& EmiRecipeHandler.super.canCraft(recipe, inventory, screen);
			}
		}
		return false;
	}

	@Override
	public Text getInvalidReason(EmiRecipe recipe, EmiPlayerInventory inventory, HandledScreen<PlayerScreenHandler> screen) {
		ScreenHandler sh = screen.getScreenHandler();
		if (sh instanceof AbstractRecipeScreenHandler<?> arsh) {
			if (recipe instanceof EmiCraftingRecipe crafting) {
				if (!crafting.canFit(arsh.getCraftingWidth(), arsh.getCraftingHeight())) {
					return TOO_SMALL;
				}
			}
		}
		return EmiRecipeHandler.super.getInvalidReason(recipe, inventory, screen);
	}
}
