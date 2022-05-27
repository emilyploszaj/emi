package dev.emi.emi.api.recipe;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import dev.emi.emi.EmiRecipeFiller;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public interface EmiRecipe {

	EmiRecipeCategory getCategory();

	/**
	 * @return The unique id of the recipe, or null. If null, the recipe cannot be serialized.
	 */
	@Nullable Identifier getId();
	
	/**
	 * @return A list of ingredients required for the recipe.
	 * 	Inputs will consider this recipe a use when exploring recipes.
	 * 
	 */
	List<EmiIngredient> getInputs();

	/**
	 * @return A list of stacks that are created after a craft.
	 * 	Outputs will consider this recipe a source when exploring recipes.
	 */
	List<EmiStack> getOutputs();

	/**
	 * @return The width taken up by the recipe's widgets
	 *  EMI will grow to accomodate requested width.
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
	 */
	void addWidgets(WidgetHolder widgets);

	/**
	 * @return Whether the recipe supports the recipe tree.
	 * 	The basic requirement is having quantifiable inputs and outputs.
	 * 	Recipes that have random or chanced outputs cannot be accurately modeled in a tree, and should exclude themselves.
	 */
	default boolean supportsRecipeTree() {
		return !getInputs().isEmpty() && !getOutputs().isEmpty();
	}

	default boolean canCraft(EmiPlayerInventory inv, HandledScreen<?> screen) {
		return inv.canCraft(this);
	}

	default List<ItemStack> getFill(HandledScreen<?> screen, boolean all) {
		return EmiRecipeFiller.fillRecipe(this, screen, all);
	}
}
