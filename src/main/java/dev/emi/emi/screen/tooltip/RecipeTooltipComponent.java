package dev.emi.emi.screen.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.recipe.EmiRecipe;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;

public class RecipeTooltipComponent implements TooltipComponent {
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
	public int getWidth(TextRenderer var1) {
		return width;
	}

	@Override
	public void drawItems(TextRenderer textRenderer, int x, int y, MatrixStack matrices, ItemRenderer itemRenderer, int z) {
		MatrixStack view = RenderSystem.getModelViewStack();
		view.push();
		view.translate(0, 0, z);
		RenderSystem.applyModelViewMatrix();
		EmiRenderHelper.renderRecipe(recipe, matrices, x, y, showMissing, overlayColor);
		view.pop();
		RenderSystem.applyModelViewMatrix();
	}
}
