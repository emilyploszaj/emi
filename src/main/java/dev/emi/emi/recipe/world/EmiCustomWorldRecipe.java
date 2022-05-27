package dev.emi.emi.recipe.world;

import org.jetbrains.annotations.Nullable;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import net.minecraft.util.Identifier;

public abstract class EmiCustomWorldRecipe implements EmiRecipe {
	private final Identifier id;

	public EmiCustomWorldRecipe(Identifier id) {
		this.id = id;
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return VanillaEmiRecipeCategories.WORLD_INTERACTION;
	}

	@Override
	public @Nullable Identifier getId() {
		return id;
	}

	@Override
	public int getDisplayWidth() {
		return 125;
	}

	@Override
	public int getDisplayHeight() {
		return 18;
	}
}
