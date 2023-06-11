package dev.emi.emi.screen.tooltip;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.font.TextRenderer;

public class RecipeTooltipComponent implements EmiTooltipComponent {
	private final EmiRecipe recipe;
	private final boolean showMissing;
	private int overlayColor = -1;
	private int width = 0, height = 0;

	public RecipeTooltipComponent(EmiRecipe recipe) {
		this(recipe, false);
	}

	public RecipeTooltipComponent(EmiRecipe recipe, int overlayColor) {
		this(recipe, false);
		this.overlayColor = overlayColor;
	}

	public RecipeTooltipComponent(EmiRecipe recipe, boolean showMissing) {
		this.recipe = recipe;
		this.showMissing = showMissing;
		try {
			width = recipe.getDisplayWidth() + 8;
			height = recipe.getDisplayHeight() + 8 + 2;
		} catch (Exception e) {
		}
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public int getWidth(TextRenderer textRenderer) {
		return width;
	}

	@Override
	public void drawTooltip(EmiDrawContext context, TooltipRenderData render) {
		EmiRenderHelper.renderRecipe(recipe, context, 0, 0, showMissing, overlayColor);
	}
}
