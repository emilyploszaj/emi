package dev.emi.emi.jemi.widget;

import java.util.List;

import com.google.common.collect.Lists;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.widget.TankWidget;
import dev.emi.emi.jemi.impl.JemiRecipeSlot;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;

public class JemiTankWidget extends TankWidget {
	private final JemiRecipeSlot slot;

	public JemiTankWidget(JemiRecipeSlot slot, EmiRecipe recipe) {
		super(slot.stack, slot.x - 1, slot.y - 1, slot.tankInfo.width() + 2, slot.tankInfo.height() + 2, slot.tankInfo.capacity());
		this.slot = slot;
		slot.widget = this;
		if (slot.getRole() == RecipeIngredientRole.OUTPUT) {
			this.recipeContext(recipe);
		}
		this.drawBack(false);
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		if (slot.background != null) {
			slot.background.drawable().draw(matrices, x + 1 + slot.background.xOff(), y + 1 + slot.background.yOff());
		}
		super.render(matrices, mouseX, mouseY, delta);
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
		JemiSlotWidget.addTooltip(list, slot, getStack().getEmiStacks().get(0));
		return list;
	}
}
