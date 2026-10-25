package dev.emi.emi.api.render;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.screen.tooltip.IngredientTooltipComponent;
import dev.emi.emi.screen.tooltip.RecipeCostTooltipComponent;
import dev.emi.emi.screen.tooltip.RecipeTooltipComponent;
import dev.emi.emi.screen.tooltip.RemainderTooltipComponent;

public class EmiTooltipComponents {

	/**
	 * @return A tooltip component that displays a provided recipe.
	 */
	public static ClientTooltipComponent getRecipeTooltipComponent(EmiRecipe recipe) {
		return new RecipeTooltipComponent(recipe);
	}

	/**
	 * @return A tooltip component that displays the remainder of a provided ingredient.
	 */
	public static ClientTooltipComponent getRemainderTooltipComponent(EmiIngredient ingredient) {
		return new RemainderTooltipComponent(ingredient);
	}

	/**
	 * @return A tooltip component that displays the the cost breakdown of a provided recipe.
	 */
	public static ClientTooltipComponent getRecipeCostTooltipComponent(EmiRecipe recipe) {
		return new RecipeCostTooltipComponent(recipe);
	}

	/**
	 * @return A tooltip component that displays a collection of stacks to represent an ingredient
	 */
	public static ClientTooltipComponent getIngredientTooltipComponent(List<? extends EmiIngredient> stacks) {
		return new IngredientTooltipComponent(stacks);
	}

	/**
	 * @return A tooltip component that displays the amount of a provided stack.
	 */
	public static ClientTooltipComponent getAmount(EmiIngredient ingredient) {
		return of(EmiRenderHelper.getAmountText(ingredient, ingredient.getAmount()).copy().withStyle(ChatFormatting.GRAY));
	}

	/**
	 * A shorthand to create a tooltip component from text
	 */
	public static ClientTooltipComponent of(Component text) {
		return ClientTooltipComponent.create(text.getVisualOrderText());
	}

	/**
	 * Appends a mod name to a list of components based on a namespace.
	 * Takes into consideration config options and formatting.
	 * EMI's config allows users to disable displaying mod names, so it is possible for the list of components to be unchanged.
	 */
	public static void appendModName(List<ClientTooltipComponent> components, String namespace) {
		if (EmiConfig.appendModId) {
			String mod = EmiUtil.getModName(namespace);
			components.add(of(EmiPort.literal(mod, ChatFormatting.BLUE, ChatFormatting.ITALIC)));
		}
	}
}
