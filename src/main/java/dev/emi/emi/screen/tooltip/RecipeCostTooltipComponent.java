package dev.emi.emi.screen.tooltip;

import java.util.List;
import java.util.stream.Stream;

import org.joml.Matrix4f;

import com.google.common.collect.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.EmiStackList;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.bom.ChanceMaterialCost;
import dev.emi.emi.bom.FlatMaterialCost;
import dev.emi.emi.bom.MaterialTree;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Matrix4f;

public class RecipeCostTooltipComponent implements TooltipComponent {
	private static final Text COST = EmiPort.translatable("emi.cost_per");
	private final List<Node> nodes = Lists.newArrayList();
	public final MaterialTree tree;
	private int maxWidth = 0;

	public RecipeCostTooltipComponent(EmiRecipe recipe) {
		tree = new MaterialTree(recipe);
		tree.batches = tree.cost.getIdealBatch(tree.goal, 1);
		tree.calculateCost();
		addNodes();
	}

	public boolean shouldDisplay() {
		return !nodes.isEmpty();
	}

	public void addNodes() {
		double batches = tree.batches;
		List<FlatMaterialCost> costs = Stream.concat(
			tree.cost.costs.values().stream(),
			tree.cost.chanceCosts.values().stream()
		).sorted((a, b) -> Integer.compare(
			EmiStackList.indices.getOrDefault(a.ingredient.getEmiStacks().get(0), Integer.MAX_VALUE),
			EmiStackList.indices.getOrDefault(b.ingredient.getEmiStacks().get(0), Integer.MAX_VALUE)
		)).toList();
		for (FlatMaterialCost cost : costs) {
			if (cost instanceof ChanceMaterialCost cmc) {
				nodes.add(new Node(cost.ingredient, cost.amount / batches * cmc.chance, true));
			} else {
				nodes.add(new Node(cost.ingredient, cost.amount / batches, false));
			}
		}
		positionNodes();
	}

	public void positionNodes() {
		int wrapWidth = getWrapWidth();
		int padding = 8;
		int x = 0;
		int y = 10;
		maxWidth = 0;
		for (Node node : nodes) {
			int width = 16 + EmiRenderHelper.getAmountOverflow(node.text);
			if (x + width > wrapWidth) {
				x = 0;
				y += 18;
			}
			maxWidth = Math.max(maxWidth, x + width);
			node.x = x;
			node.y = y;
			x += width + padding;
		}
	}

	public int getWrapWidth() {
		return 160;
	}

	@Override
	public int getHeight() {
		if (!nodes.isEmpty()) {
			return nodes.get(nodes.size() - 1).y + 18;
		}
		return 10;
	}

	@Override
	public int getWidth(TextRenderer textRenderer) {
		return Math.max(textRenderer.getWidth(COST), maxWidth);
	}
	
	@Override
	public void drawItems(TextRenderer textRenderer, int x, int y, MatrixStack matrices, ItemRenderer itemRenderer, int z) {
		matrices.push();
		matrices.translate(x, y, z);
		for (Node node : nodes) {
			node.stack.render(matrices, node.x, node.y, MinecraftClient.getInstance().getTickDelta());
			EmiRenderHelper.renderAmount(matrices, node.x, node.y, node.text);
		}
		matrices.pop();
	}

	@Override
	public void drawText(TextRenderer textRenderer, int x, int y, Matrix4f matrix, Immediate vertexConsumers) {
		textRenderer.draw(COST, x, y, Formatting.GRAY.getColorValue(), true, matrix, vertexConsumers, false, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
	}

	private static class Node {
		public final EmiIngredient stack;
		public final Text text;
		public int x, y;

		public Node(EmiIngredient stack, double amount, boolean chanced) {
			this.stack = stack;
			if (chanced) {
				text = EmiPort.append(EmiPort.literal("≈"), stack.getAmountText(amount)).formatted(Formatting.GOLD);
			} else {
				text = stack.getAmountText(amount);
			}
		}
	}
}
