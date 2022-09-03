package dev.emi.emi.screen.tooltip;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import org.apache.commons.compress.utils.Lists;

import dev.emi.emi.EmiClient;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class RecipeTooltipComponent implements TooltipComponent {
	private static final Identifier TEXTURE = new Identifier("emi", "textures/gui/background.png");
	private final EmiRecipe recipe;
	private final boolean showMissing;
	private int overlayColor = -1;

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
	}

	@Override
	public int getHeight() {
		return recipe.getDisplayHeight() + 8;
	}

	@Override
	public int getWidth(TextRenderer var1) {
		return recipe.getDisplayWidth() + 8;
	}
	
	@Override
	public void drawItems(TextRenderer textRenderer, int x, int y, MatrixStack matrices, ItemRenderer itemRenderer, int z) {
		MatrixStack view = RenderSystem.getModelViewStack();
		itemRenderer.zOffset -= z;
		view.push();
		view.translate(0, 0, z);
		RenderSystem.applyModelViewMatrix();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		RenderSystem.setShaderTexture(0, TEXTURE);
		EmiRenderHelper.drawNinePatch(matrices, x, y, getWidth(textRenderer), getHeight(), 0, 0, 4, 1);
		List<Widget> widgets = Lists.newArrayList();
		WidgetHolder holder = new WidgetHolder() {

			public int getWidth() {
				return recipe.getDisplayWidth();
			}

			public int getHeight() {
				return recipe.getDisplayHeight();
			}

			public <T extends Widget> T add(T widget) {
				widgets.add(widget);
				return widget;
			}
		};
		view.translate(x + 4, y + 4, 0);
		RenderSystem.applyModelViewMatrix();
		recipe.addWidgets(holder);
		if (showMissing) {
			EmiClient.getAvailable(recipe);
		}
		float delta = MinecraftClient.getInstance().getTickDelta();
		for (Widget widget : widgets) {
			widget.render(matrices, -1000, -1000, delta);
		}
		if (overlayColor != -1) {
			DrawableHelper.fill(matrices, -1, -1, recipe.getDisplayWidth() + 1, recipe.getDisplayHeight() + 1, overlayColor);
		}
		EmiClient.availableForCrafting.clear();
		view.pop();
		RenderSystem.applyModelViewMatrix();
		itemRenderer.zOffset += z;
	}
}
