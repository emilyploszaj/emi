package dev.emi.emi.screen.tooltip;

import java.text.DecimalFormat;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.bom.MaterialTree;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.Matrix4f;

public class RecipeCostTooltipComponent implements TooltipComponent {
	private static final TranslatableText COST = new TranslatableText("emi.cost_per");
	private final MaterialTree tree;

	public RecipeCostTooltipComponent(EmiRecipe recipe) {
		tree = new MaterialTree(recipe);
		tree.calculateCost(true);
	}

	@Override
	public int getHeight() {
		return 10 + ((tree.fractionalCosts.size() - 1) / 4 + 1) * 18;
	}

	@Override
	public int getWidth(TextRenderer textRenderer) {
		return Math.max(textRenderer.getWidth(COST), Math.min(4, tree.fractionalCosts.size()) * 24);
	}
	
	@Override
	public void drawItems(TextRenderer textRenderer, int x, int y, MatrixStack matrices, ItemRenderer itemRenderer, int z) {
		MatrixStack view = RenderSystem.getModelViewStack();
		view.push();
		view.translate(0, 0, z);
		RenderSystem.applyModelViewMatrix();
		DecimalFormat format = new DecimalFormat("0.##");
		for (int i = 0; i < tree.fractionalCosts.size(); i++) {
			int ix = x + i % 4 * 24;
			int iy = y + 10 + i / 4 * 18;
			tree.fractionalCosts.get(i).ingredient.render(matrices, ix, iy, MinecraftClient.getInstance().getTickDelta());
			matrices.push();
			// This terrifies me, I'd like to do something else
			matrices.translate(0, 0, 590);
			RenderSystem.disableDepthTest();
			float amount = tree.fractionalCosts.get(i).amount;
			String s = format.format(amount);
			textRenderer.drawWithShadow(matrices, s, ix + 17 - Math.min(10, textRenderer.getWidth(s)), iy + 9, -1);
			matrices.pop();
		}
		view.pop();
		RenderSystem.applyModelViewMatrix();
	}

	@Override
	public void drawText(TextRenderer textRenderer, int x, int y, Matrix4f matrix, Immediate vertexConsumers) {
		textRenderer.draw(COST, x, y, 0xffffff, true, matrix, vertexConsumers, false, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
	}
}
