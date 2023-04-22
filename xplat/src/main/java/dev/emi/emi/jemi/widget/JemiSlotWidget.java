package dev.emi.emi.jemi.widget;

import java.util.List;

import com.google.common.collect.Lists;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.jemi.impl.JemiRecipeSlot;
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
	}

	@Override
	public void drawOverlay(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		if (slot.overlay != null) {
			slot.overlay.drawable().draw(matrices, x + 1 + slot.overlay.xOff(), y + 1 + slot.overlay.yOff());
		}
		super.drawOverlay(matrices, mouseX, mouseY, delta);
	}

	@Override
	public List<TooltipComponent> getTooltip(int mouseX, int mouseY) {
		List<TooltipComponent> list = Lists.newArrayList(super.getTooltip(mouseX, mouseY));
		if (slot.tooltipCallback != null) {
			try {
				List<Text> event = Lists.newArrayList();
				slot.tooltipCallback.onTooltip(slot, event);
				list.addAll(event.stream().map(t -> TooltipComponent.of(t.asOrderedText())).toList());
			} catch (Exception e) {
			}
		}
		return list;
	}
}
