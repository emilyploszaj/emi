package dev.emi.emi.screen.tooltip;

import java.util.List;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.stack.EmiIngredient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;

public class IngredientTooltipComponent implements TooltipComponent {
	private final List<? extends EmiIngredient> ingredients;
	
	public IngredientTooltipComponent(List<? extends EmiIngredient> ingredients) {
		this.ingredients = ingredients;
	}

	@Override
	public int getHeight() {
		return ((ingredients.size() - 1) / 8 + 1) * 18;
	}

	@Override
	public int getWidth(TextRenderer textRenderer) {
		return 18 * 8;
	}
	
	@Override
	public void drawItems(TextRenderer textRenderer, int x, int y, MatrixStack matrices, ItemRenderer itemRenderer, int z) {
		matrices.push();
		matrices.translate(0, 0, z);
		for (int i = 0; i < ingredients.size(); i++) {
			EmiIngredient ingredient = ingredients.get(i);
			ingredient.render(matrices, x + i % 8 * 18, y + i / 8 * 18, MinecraftClient.getInstance().getTickDelta());
			if (!ingredient.isEmpty()) {
				EmiRenderHelper.renderTag(ingredient, matrices, x + i % 8 * 18, y + i / 8 * 18);
				EmiRenderHelper.renderRemainder(ingredient, matrices, x + i % 8 * 18, y + i / 8 * 18);
			}
		}
		matrices.pop();
	}
}
