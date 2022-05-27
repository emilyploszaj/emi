package dev.emi.emi;

import java.util.List;

import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.screen.tooltip.RecipeTooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;

public class EmiFavorite implements EmiIngredient {
	private final EmiStack stack;
	private final @Nullable EmiRecipe recipe;

	public EmiFavorite(EmiStack stack, @Nullable EmiRecipe recipe) {
		this.stack = stack;
		this.recipe = recipe;
	}

	public EmiStack getStack() {
		return stack;
	}

	@Override
	public int getAmount() {
		return 1;
	}

	public EmiRecipe getRecipe() {
		return recipe;
	}

	@Override
	public List<EmiStack> getEmiStacks() {
		return List.of(getStack());
	}

	@Override
	public void render(MatrixStack matrices, int x, int y, float delta, int flags) {
		stack.render(matrices, x, y, delta, flags);
	}

	@Override
	public List<TooltipComponent> getTooltip() {
		List<TooltipComponent> list = Lists.newArrayList();
		list.addAll(stack.getTooltip());
		if (recipe != null) {
			list.add(new RecipeTooltipComponent(recipe));
		}
		return list;
	}
}
