package dev.emi.emi.recipe;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.VanillaPlugin;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.TextureWidget;
import dev.emi.emi.api.widget.Widget;
import net.minecraft.util.Identifier;

public class EmiWorldRecipe implements EmiRecipe {
	private final boolean isCatalyst;
	private final Identifier id;
	private final EmiIngredient input, catalyst;
	private final EmiStack result;

	public EmiWorldRecipe(EmiIngredient input, EmiIngredient catalyst, EmiStack result, Identifier id) {
		this(input, catalyst, result, id, true);
	}

	public EmiWorldRecipe(EmiIngredient input, EmiIngredient catalyst, EmiStack result, Identifier id, boolean isCatalyst) {
		this.isCatalyst = isCatalyst;
		this.input = input;
		this.catalyst = catalyst;
		this.result = result;
		this.id = id;
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return VanillaPlugin.WORLD_INTERACTION;
	}

	@Override
	public @Nullable Identifier getId() {
		return id;
	}

	@Override
	public List<EmiIngredient> getInputs() {
		if (isCatalyst) {
			return List.of(input);
		} else {
			return List.of(input, catalyst);
		}
	}

	@Override
	public List<EmiIngredient> getCatalysts() {
		if (isCatalyst) {
			return List.of(catalyst);
		} else {
			return List.of();
		}
	}

	@Override
	public List<EmiStack> getOutputs() {
		return List.of(result);
	}

	@Override
	public int getDisplayWidth() {
		return 125;
	}

	@Override
	public int getDisplayHeight() {
		return 18;
	}

	@Override
	public void addWidgets(List<Widget> widgets, int x, int y) {
		widgets.add(new TextureWidget(EmiRenderHelper.WIDGETS, x + 27, y + 3, 13, 13, 82, 0));
		widgets.add(new TextureWidget(EmiRenderHelper.WIDGETS, x + 75, y + 1, 24, 17, 44, 0));
		widgets.add(new SlotWidget(input, x, y));
		widgets.add(new SlotWidget(catalyst, x + 49, y).catalyst(isCatalyst));
		widgets.add(new SlotWidget(result, x + 107, y).recipeContext(this));
	}	
}
