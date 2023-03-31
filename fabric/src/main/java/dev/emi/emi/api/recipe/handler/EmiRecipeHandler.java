package dev.emi.emi.api.recipe.handler;

import java.util.List;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.widget.Widget;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;

/**
 * The base recipe handler.
 * In most cases, implementing this is not necessary, and {@link StandardRecipeHandler} can be used instead.
 */
public interface EmiRecipeHandler<T extends ScreenHandler> {
	public static final Text NOT_ENOUGH_INGREDIENTS = EmiPort.translatable("emi.not_enough_ingredients");
	
	/**
	 * @return An inventory with the stacks the player can use for crafting.
	 *  Craftables can only ever be discovered if the inventory contains one of its ingredients.
	 * 	A changed inventory indicates that EMI needs to refresh craftables.
	 */
	EmiPlayerInventory getInventory(HandledScreen<T> screen);

	/**
	 * @return Whether the handler is applicable for the provided recipe.
	 */
	boolean supportsRecipe(EmiRecipe recipe);

	/**
	 * @return Whether the recipe should always display the ability to be filled if supported by this handler.
	 * 	When returning true, the recipe screen will always display a grayed out fill button in all contexts.
	 *  Useful for recipe handlers which support nearly every recipe, and do not want to pollute the recipe screen.
	 */
	default boolean alwaysDisplaySupport(EmiRecipe recipe) {
		return true;
	}

	/**
	 * @return Whether the handler can craft the provided recipe with the given context
	 */
	boolean canCraft(EmiRecipe recipe, EmiCraftContext<T> context);

	/**
	 * @return Whether the craft was successful
	 */
	boolean craft(EmiRecipe recipe, EmiCraftContext<T> context);

	/**
	 * @return The tooltip describing status for crafting the recipe
	 */
	default List<TooltipComponent> getTooltip(EmiRecipe recipe, EmiCraftContext<T> context) {
		if (canCraft(recipe, context)) {
			return List.of();
		} else {
			return List.of(TooltipComponent.of(EmiPort.ordered(NOT_ENOUGH_INGREDIENTS)));
		}
	}

	/**
	 * Render feedback about the status of the current fill.
	 * Common use is to render an overlay on missing ingredients
	 */
	default void render(EmiRecipe recipe, EmiCraftContext<T> context, List<Widget> widgets, MatrixStack matrices) {
	}
}
