package dev.emi.emi;

import java.util.List;

import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.screen.StackBatcher.Batchable;
import dev.emi.emi.screen.tooltip.RecipeTooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

public class EmiFavorite implements EmiIngredient, Batchable {
	private final EmiIngredient stack;
	private final @Nullable EmiRecipe recipe;

	public EmiFavorite(EmiIngredient stack, @Nullable EmiRecipe recipe) {
		this.stack = stack;
		this.recipe = recipe;
	}

	public EmiIngredient getStack() {
		return stack;
	}

	@Override
	public long getAmount() {
		return stack.getAmount();
	}

	public EmiRecipe getRecipe() {
		return recipe;
	}

	@Override
	public List<EmiStack> getEmiStacks() {
		return stack.getEmiStacks();
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

	@Override
	public boolean isSideLit() {
		return stack instanceof Batchable b && b.isSideLit();
	}

	@Override
	public boolean isUnbatchable() {
		return !(stack instanceof Batchable b) || b.isUnbatchable();
	}

	@Override
	public void setUnbatchable() {
		if (stack instanceof Batchable b) {
			b.setUnbatchable();
		}
	}

	@Override
	public void renderForBatch(VertexConsumerProvider vcp, MatrixStack matrices, int x, int y, int z, float delta) {
		if (stack instanceof Batchable b) {
			b.renderForBatch(vcp, matrices, x, y, z, delta);
		}
	}
}
