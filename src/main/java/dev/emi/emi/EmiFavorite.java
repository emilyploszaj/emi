package dev.emi.emi;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.screen.StackBatcher.Batchable;
import dev.emi.emi.screen.tooltip.RecipeTooltipComponent;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;

public class EmiFavorite implements EmiIngredient, Batchable {
	protected final EmiIngredient stack;
	protected final @Nullable EmiRecipe recipe;

	public EmiFavorite(EmiIngredient stack, @Nullable EmiRecipe recipe) {
		this.stack = stack;
		this.recipe = recipe;
	}

	public EmiIngredient getStack() {
		return stack;
	}

	@Override
	public EmiIngredient copy() {
		return new EmiFavorite(stack, recipe);
	}

	@Override
	public long getAmount() {
		return stack.getAmount();
	}

	@Override
	public EmiIngredient setAmount(long amount) {
		return this;
	}

	@Override
	public float getChance() {
		return 1;
	}

	@Override
	public EmiIngredient setChance(float chance) {
		return this;
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
		if (recipe != null) {
			flags |= EmiIngredient.RENDER_AMOUNT;
		}
		stack.render(matrices, x, y, delta, flags);
		if ((flags & EmiIngredient.RENDER_INGREDIENT) > 0 && recipe != null) {
			EmiRenderHelper.renderRecipeFavorite(stack, matrices, x, y);
		}
	}

	@Override
	public List<TooltipComponent> getTooltip() {
		List<TooltipComponent> list = Lists.newArrayList();
		list.addAll(stack.getTooltip());
		if (recipe != null) {
			list.add(new RecipeTooltipComponent(recipe, true));
		}
		return list;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof EmiIngredient ingredient && EmiIngredient.areEqual(this, ingredient);
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

	public static class Craftable extends EmiFavorite {

		public Craftable(EmiRecipe recipe) {
			super(recipe.getOutputs().isEmpty() ? EmiStack.EMPTY : recipe.getOutputs().get(0), recipe);
		}

		@Override
		public void render(MatrixStack matrices, int x, int y, float delta, int flags) {
			super.render(matrices, x, y, delta, flags & (~EmiIngredient.RENDER_INGREDIENT));
		}
	}

	public static class Synthetic extends EmiFavorite {
		public final long batches;
		public final long amount;
		public final int state;
		public long total;

		public Synthetic(EmiRecipe recipe, long batches, int state) {
			super(recipe.getOutputs().get(0), recipe);
			this.batches = batches;
			this.amount = batches * recipe.getOutputs().get(0).getAmount();
			this.state = state;
		}

		public Synthetic(EmiIngredient ingredient, long needed, long total) {
			super(ingredient, null);
			this.batches = needed;
			this.amount = needed;
			this.total = total;
			this.state = -1;
		}

		@Override
		public void render(MatrixStack matrices, int x, int y, float delta, int flags) {
			int color = 0xff2200;
			if (state == 1) {
				color = 0xaa00ff;
			} else if (state == 2) {
				color = 0x00dddd;
			} else if (state == -1) {
				color = 0xea842a;
			}
			DrawableHelper.fill(matrices, x - 1, y - 1, x + 17, y + 17, 0x44000000 | color);
			stack.render(matrices, x, y, delta, flags & (~EmiIngredient.RENDER_AMOUNT));
			if (recipe != null) {
				EmiRenderHelper.renderAmount(matrices, x, y, EmiPort.literal("" + amount));
			} else {
				EmiRenderHelper.renderAmount(matrices, x, y, stack.getAmountText(amount));
			}
		}

		@Override
		public List<TooltipComponent> getTooltip() {
			List<TooltipComponent> list = Lists.newArrayList();
			list.addAll(super.getTooltip());
			if (state == -1) {
				return list;
			}
			String key = "";
			if (state == 0) {
				key = "tooltip.emi.synfav.uncraftable";
			} else if (state == 1) {
				key = "tooltip.emi.synfav.partially_craftable";
			} else if (state == 2) {
				key = "tooltip.emi.synfav.fully_craftable";
			}
			list.addAll(Arrays
					.stream(I18n.translate(key, batches).split("\n"))
					.map(s -> TooltipComponent.of(EmiPort.ordered(EmiPort.literal(s)))).toList());
			return list;
		}

		@Override
		public boolean isUnbatchable() {
			return true;
		}
	}
}
