package dev.emi.emi.api.recipe;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.util.Identifier;

public abstract class BasicEmiRecipe implements EmiRecipe {
	protected List<EmiIngredient> inputs = Lists.newArrayList();
	protected List<EmiIngredient> catalysts = Lists.newArrayList();
	protected List<EmiStack> outputs = Lists.newArrayList();
	protected EmiRecipeCategory category;
	protected Identifier id;
	protected int width, height;

	public BasicEmiRecipe(EmiRecipeCategory category, Identifier id, int width, int height) {
		this.category = category;
		this.id = id;
		this.width = width;
		this.height = height;
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return category;
	}

	@Override
	public @Nullable Identifier getId() {
		return id;
	}

	@Override
	public List<EmiIngredient> getInputs() {
		return inputs;
	}

	@Override
	public List<EmiIngredient> getCatalysts() {
		return catalysts;
	}

	@Override
	public List<EmiStack> getOutputs() {
		return outputs;
	}

	@Override
	public int getDisplayWidth() {
		return width;
	}

	@Override
	public int getDisplayHeight() {
		return height;
	}
}
