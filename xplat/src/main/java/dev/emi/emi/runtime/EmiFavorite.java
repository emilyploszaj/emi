package dev.emi.emi.runtime;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.render.EmiTooltipComponents;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.FluidEmiStack;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.config.HelpLevel;
import dev.emi.emi.registry.EmiRecipeFiller;
import dev.emi.emi.screen.MicroTextRenderer;
import dev.emi.emi.screen.StackBatcher.Batchable;
import dev.emi.emi.screen.tooltip.RecipeTooltipComponent;

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
	public void render(GuiGraphics raw, int x, int y, float delta, int flags) {
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		if (recipe != null) {
			flags |= EmiIngredient.RENDER_AMOUNT;
		}
		stack.render(context.raw(), x, y, delta, flags);
		if ((flags & EmiIngredient.RENDER_INGREDIENT) > 0 && recipe != null) {
			EmiRenderHelper.renderRecipeFavorite(stack, context, x, y);
		}
	}

	@Override
	public List<ClientTooltipComponent> getTooltip() {
		List<ClientTooltipComponent> list = Lists.newArrayList();
		list.addAll(stack.getTooltip());
		if (recipe != null) {
			list.add(new RecipeTooltipComponent(recipe, true));
		}
		return list;
	}

	public boolean strictEquals(EmiIngredient other) {
		List<EmiStack> as = this.getEmiStacks();
		List<EmiStack> bs = other.getEmiStacks();
		if (as.size() != bs.size()) {
			return false;
		}
		for (int i = 0; i < as.size(); i++) {
			if (!as.get(i).isEqual(bs.get(i), EmiPort.compareStrict())) {
				return false;
			}
		}
		return true;
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
	public void renderForBatch(MultiBufferSource vcp, GuiGraphics raw, int x, int y, int z, float delta) {
		if (stack instanceof Batchable b) {
			b.renderForBatch(vcp, raw, x, y, z, delta);
		}
	}

	public static class Craftable extends EmiFavorite {

		public Craftable(EmiRecipe recipe) {
			super(recipe.getOutputs().isEmpty() ? EmiStack.EMPTY : recipe.getOutputs().get(0), recipe);
		}

		@Override
		public void render(GuiGraphics raw, int x, int y, float delta, int flags) {
			super.render(raw, x, y, delta, flags & (~EmiIngredient.RENDER_INGREDIENT));
		}
	}

	public static class Synthetic extends EmiFavorite {
		public final long batches;
		public final long amount;
		public final int state;
		public final long total;

		public Synthetic(EmiRecipe recipe, long batches, long amount, long total, int state) {
			super(recipe.getOutputs().get(0), recipe);
			this.batches = batches;
			this.amount = amount;
			this.total = total;
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
		public void render(GuiGraphics raw, int x, int y, float delta, int flags) {
			EmiDrawContext context = EmiDrawContext.wrap(raw);
			int color = 0x915900; // Orange
			if (state == 1) {
				color = 0x790091; // Magenta
			} else if (state == 2) {
				color = 0x00918e; // Blue
			} else if (state == -1) {
				color = 0x911300; // Red
			}
			//context.fill(x - 1, y - 1, 18, 18, 0x44000000 | color);
			stack.render(context.raw(), x, y, delta, flags & (~EmiIngredient.RENDER_AMOUNT));
			MicroTextRenderer.render(context, amount, stack.getEmiStacks().get(0) instanceof FluidEmiStack, 18, x + 17, y + 17, color);
		}

		@Override
		public List<ClientTooltipComponent> getTooltip() {
			List<ClientTooltipComponent> list = Lists.newArrayList();
			list.addAll(super.getTooltip());

			long diff = total - amount;
			list.add(EmiTooltipComponents.of(EmiPort.translatable("tooltip.emi.synfav.remaining", EmiRenderHelper.getAmountText(stack, amount)).withStyle(ChatFormatting.GRAY)));
			list.add(EmiTooltipComponents.of(EmiPort.translatable("tooltip.emi.synfav.obtained", EmiRenderHelper.getAmountText(stack, diff), EmiRenderHelper.getAmountText(stack, total)).withStyle(ChatFormatting.GRAY)));
			if (batches != amount) {
				list.add(EmiTooltipComponents.of(EmiPort.translatable("tooltip.emi.synfav.batches_remaining", batches).withStyle(ChatFormatting.GRAY)));
			}

			if (state == -1) {
				return list;
			}
			
			Component craftKey = null;

			if (EmiConfig.helpLevel.has(HelpLevel.NORMAL) && EmiRecipeFiller.getFirstValidHandler(recipe, EmiApi.getHandledScreen()) != null) {
				if (EmiConfig.craftAllToInventory.isBound()) {
					craftKey = EmiConfig.craftAllToInventory.getBindText();
				} else if (EmiConfig.craftAll.isBound()) {
					craftKey = EmiConfig.craftAll.getBindText();
				}
			}
			if (state == 0) {
				list.add(ClientTooltipComponent.create(EmiPort.translatable("tooltip.emi.synfav.uncraftable").getVisualOrderText()));
			} else if (state == 1) {
				list.add(ClientTooltipComponent.create(EmiPort.translatable("tooltip.emi.synfav.partially_craftable").getVisualOrderText()));
				if (craftKey != null) {
					list.add(ClientTooltipComponent.create(EmiPort.translatable("tooltip.emi.synfav.craft_some", craftKey).getVisualOrderText()));
				}
			} else if (state == 2) {
				list.add(ClientTooltipComponent.create(EmiPort.translatable("tooltip.emi.synfav.fully_craftable", batches).getVisualOrderText()));
				if (craftKey != null) {
					list.add(ClientTooltipComponent.create(EmiPort.translatable("tooltip.emi.synfav.craft_all", craftKey, batches).getVisualOrderText()));
				}
			}
			return list;
		}

		@Override
		public boolean isUnbatchable() {
			return true;
		}
	}
}
