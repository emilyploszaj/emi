package dev.emi.emi.api;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import dev.emi.emi.EmiClient;
import dev.emi.emi.EmiRecipeFiller;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public interface EmiRecipeHandler<T extends ScreenHandler> {
	public static final Text NOT_ENOUGH_INGREDIENTS = new TranslatableText("emi.not_enough_ingredients");
	
	/**
	 * @return The slots for the recipe handler to source ingredients from.
	 * Typically this should include the player's inventory, and crafting slots.
	 */
	List<Slot> getInputSources(T handler);

	/**
	 * @return The slots where inputs should be placed to perform crafting.
	 */
	List<Slot> getCraftingSlots(T handler);

	/**
	 * @return The output slot for recipe handlers that support instant interaction, like crafting tables.
	 * For handlers that have processing time, or where this concept is otherwise inapplicable, null.
	 */
	default @Nullable Slot getOutputSlot(T handler) {
		return null;
	}

	boolean supportsRecipe(EmiRecipe recipe);

	default boolean canCraft(EmiRecipe recipe, EmiPlayerInventory inventory, HandledScreen<T> screen) {
		return inventory.canCraft(recipe);
	}

	default Text getInvalidReason(EmiRecipe recipe, EmiPlayerInventory inventory, HandledScreen<T> screen) {
		return NOT_ENOUGH_INGREDIENTS;
	}

	default boolean performFill(EmiRecipe recipe, HandledScreen<T> screen, EmiFillAction action, int amount) {
		List<ItemStack> stacks = EmiRecipeFiller.getStacks(recipe, screen, amount);
		if (stacks != null) {
			stacks = mutateFill(recipe, screen, stacks);
			if (stacks != null) {
				MinecraftClient.getInstance().setScreen(screen);
				EmiClient.sendFillRecipe(this, screen, screen.getScreenHandler().syncId, action.id, stacks);
				return true;
			}
		}
		return false;
	}

	default List<ItemStack> mutateFill(EmiRecipe recipe, HandledScreen<T> screen, List<ItemStack> stacks) {
		return stacks;
	}
}
