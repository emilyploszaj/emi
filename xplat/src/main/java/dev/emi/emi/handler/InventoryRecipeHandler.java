package dev.emi.emi.handler;

import java.util.List;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;

public class InventoryRecipeHandler implements StandardRecipeHandler<InventoryMenu> {
	public static final Component TOO_SMALL = EmiPort.translatable("emi.too_small");

	@Override
	public List<Slot> getInputSources(InventoryMenu handler) {
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
	public List<Slot> getCraftingSlots(InventoryMenu handler) {
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
	public List<Slot> getCraftingSlots(EmiRecipe recipe, InventoryMenu handler) {
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
	public @Nullable Slot getOutputSlot(InventoryMenu handler) {
		return handler.slots.get(0);
	}

	@Override
	public boolean supportsRecipe(EmiRecipe recipe) {
		if (recipe.getCategory() == VanillaEmiRecipeCategories.CRAFTING && recipe.supportsRecipeTree()) {
			if (recipe instanceof EmiCraftingRecipe crafting) {
				return crafting.canFit(2, 2);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean canCraft(EmiRecipe recipe, EmiCraftContext<InventoryMenu> context) {
		AbstractContainerMenu sh = context.getScreenHandler();
		if (sh instanceof RecipeBookMenu<?, ?> arsh) {
			if (recipe instanceof EmiCraftingRecipe crafting) {
				return crafting.canFit(arsh.getGridWidth(), arsh.getGridHeight())
					&& StandardRecipeHandler.super.canCraft(recipe, context);
			}
		}
		return false;
	}

	@Override
	public List<ClientTooltipComponent> getTooltip(EmiRecipe recipe, EmiCraftContext<InventoryMenu> context) {
		if (!canCraft(recipe, context)) {
			AbstractContainerMenu sh = context.getScreenHandler();
			if (sh instanceof RecipeBookMenu<?, ?> arsh) {
				if (recipe instanceof EmiCraftingRecipe crafting) {
					if (!crafting.canFit(arsh.getGridWidth(), arsh.getGridHeight())) {
						return List.of(ClientTooltipComponent.create(EmiPort.ordered(TOO_SMALL)));
					}
				}
			}
		}
		return StandardRecipeHandler.super.getTooltip(recipe, context);
	}
}
