package dev.emi.emi.handler;

import java.util.List;

import com.google.common.collect.Lists;

import org.jetbrains.annotations.Nullable;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.EmiRecipeHandler;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.recipe.EmiShapelessRecipe;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
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
		for (int i = 1; i < 5; i++) { 
			list.add(handler.getSlot(i));
		}
		return list;
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

	@Override
	public List<ItemStack> mutateFill(EmiRecipe recipe, HandledScreen<PlayerScreenHandler> screen, List<ItemStack> stacks) {
		if (recipe instanceof EmiCraftingRecipe crafting && crafting.shapeless) {
			return stacks;
		}
		if (recipe instanceof EmiShapelessRecipe) {
			return stacks;
		}
		List<ItemStack> out = Lists.newArrayList();
		int width = screen.getScreenHandler().getCraftingWidth();
		int height = screen.getScreenHandler().getCraftingHeight();
		for (int i = 0; i < stacks.size(); i++) {
			int x = i % 3;
			int y = i / 3;
			if (x < width && y < height) {
				out.add(stacks.get(i));
			} else if (!stacks.get(i).isEmpty()) {
				return null;
			}
		}
		return out;
	}
}
