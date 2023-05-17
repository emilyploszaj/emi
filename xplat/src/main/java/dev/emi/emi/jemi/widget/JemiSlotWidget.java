package dev.emi.emi.jemi.widget;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.jemi.JemiStack;
import dev.emi.emi.jemi.JemiUtil;
import dev.emi.emi.jemi.impl.JemiIngredientAcceptor;
import dev.emi.emi.jemi.impl.JemiRecipeSlot;
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
		IIngredientRenderer<?> renderer = getRenderer();
		if (renderer != null) {
			this.customBackground(null, 0, 0, renderer.getWidth() + 2, renderer.getHeight() + 2);
		}
	}

	private ITypedIngredient<?> getIngredient() {
		if (slot.renderers != null) {
			Optional<ITypedIngredient<?>> opt = JemiUtil.getTyped(getStack().getEmiStacks().get(0));
			if (opt.isPresent()) {
				return opt.get();
			}
		}
		return null;
	}

	private IIngredientRenderer<?> getRenderer() {
		ITypedIngredient<?> typed = getIngredient();
		if (typed != null) {
			if (slot.renderers.containsKey(typed.getType())) {
				return slot.renderers.get(typed.getType()).renderer();
			}
		}
		return null;
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
		IIngredientRenderer renderer = getRenderer();
		if (renderer != null) {
			ITypedIngredient<?> typed = getIngredient();
			Bounds bounds = getBounds();
			int xOff = bounds.x() + (bounds.width() - 16) / 2 + (16 - renderer.getWidth()) / 2;
			int yOff = bounds.y() + (bounds.height() - 16) / 2 + (16 - renderer.getHeight()) / 2;
			RenderSystem.enableBlend();
			matrices.push();
			matrices.translate(xOff, yOff, 0);
			renderer.render(matrices, typed.getIngredient());
			matrices.pop();
			return;
		}
		super.drawStack(matrices, mouseX, mouseY, delta);
	}

	@Override
	public void drawOverlay(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		if (slot.overlay != null) {
			RenderSystem.enableBlend();
			matrices.push();
			matrices.translate(0, 0, 200);
			slot.overlay.drawable().draw(matrices, x + 1 + slot.overlay.xOff(), y + 1 + slot.overlay.yOff());
			matrices.pop();
		}
		super.drawOverlay(matrices, mouseX, mouseY, delta);
	}

	@SuppressWarnings("unchecked")
	public static void addTooltip(List<TooltipComponent> list, JemiRecipeSlot slot, EmiIngredient stack, IIngredientRenderer<?> renderer) {
		if (renderer != null) {
			if (stack.getEmiStacks().size() == 1 && stack.getEmiStacks().get(0) instanceof JemiStack js) {
				js = js.copy();
				js.renderer = renderer;
				stack = js;
			}
		}
		list.addAll(stack.getTooltip());
		if (slot.tooltipCallback != null) {
			try {
				List<Text> event = Lists.newArrayList();
				List<Text> original = stack.getEmiStacks().get(0).getTooltipText();
				Set<Text> toRemove = original.stream().collect(Collectors.toSet());
				event.addAll(stack.getEmiStacks().get(0).getTooltipText());
				slot.tooltipCallback.onTooltip(slot, event);
				int index = Math.min(list.size(), 1);
				if (!event.isEmpty()) {
					list.addAll(index, event.stream().filter(t -> !toRemove.contains(t) && !JemiIngredientAcceptor.FLUID_END.matcher(t.getString()).find()).map(t -> TooltipComponent.of(t.asOrderedText())).toList());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public List<TooltipComponent> getTooltip(int mouseX, int mouseY) {
		List<TooltipComponent> list = Lists.newArrayList();
		if (getStack().isEmpty()) {
			return List.of();
		}
		addTooltip(list, slot, getStack(), getRenderer());
		addSlotTooltip(list);
		return list;
	}
}
