package dev.emi.emi.screen.tooltip;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import org.apache.commons.compress.utils.Lists;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.widget.Widget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class RecipeTooltipComponent implements TooltipComponent {
	private static final Identifier TEXTURE = new Identifier("emi", "textures/gui/background.png");
	private final EmiRecipe recipe;

	public RecipeTooltipComponent(EmiRecipe recipe) {
		this.recipe = recipe;
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
		matrices.push();
		matrices.translate(0, 0, z);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		RenderSystem.setShaderTexture(0, TEXTURE);
		EmiRenderHelper.drawNinePatch(matrices, x, y, getWidth(textRenderer), getHeight(), 0, 0, 4, 1);
		List<Widget> widgets = Lists.newArrayList();
		recipe.addWidgets(widgets, x + 4, y + 4);
		for (Widget widget : widgets) {
			widget.render(matrices, -1000, -1000, MinecraftClient.getInstance().getTickDelta());
		}
		matrices.pop();
	}
}
