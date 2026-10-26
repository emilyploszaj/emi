package dev.emi.emi.jemi.widget;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.jemi.JemiStack;
import dev.emi.emi.jemi.JemiUtil;
import dev.emi.emi.jemi.impl.JemiRecipeSlot;
import dev.emi.emi.jemi.impl.JemiTooltipBuilder;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.runtime.EmiLog;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

public class JemiSlotWidget extends SlotWidget {
	public final JemiRecipeSlot slot;

	public JemiSlotWidget(JemiRecipeSlot slot, EmiRecipe recipe) {
		super(slot.stack, slot.x - (slot.large ? 6 : 1) , slot.y - (slot.large ? 6 : 1));
		this.slot = slot;
		slot.widget = this;
		if (slot.getRole() == RecipeIngredientRole.OUTPUT) {
			this.recipeContext(recipe);
		}
		this.drawBack(slot.defaultBackground);
		IIngredientRenderer<?> renderer = getRenderer();
		if (renderer != null) {
			this.customBackground(null, 0, 0, renderer.getWidth() + 2, renderer.getHeight() + 2);
		} else if (slot.large) {
			this.large(true);
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
	public void render(GuiGraphicsExtractor raw, int mouseX, int mouseY, float delta) {
		if (slot.background != null) {
			slot.background.drawable().draw(raw, x + 1 + slot.background.xOff(), y + 1 + slot.background.yOff());
		}
		super.render(raw, mouseX, mouseY, delta);
	}

	@Override
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void drawStack(GuiGraphicsExtractor raw, int mouseX, int mouseY, float delta) {
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		IIngredientRenderer renderer = getRenderer();
		if (renderer != null) {
			ITypedIngredient<?> typed = getIngredient();
			Bounds bounds = getBounds();
			int xOff = bounds.x() + (bounds.width() - 16) / 2 + (16 - renderer.getWidth()) / 2;
			int yOff = bounds.y() + (bounds.height() - 16) / 2 + (16 - renderer.getHeight()) / 2;
			context.push();
			context.translate(xOff, yOff);
			renderer.render(context.raw(), typed.getIngredient());
			context.pop();
			return;
		}
		super.drawStack(context.raw(), mouseX, mouseY, delta);
	}

	@Override
	public void drawOverlay(GuiGraphicsExtractor raw, int mouseX, int mouseY, float delta) {
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		if (slot.overlay != null) {
			context.push();
			slot.overlay.drawable().draw(context.raw(), x + 1 + slot.overlay.xOff(), y + 1 + slot.overlay.yOff());
			context.pop();
		}
		super.drawOverlay(context.raw(), mouseX, mouseY, delta);
	}

	@SuppressWarnings("unchecked")
	public static void addTooltip(List<ClientTooltipComponent> list, JemiRecipeSlot slot, EmiIngredient stack, IIngredientRenderer<?> renderer) {
		if (renderer != null) {
			if (stack.getEmiStacks().size() == 1 && stack.getEmiStacks().get(0) instanceof JemiStack js) {
				js = js.copy();
				js.renderer = renderer;
				stack = js;
			}
		}
		list.addAll(stack.getTooltip());
		if (slot.richTooltipCallback != null) {
			try {
				JemiTooltipBuilder tooltipBuilder = new JemiTooltipBuilder();
				slot.richTooltipCallback.onRichTooltip(slot, tooltipBuilder);
				list.addAll(tooltipBuilder.getTooltip());
			} catch (Exception e) {
				EmiLog.error("Error initializing JEI TooltipBuilder", e);
			}
		}
	}

	@Override
	public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
		List<ClientTooltipComponent> list = Lists.newArrayList();
		if (getStack().isEmpty()) {
			return List.of();
		}
		addTooltip(list, slot, getStack(), getRenderer());
		addSlotTooltip(list);
		return list;
	}
}
