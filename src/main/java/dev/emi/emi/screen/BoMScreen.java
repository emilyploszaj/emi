package dev.emi.emi.screen;

import java.util.List;
import java.util.function.Function;

import com.mojang.blaze3d.systems.RenderSystem;

import org.apache.commons.compress.utils.Lists;
import org.lwjgl.glfw.GLFW;

import dev.emi.emi.EmiConfig;
import dev.emi.emi.EmiHistory;
import dev.emi.emi.EmiRecipes;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.EmiResolutionRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.bind.EmiBind;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.bom.FlatMaterialCost;
import dev.emi.emi.bom.MaterialNode;
import dev.emi.emi.mixin.accessor.ScreenAccessor;
import dev.emi.emi.screen.tooltip.RecipeTooltipComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;

public class BoMScreen extends Screen {
	private static final int NODE_WIDTH = 30;
	private static final int NODE_HORIZONTAL_SPACING = 20;
	private static final int NODE_VERTICAL_SPACING = 20;
	private static int zoom = 0;
	private double offX, offY;
	private List<Node> nodes = Lists.newArrayList();
	public HandledScreen<?> old;
	private int nodeWidth = 0;
	private int nodeHeight = 0;

	public BoMScreen(HandledScreen<?> old) {
		super(new TranslatableText("screen.emi.bom"));
		this.old = old;
	}

	public void init() {
		if (BoM.tree != null) {
			offY = height / -3;
		} else {
			offY = 0;
		}
		if (BoM.tree != null) {
			TreeVolume volume = addNewNodes(BoM.tree.goal, 1, 1, 0);
			nodes = volume.nodes;
			int horizontalOffset = (volume.getMaxRight() + volume.getMinLeft()) / 2;
			for (Node node : volume.nodes) {
				node.x -= horizontalOffset;
			}

			nodeWidth = volume.getMaxRight() - volume.getMinLeft();
			nodeHeight = getNodeHeight(BoM.tree.goal);
		} else {
			nodes = Lists.newArrayList();
		}
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		float scale = getScale();
		int scaledWidth = (int) (width / scale);
		int scaledHeight = (int) (height / scale);
		// TODO should be the ingredient width if higher
		int contentWidth = nodeWidth * NODE_WIDTH;
		int contentHeight = nodeHeight * NODE_VERTICAL_SPACING + 80;
		int xBound = scaledWidth / 2 + contentWidth - 100;
		int topBound = scaledHeight * 1 / -2 + 20;
		int bottomBound = contentHeight + scaledHeight / 2 - 20;
		offX = MathHelper.clamp(offX, -xBound, xBound);
		offY = MathHelper.clamp(offY, -bottomBound, -topBound);

		MatrixStack viewMatrices = RenderSystem.getModelViewStack();
		viewMatrices.push();
		viewMatrices.translate(width / 2, height / 2, 0);
		viewMatrices.scale(scale, scale, 1);
		viewMatrices.translate(offX, offY, 0);
		RenderSystem.applyModelViewMatrix();
		if (BoM.tree != null) {
			BoM.tree.calculateCost(false);
			int cx = (BoM.tree.costs.size() * 40) / -2 + 10;
			int cy = nodeHeight * NODE_VERTICAL_SPACING * 2 + 10;
			DrawableHelper.drawCenteredText(matrices, textRenderer, new TranslatableText("emi.total_cost"), 0, cy - 16, -1);
			for (FlatMaterialCost cost : BoM.tree.costs) {
				cost.ingredient.render(matrices, cx, cy, 0);
				textRenderer.drawWithShadow(matrices, "" + cost.amount, cx + 18, cy, -1);
				cx += 40;
			}
			if (!BoM.tree.remainders.isEmpty()) {
				cx = (BoM.tree.remainders.size() * 40) / -2 + 10;
				cy += 40;
				DrawableHelper.drawCenteredText(matrices, textRenderer, new TranslatableText("emi.leftovers"), 0, cy - 16, -1);
				for (FlatMaterialCost cost : BoM.tree.remainders.values()) {
					cost.ingredient.render(matrices, cx, cy, 0);
					textRenderer.drawWithShadow(matrices, "" + cost.amount, cx + 18, cy, -1);
					cx += 40;
				}
			}

			for (Node node : nodes) {
				node.render(matrices, textRenderer, delta);
			}
		} else {
			drawCenteredText(matrices, textRenderer, new TranslatableText("emi.tree_welcome", EmiRenderHelper.getEmiText()), 0, -72, -1);
			drawCenteredText(matrices, textRenderer, new TranslatableText("emi.no_tree"), 0, -48, -1);
			drawCenteredText(matrices, textRenderer, new TranslatableText("emi.random_tree"), 0, -24, -1);
			drawCenteredText(matrices, textRenderer, new TranslatableText("emi.random_tree_input"), 0, 0, -1);
		}

		viewMatrices.pop();
		RenderSystem.applyModelViewMatrix();

		int mx = (int) ((mouseX - width / 2) / scale - offX);
		int my = (int) ((mouseY - height / 2) / scale - offY);
		for (Node node : nodes) {
			Hover hover = node.getHover(mx, my);
			if (hover != null && hover.drawTooltip(matrices, mouseX, mouseY)) {
				break;
			}
		}
	}

