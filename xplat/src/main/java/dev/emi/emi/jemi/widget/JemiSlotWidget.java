package dev.emi.emi.jemi.widget;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.jemi.JemiUtil;
import dev.emi.emi.jemi.impl.JemiRecipeSlot;
import dev.emi.emi.screen.FakeScreen;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class JemiSlotWidget extends SlotWidget {
	private final JemiRecipeSlot slot;

	public JemiSlotWidget(JemiRecipeSlot slot, EmiRecipe recipe) {
		super(slot.stack, slot.x - 1, slot.y - 1);
		this.slot = slot;
		slot.widget = this;
		if (slot.getRole() == RecipeIngredientRole.OUTPUT) {
			this.recipeContext(recipe);
		}
		this.drawBack(false);
		if (slot.renderers != null) {
			Optional<ITypedIngredient<?>> opt = JemiUtil.getTyped(getStack().getEmiStacks().get(0));
			if (opt.isPresent()) {
				ITypedIngredient<?> typed = opt.get();
				if (slot.renderers.containsKey(typed.getType())) {
					IIngredientRenderer<?> renderer = slot.renderers.get(typed.getType()).renderer();
					this.customBackground(null, 0, 0, renderer.getWidth() + 2, renderer.getHeight() + 2);
				}
			}
		}
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		if (slot.background != null) {
			slot.background.drawable().draw(matrices, x + 1 + slot.background.xOff(), y + 1 + slot.background.yOff());
		}
		super.render(matrices, mouseX, mouseY, delta);
	}

	@Override
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void drawStack(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		if (slot.renderers != null) {
			Optional<ITypedIngredient<?>> opt = JemiUtil.getTyped(getStack().getEmiStacks().get(0));
			if (opt.isPresent()) {
				ITypedIngredient<?> typed = opt.get();
				if (slot.renderers.containsKey(typed.getType())) {
					Bounds bounds = getBounds();
					IIngredientRenderer renderer = slot.renderers.get(typed.getType()).renderer();
					int xOff = bounds.x() + (bounds.width() - 16) / 2 + (16 - renderer.getWidth()) / 2;
					int yOff = bounds.y() + (bounds.height() - 16) / 2 + (16 - renderer.getHeight()) / 2;
					matrices.push();
					matrices.translate(xOff, yOff, 0);
					renderer.render(matrices, typed.getIngredient());
					matrices.pop();
					return;
				}
			}
		}
		super.drawStack(matrices, mouseX, mouseY, delta);
	}

	@Override
	public void drawOverlay(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		if (slot.overlay != null) {
			slot.overlay.drawable().draw(matrices, x + 1 + slot.overlay.xOff(), y + 1 + slot.overlay.yOff());
		}
		super.drawOverlay(matrices, mouseX, mouseY, delta);
	}

	public static void addTooltip(List<TooltipComponent> list, JemiRecipeSlot slot, EmiStack stack) {
		if (slot.tooltipCallback != null) {
			try {
				List<Text> event = Lists.newArrayList();
				Set<Text> toRemove;
				if (!stack.getItemStack().isEmpty()) {
					List<Text> text = FakeScreen.INSTANCE.getTooltipFromItem(stack.getItemStack());
					event.addAll(text);
					toRemove = text.stream().collect(Collectors.toSet());
				} else {
					toRemove = Set.of();
				}
				slot.tooltipCallback.onTooltip(slot, event);
				List<TooltipComponent> orig = stack.getTooltip();
				int index = Math.min(list.size(), orig.size());
				list.addAll(index, event.stream().filter(t -> !toRemove.contains(t)).map(t -> TooltipComponent.of(t.asOrderedText())).toList());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public List<TooltipComponent> getTooltip(int mouseX, int mouseY) {
		List<TooltipComponent> list = Lists.newArrayList(super.getTooltip(mouseX, mouseY));
		addTooltip(list, slot, getStack().getEmiStacks().get(0));
		return list;
	}
}
