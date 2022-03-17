package dev.emi.emi.api.recipe;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import dev.emi.emi.EmiRecipeFiller;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Widget;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public interface EmiRecipe {

	EmiRecipeCategory getCategory();

	/**
	 * @return The unique id of the recipe, or null. If null, the recipe cannot be serialized.
	 */
	@Nullable Identifier getId();
	
	List<EmiIngredient> getInputs();

	List<EmiStack> getOutputs();

	default int getDisplayWidth() {
		return 176;
	}
	
	int getDisplayHeight();

	void addWidgets(List<Widget> widgets, int x, int y);

	default boolean canFill(HandledScreen<?> hs) {
		List<ItemStack> stacks = EmiRecipeFiller.fillRecipe(this, hs, true);
		if (stacks != null) {
			return true;
		}
		return false;
	}

	default List<ItemStack> getFill(HandledScreen<?> screen, boolean all) {
		return EmiRecipeFiller.fillRecipe(this, screen, all);
	}
}
