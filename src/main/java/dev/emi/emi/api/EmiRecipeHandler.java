package dev.emi.emi.api;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public interface EmiRecipeHandler<T extends ScreenHandler> {
	
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
}
