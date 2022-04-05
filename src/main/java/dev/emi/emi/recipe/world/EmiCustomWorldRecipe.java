package dev.emi.emi.recipe.world;

import org.jetbrains.annotations.Nullable;

import dev.emi.emi.VanillaPlugin;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import net.minecraft.util.Identifier;

public abstract class EmiCustomWorldRecipe implements EmiRecipe {
	private final Identifier id;

	public EmiCustomWorldRecipe(Identifier id) {
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
	public int getDisplayWidth() {
		return 125;
	}

	@Override
	public int getDisplayHeight() {
		return 18;
	}
}
