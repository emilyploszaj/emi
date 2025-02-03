package dev.emi.emi.api.render;

import java.util.List;

import org.jetbrains.annotations.ApiStatus;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.screen.tooltip.EmiTextTooltipWrapper;
import dev.emi.emi.screen.tooltip.RecipeTooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipComponent;

/**
 * Represents metadata about tooltips that EMI creates.
 * Context includes what stack is having its tooltip rendered.
 * This information can be useful for mods modifying the display of tooltips.
 */
@ApiStatus.Experimental
public class EmiTooltipMetadata {
	private static final EmiTooltipMetadata EMPTY = new EmiTooltipMetadata(EmiStack.EMPTY, null);
	private final EmiIngredient stack;
	private final EmiRecipe recipe;

	private EmiTooltipMetadata(EmiIngredient stack, EmiRecipe recipe) {
		this.stack = stack;
		this.recipe = recipe;
	}

	/**
	 * @return The stack responsible for the tooltip, or {@link EmiStack#EMPTY} if not present.
	 */
	public EmiIngredient getStack() {
		return stack;
	}

	/**
	 * @return The recipe context for the tooltip, or {@code null} if not present.
	 */
	public EmiRecipe getRecipe() {
		return recipe;
	}

	/**
	 * Constructs an {@link EmiTooltipMetadata} object based on a list of tooltip components.
	 * This can be useful for retrieving otherwised erased information, like the {@link EmiIngredient}, from a tooltip.
	 */
	public static EmiTooltipMetadata of(List<TooltipComponent> tooltip) {
		if (!tooltip.isEmpty()) {
			TooltipComponent title = tooltip.get(0);
			EmiRecipe recipe = null;
			for (TooltipComponent comp : tooltip) {
				if (comp instanceof RecipeTooltipComponent rtc) {
					recipe = rtc.getRecipe();
					break;
				}
			}
			if (title instanceof EmiTextTooltipWrapper ettw && !ettw.stack.isEmpty()) {
				return new EmiTooltipMetadata(ettw.stack, recipe);
			}
		}
		return EMPTY;
	}
}
