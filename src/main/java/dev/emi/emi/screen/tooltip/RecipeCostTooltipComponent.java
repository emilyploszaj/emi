package dev.emi.emi.screen.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.bom.FractionalMaterialCost;
import dev.emi.emi.bom.MaterialTree;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.joml.Matrix4f;

public class RecipeCostTooltipComponent implements TooltipComponent {
	private static final Text COST = EmiPort.translatable("emi.cost_per");
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
		int max = textRenderer.getWidth(COST);
		int c = 0;
		int cx = 0;
		for (int i = 0; i < tree.fractionalCosts.size(); i++) {
			FractionalMaterialCost cost = tree.fractionalCosts.get(i);
			Text amount = cost.ingredient.getAmountText(cost.amount);
			int ew = EmiRenderHelper.getAmountOverflow(amount);
			if (c++ > 3) {
				c = 0;
				cx = 0;
			}
			cx += ew + 24;
			max = Math.max(max, cx - 6);
		}
		return max;
	}
	
	@Override
	public void drawItems(TextRenderer textRenderer, int x, int y, MatrixStack matrices, ItemRenderer itemRenderer, int z) {
		MatrixStack view = RenderSystem.getModelViewStack();
		view.push();
		view.translate(0, 0, z);
		RenderSystem.applyModelViewMatrix();
		int c = 0;
		int cx = 0;
		int cy = 10;
		for (int i = 0; i < tree.fractionalCosts.size(); i++) {
			FractionalMaterialCost cost = tree.fractionalCosts.get(i);
			Text amount = cost.ingredient.getAmountText(cost.amount);
			int ew = EmiRenderHelper.getAmountOverflow(amount);
			if (c++ > 3) {
				c = 0;
				cx = 0;
				cy += 18;
			}
			int ix = x + cx;
			int iy = y + cy;
			cost.ingredient.render(matrices, ix, iy, MinecraftClient.getInstance().getTickDelta());
			matrices.push();
			// This terrifies me, I'd like to do something else
			matrices.translate(0, 0, 390);
			EmiRenderHelper.renderAmount(matrices, ix, iy, amount);
			matrices.pop();
			cx += 24 + ew;
		}
		view.pop();
		RenderSystem.applyModelViewMatrix();
	}

	@Override
	public void drawText(TextRenderer textRenderer, int x, int y, Matrix4f matrix, Immediate vertexConsumers) {
		textRenderer.draw(COST, x, y, 0xffffff, true, matrix, vertexConsumers, false, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
	}
}
