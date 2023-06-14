package dev.emi.emi.jemi.widget;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.widget.TankWidget;
import dev.emi.emi.jemi.impl.JemiRecipeSlot;
import dev.emi.emi.runtime.EmiDrawContext;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;

public class JemiTankWidget extends TankWidget {
	private final JemiRecipeSlot slot;
	private final JemiSlotWidget jsw;

	public JemiTankWidget(JemiRecipeSlot slot, EmiRecipe recipe) {
		super(slot.stack, slot.x - 1, slot.y - 1, slot.tankInfo.width() + 2, slot.tankInfo.height() + 2, slot.tankInfo.capacity());
		this.slot = slot;
		slot.widget = this;
		if (slot.getRole() == RecipeIngredientRole.OUTPUT) {
			this.recipeContext(recipe);
		}
		this.drawBack(false);
		this.jsw = new JemiSlotWidget(slot, recipe);
	}

	@Override
	public void render(MatrixStack raw, int mouseX, int mouseY, float delta) {
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		if (slot.background != null) {
			slot.background.drawable().draw(context.raw(), x + 1 + slot.background.xOff(), y + 1 + slot.background.yOff());
		}
		super.render(context.raw(), mouseX, mouseY, delta);
	}

	@Override
	public void drawOverlay(MatrixStack raw, int mouseX, int mouseY, float delta) {
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		if (slot.overlay != null) {
			RenderSystem.enableBlend();
			context.push();
			context.matrices().translate(0, 0, 200);
			slot.overlay.drawable().draw(context.raw(), x + 1 + slot.overlay.xOff(), y + 1 + slot.overlay.yOff());
			context.pop();
		}
		super.drawOverlay(context.raw(), mouseX, mouseY, delta);
	}

	@Override
	public List<TooltipComponent> getTooltip(int mouseX, int mouseY) {
		return jsw.getTooltip(mouseX, mouseY);
	}
}