	public Hover getHoveredStack(int mx, int my) {
		float scale = getScale();
		mx = (int) ((mx - width / 2) / scale - offX);
		my = (int) ((my - height / 2) / scale - offY);
		for (Node node : nodes) {
			Hover hover = node.getHover(mx, my);
			if (hover != null) {
				return hover;
			}
		}
		return null;
	}

	public int getNodeHeight(MaterialNode node) {
		if (node.recipe != null) {
			int i = 1;
			for (MaterialNode n : node.children) {
				i = Math.max(i, getNodeHeight(n));
			}
			if (node.recipe instanceof EmiResolutionRecipe) {
				return i;
			}
			return i + 1;
		}
		return 1;
	}

	public TreeVolume addNewNodes(MaterialNode node, int multiplier, int divisor, int depth) {
		multiplier = node.amount * (int) Math.ceil(multiplier / (float) divisor);
		if (node.recipe != null && node.children.size() > 0) {
			if (node.recipe instanceof EmiResolutionRecipe) {
				TreeVolume volume = addNewNodes(node.children.get(0), multiplier, node.divisor, depth);
				volume.nodes.get(0).resolution = node;
				return volume;
			}
			TreeVolume volume = addNewNodes(node.children.get(0), multiplier, node.divisor, depth + 1);
			for (int i = 1; i < node.children.size(); i++) {
				volume.addToRight(addNewNodes(node.children.get(i), multiplier, node.divisor, depth + 1));
			}
			volume.addHead(node, multiplier, depth * NODE_VERTICAL_SPACING);
			return volume;
		}
		return new TreeVolume(node, multiplier, depth * NODE_VERTICAL_SPACING);
	}

	private static void drawLine(MatrixStack matrices, int x1, int y1, int x2, int y2) {
		if (x2 < x1) {
			drawLine(matrices, x2, y1, x1, y2);
			return;
		}
		if (y2 < y1) {
			drawLine(matrices, x1, y2, x2, y1);
			return;
		}
		fill(matrices, x1, y1, x2 + 1, y2 + 1, 0xFFFFFFFF);
		/*
		matrices.push();
		matrices.translate(x1, y1, 0);
		matrices.multiply(Vec3f.POSITIVE_Z.getRadialQuaternion((float) Math.atan2(y2 - y1, x2 - x1)));

		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		RenderSystem.disableTexture();
		BufferBuilder builder = Tessellator.getInstance().getBuffer();
		builder.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);

		float w = (float) Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y2 - y1, 2));
		float size = 0.5f;
		if (zoom < -3) {
			size = 1f;
			if (zoom < -4) {
				size = 2f;
			}
		} 

		Matrix4f mat = matrices.peek().getPositionMatrix();
		builder.vertex(mat, 0, -size, 1).color(1, 1, 1, 1f).next();
		builder.vertex(mat, 0, size, 1).color(1, 1, 1, 1f).next();
		builder.vertex(mat, w, size, 1).color(1, 1, 1, 1f).next();
		builder.vertex(mat, w, -size, 1).color(1, 1, 1, 1f).next();
		builder.end();
		BufferRenderer.draw(builder);
		matrices.pop();*/
	}

