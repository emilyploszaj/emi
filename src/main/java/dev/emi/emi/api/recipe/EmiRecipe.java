package dev.emi.emi.api.recipe;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.util.Identifier;

public interface EmiRecipe {

	/**
	 * @return The recipe category this recipe should be displayed under.
	 *  This is used for grouping in the recipe screen, as well as category display in the recipe tree.
	 */
	EmiRecipeCategory getCategory();

	/**
	 * @return The unique id of the recipe, or null. If null, the recipe cannot be serialized.
	 */
	@Nullable Identifier getId();
	
	/**
	 * @return A list of ingredients required for the recipe.
	 * 	Inputs will consider this recipe a use when exploring recipes.
	 */
	List<EmiIngredient> getInputs();
	
	/**
	 * @return A list of ingredients associated with the creation of the recipe.
	 * 	Catalysts are considered the same as workstations in the recipe, not broken down as a requirement.
	 * 	However, catalysts will consider this recipe a use when exploring recipes.
	 */
	default List<EmiIngredient> getCatalysts() {
		return List.of();
	}

	/**
	 * @return A list of stacks that are created after a craft.
	 * 	Outputs will consider this recipe a source when exploring recipes.
	 */
	List<EmiStack> getOutputs();

	/**
	 * @return The width taken up by the recipe's widgets
	 *  EMI will grow to accomodate requested width.
	 *  To fit within the default width, recipes should request a width of 134.
	 *  If a recipe does not support the recipe tree or recipe filling, EMI
	 * 	will not need to add buttons, and it will have space for a width of 160.
	 */
	int getDisplayWidth();

	/**
	 * @return The maximum height taken up by the recipe's widgets.
	 * 	Vertical screen space is capped, however, and EMI may opt to provide less vertical space.
	 * 
	 * @see {@link WidgetHolder#getHeight()} when adding widgets for the EMI adjusted height.
	 */
	int getDisplayHeight();

	/**
	 * Called to add widgets that display the recipe.
	 * Can be used in several places, including the main recipe screen, and tooltips.
	 * It is worth noting that EMI cannot grow vertically, so recipes with large heights
	 * may be provided less space than requested if they span more than the entire vertical
	 * space available in the recipe scren.
	 * In the case of very large heights, recipes should respect {@link WidgetHolder#getHeight()}.
	 */
	void addWidgets(WidgetHolder widgets);

	/**
	 * @return Whether the recipe supports the recipe tree.
	 * 	Recipes that do not represent a set of inputs producing a set of outputs should exclude themselves.
	 *  Example for unsupportable recipes are pattern based recipes, like arbitrary dying.
	 */
	default boolean supportsRecipeTree() {
		return !getInputs().isEmpty() && !getOutputs().isEmpty();
	}

	/**
	 * @return Whether the recipe should be hidden from the craftable menu.
	 *  This is desirable behavior for recipes that are reimplementations of vanilla recipes in other workstations.
	 */
	default boolean hideCraftable() {
		return false;
	}
}