	public float getScale() {
		zoom = MathHelper.clamp(zoom, -5, 4);
		if (zoom == -5) {
			return 0.1f;
		}
		return 1 + zoom * 0.2f;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			this.close();
			return true;
		} else if (this.client.options.inventoryKey.matchesKey(keyCode, scanCode)) {
			this.close();
			return true;
		}
		Function<EmiBind, Boolean> function = bind -> bind.matchesKey(keyCode, scanCode);
		if (function.apply(EmiConfig.back)) {
			EmiHistory.pop();
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_R) {
			if (EmiRecipes.recipes.size() > 0) {
				for (int i = 0; i < 100_000; i++) {
					EmiRecipe recipe = EmiRecipes.recipes.get(client.world.getRandom().nextInt(EmiRecipes.recipes.size()));
					if (recipe.supportsRecipeTree()) {
						BoM.setGoal(recipe);
						init();
						return true;
					}
				}
			}
		} else if (keyCode == GLFW.GLFW_KEY_C) {
			BoM.tree = null;
			init();
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		Hover hover = getHoveredStack((int) mouseX, (int) mouseY);
		if (hover != null && hover.stack != null) {
			EmiApi.displayRecipes(hover.stack);
			if (hover.node != null) {
				RecipeScreen.resolve = hover.node;
				if (hover.node.recipe != null) {
					EmiApi.focusRecipe(hover.node.recipe);
				}
			}
			return true;
		}
		Function<EmiBind, Boolean> function = bind -> bind.matchesMouse(button);
		if (function.apply(EmiConfig.back)) {
			EmiHistory.pop();
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		zoom += (int) amount;
		return true;
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		float scale = getScale();
		offX += deltaX / scale;
		offY += deltaY / scale;
		return true;
		//return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public void close() {
		MinecraftClient.getInstance().setScreen(old);
	}

	private static class Hover {
		public EmiIngredient stack;
		public MaterialNode node;
		public EmiRecipeCategory category;

		public Hover(EmiIngredient stack, MaterialNode node) {
			this.stack = stack;
			this.node = node;
		}

		public Hover(EmiRecipeCategory category) {
			this.category = category;
		}

		public boolean drawTooltip(MatrixStack matrices, int mouseX, int mouseY) {
			if (stack != null) {
				List<TooltipComponent> list = Lists.newArrayList();
				list.addAll(stack.getTooltip());
				if (node.recipe != null) {
					list.add(new RecipeTooltipComponent(node.recipe));
				}
				((ScreenAccessor) FakeScreen.INSTANCE).invokeRenderTooltipFromComponents(matrices, list, mouseX, Math.max(16, mouseY));
				return true;
			} else if (category != null) {
				((ScreenAccessor) FakeScreen.INSTANCE).invokeRenderTooltipFromComponents(matrices,
					category.getTooltip(), mouseX, Math.max(16, mouseY));
				return true;
			}
			return false;
		}
	}

	private static class Node {
		public Node parent = null;
		public MaterialNode resolution = null;
		public MaterialNode node;
		public int width;
		public int amount, x, y;
		public boolean leaf = false;

		public Node(MaterialNode node, int amount, int x, int y) {
			this.node = node;
			if (node.recipe != null) {
				width = 36;
			} else {
				width = 16;
			}
			this.amount = amount;
			this.x = x;
			this.y = y;
		}

		public void render(MatrixStack matrices, TextRenderer textRenderer, float delta) {
			if (parent != null) {
				int nx = x;
				int ny = y;
				int px = parent.x;
				int py = parent.y;
				int off = NODE_VERTICAL_SPACING - 1;
				drawLine(matrices, px, py + 11, px, py + off);
				drawLine(matrices, px, py + off, nx, py + off);
				if (resolution != null) {
					RenderSystem.setShaderTexture(0, EmiRenderHelper.WIDGETS);
					DrawableHelper.drawTexture(matrices, x - 3, y - 19, 9, 192, 7, 7, 256, 256);
					drawLine(matrices, nx, y - 12, nx, ny - 11);
					drawLine(matrices, nx, py + off, nx, y - 19);
				} else {
					drawLine(matrices, nx, ny - 11, nx, py + off);
				}
			}
			if (leaf) {
				//fill(matrices, x - 9, node.y - 9, node.x + 9, node.y + 9, 0x66ff0000);
				//drawLine(matrices, x - 9, node.y + 10, node.x + 9, node.y + 10);
				//drawLine(matrices, x - 9, node.y + 10, node.x - 9, node.y + 6);
				//drawLine(matrices, node.x + 9, node.y + 10, node.x + 9, node.y + 6);
			}
			int xo = 0;
			if (node.recipe != null) {
				int lx = x - width / 2 - 4;
				int ly = y - 11;
				int hx = x + width / 2 + 4;
				int hy = y + 11;
				drawLine(matrices, lx, ly, lx, hy);
				drawLine(matrices, hx, ly, hx, hy);
				drawLine(matrices, lx, ly, hx, ly);
				drawLine(matrices, lx, hy, hx, hy);
				EmiRecipeCategory cat = node.recipe.getCategory();
				cat.renderSimplified(matrices, x - 18, y - 8, delta);
				xo = 10;
			} else {
			}
			node.ingredient.render(matrices, x + xo - 8, y - 8, 0);
			matrices.push();
			matrices.translate(0, 0, 200);
			textRenderer.drawWithShadow(matrices, "" + amount, x + xo + 4, y + 1, -1);
			matrices.pop();
		}

		public Hover getHover(int mouseX, int mouseY) {
			if (resolution != null) {
				if (mouseX >= x - 4 && mouseX < x + 4 && mouseY >= y - 19 && mouseY < y - 11) {
					return new Hover(resolution.ingredient, resolution);
				}
			}
			if (node.recipe != null) {
				if (mouseX >= x - 18 && mouseX < x - 2 && mouseY >= y - 8 && mouseY < y + 8) {
					return new Hover(node.recipe.getCategory());
				}
				mouseX -= 10;
			}
			if (mouseX >= x - 8 && mouseX < x + 8 && mouseY >= y - 8 && mouseY < y + 8) {
				return new Hover(node.ingredient, node);
			}
			return null;
		}
	}

	private static class TreeVolume {
		public List<Width> widths = Lists.newArrayList();
		public List<Node> nodes = Lists.newArrayList();

		public TreeVolume(MaterialNode node, int amount, int y) {
			Node head = new Node(node, amount, 0, y);
			int l = head.width / 2;
			widths.add(new Width(-l, head.width - l));
			head.leaf = true;
			nodes.add(head);
		}

		public void addHead(MaterialNode node, int amount, int y) {
			int x = (getLeft(0) + getRight(0)) / 2;
			Node newNode = new Node(node, amount, x, y);
			for (Node n : nodes) {
				if (n.parent == null) {
					n.parent = newNode;
				}
				n.y += NODE_VERTICAL_SPACING;
			}
			int l = newNode.width / 2;
			widths.add(0, new Width(x - l, x + newNode.width - l));
			nodes.add(0, newNode);
		}

		public int getDepth() {
			return widths.size();
		}

		public int getMinLeft() {
			int m = getLeft(0);
			for (int i = 1; i < getDepth(); i++) {
				m = Math.min(m, getLeft(i));
			}
			return m;
		}

		public int getMaxRight() {
			int m = getRight(0);
			for (int i = 1; i < getDepth(); i++) {
				m = Math.max(m, getRight(i));
			}
			return m;
		}

		public int getLeft(int depth) {
			return widths.get(depth).left;
		}

		public int getRight(int depth) {
			return widths.get(depth).right;
		}

		public void addToRight(TreeVolume other) {
			int rOff = getRight(0) - other.getLeft(0) + NODE_HORIZONTAL_SPACING;
			for (int i = 1; i < getDepth() && i < other.getDepth(); i++) {
				rOff = Math.max(rOff, getRight(i) - other.getLeft(i) + NODE_HORIZONTAL_SPACING);
			}
			for (int i = 0; i < other.getDepth(); i++) {
				if (i < getDepth()) {
					widths.get(i).right = other.getRight(i) + rOff;
				} else {
					widths.add(new Width(other.getLeft(i) + rOff, other.getRight(i) + rOff));
				}
			}
			for (Node node : other.nodes) {
				node.x += rOff;
				nodes.add(node);
			}
		}

		private static class Width {
			private int left, right;

			public Width(int left, int right) {
				this.left = left;
				this.right = right;
			}
		}
	}
}
