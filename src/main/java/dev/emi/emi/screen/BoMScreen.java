package dev.emi.emi.screen;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import org.apache.commons.compress.utils.Lists;
import org.lwjgl.glfw.GLFW;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.bom.MaterialCost;
import dev.emi.emi.bom.MaterialNode;
import dev.emi.emi.mixin.accessor.ScreenAccessor;
import dev.emi.emi.screen.tooltip.RecipeTooltipComponent;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;

public class BoMScreen extends Screen {
	private static final int NODE_WIDTH = 30;
	private static final int NODE_HORIZONTAL_SPACING = 20;
	private static final int NODE_VERTICAL_SPACING = 16;
	private static int zoom = 0;
	private double offX, offY;
	private List<Line> lines = Lists.newArrayList();
	private List<Node> nodes = Lists.newArrayList();
	public HandledScreen<?> old;
	private int horizontalOffset = 0;
	private int nodeWidth = 0;
	private int nodeHeight = 0;

	public BoMScreen(HandledScreen<?> old) {
		super(new TranslatableText("screen.emi.bom"));
		this.old = old;
	}

	public void init() {
		offY = height / -3;
		if (BoM.goal != null) {
			TreeVolume volume = addNewNodes(BoM.goal, 1, 1, 0);
			nodes = volume.nodes;
			horizontalOffset = (volume.getMaxRight() + volume.getMinLeft()) / 2;

			nodeHeight = getNodeHeight(BoM.goal);
			nodeWidth = getNodeWidth(BoM.goal);
			/*
			nodes.clear();
			lines.clear();
			addNodes(BoM.goal, 1, 1, nodeWidth * NODE_WIDTH / -2, 0, nodeWidth);*/
		}
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		if (BoM.goal != null) {
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
			BoM.calculateCost();
			int cx = (BoM.costs.size() * 40) / -2 + 10;
			int cy = nodeHeight * NODE_VERTICAL_SPACING * 2 + 10;
			DrawableHelper.drawCenteredText(matrices, textRenderer, new TranslatableText("emi.total_cost"), 0, cy - 16, -1);
			for (MaterialCost cost : BoM.costs) {
				cost.ingredient.render(matrices, cx, cy, 0);
				textRenderer.drawWithShadow(matrices, "" + cost.amount, cx + 18, cy, -1);
				cx += 40;
			}
			if (!BoM.remainders.isEmpty()) {
				cx = (BoM.remainders.size() * 40) / -2 + 10;
				cy += 40;
				DrawableHelper.drawCenteredText(matrices, textRenderer, new TranslatableText("emi.leftovers"), 0, cy - 16, -1);
				for (MaterialCost cost : BoM.remainders.values()) {
					cost.ingredient.render(matrices, cx, cy, 0);
					textRenderer.drawWithShadow(matrices, "" + cost.amount, cx + 18, cy, -1);
					cx += 40;
				}
			}

			for (Line line : lines) {
				drawLine(matrices, line.x1, line.y1, line.x2, line.y2);
			}

			for (Node node : nodes) {
				if (node.parent != null) {
					Node parent = node.parent;
					int nx = node.x;
					int ny = node.y;
					int px = parent.x;
					int py = parent.y;
					drawLine(matrices, px, py + 10, px, py + 14);
					drawLine(matrices, px, py + 14, nx, py + 14);
					drawLine(matrices, nx, ny - 10, nx, py + 14);
				}
				if (node.leaf) {
					//fill(matrices, node.x - 9, node.y - 9, node.x + 9, node.y + 9, 0x66ff0000);
					drawLine(matrices, node.x - 9, node.y + 10, node.x + 9, node.y + 10);
					drawLine(matrices, node.x - 9, node.y + 10, node.x - 9, node.y + 6);
					drawLine(matrices, node.x + 9, node.y + 10, node.x + 9, node.y + 6);
				}
				node.node.ingredient.render(matrices, node.x - 8, node.y - 8, 0);
				textRenderer.drawWithShadow(matrices, "" + node.amount, node.x + 10, node.y + 4, -1);
			}

			viewMatrices.pop();
			RenderSystem.applyModelViewMatrix();

			Node hovered = getNode(mouseX, mouseY);
			if (hovered != null) {
				List<TooltipComponent> list = Lists.newArrayList();
				list.addAll(hovered.node.ingredient.getTooltip());
				if (hovered.node.recipe != null) {
					list.add(new RecipeTooltipComponent(hovered.node.recipe));
				}
				((ScreenAccessor) FakeScreen.INSTANCE).invokeRenderTooltipFromComponents(matrices, list, mouseX, Math.max(16, mouseY));
			}
		}
	}

	public Node getNode(int mx, int my) {
		float scale = getScale();
		mx = (int) ((mx - width / 2) / scale - offX);
		my = (int) ((my - height / 2) / scale - offY);
		for (Node node : nodes) {
			if (mx >= node.x - 8 && mx < node.x + 8 && my >= node.y - 8 && my < node.y + 8) {
				return node;
			}
		}
		return null;
	}

	// TODO delete
	public void addNodes(MaterialNode node, int multiplier, int divisor, int x, int y, int width) {
		int cx = x + width * NODE_WIDTH / 2;
		multiplier = node.amount * (int) Math.ceil(multiplier / (float) divisor);
		nodes.add(new Node(node, multiplier, cx - 8, y));
		int x1 = cx;
		int y1 = y + 22;
		int y2 = y + 29;
		if (node.recipe != null) {
			lines.add(new Line(x1, y + 17, x1, y1));
			int tw = 0;
			int min = Integer.MAX_VALUE;
			int max = Integer.MIN_VALUE;
			for (MaterialNode n : node.children) {
				int w = getNodeWidth(n);
				addNodes(n, multiplier, node.divisor, x + tw * NODE_WIDTH, y + 30, w);
				int e = x + tw * NODE_WIDTH + w * NODE_WIDTH / 2;
				min = Math.min(min, e);
				max = Math.max(max, e);
				lines.add(new Line(e, y1, e, y2));
				tw += w;
			}
			if (max > min) {
				lines.add(new Line(min, y1, max, y1));
			}
		}
	}

	public int getNodeWidth(MaterialNode node) {
		if (node.recipe != null) {
			int i = 0;
			for (MaterialNode n : node.children) {
				i += getNodeWidth(n);
			}
			return i;
		}
		return 1;
	}

	public int getNodeHeight(MaterialNode node) {
		if (node.recipe != null) {
			int i = 1;
			for (MaterialNode n : node.children) {
				i = Math.max(i, getNodeHeight(n));
			}
			return i + 1;
		}
		return 1;
	}

	public TreeVolume addNewNodes(MaterialNode node, int multiplier, int divisor, int depth) {
		multiplier = node.amount * (int) Math.ceil(multiplier / (float) divisor);
		if (node.recipe != null && node.children.size() > 0) {
			TreeVolume volume = addNewNodes(node.children.get(0), multiplier, node.divisor, depth + 1);
			for (int i = 1; i < node.children.size(); i++) {
				volume.addToRight(addNewNodes(node.children.get(i), multiplier, node.divisor, depth + 1));
			}
			volume.addHead(node, multiplier, depth * NODE_VERTICAL_SPACING);
			return volume;
		}
		return new TreeVolume(node, multiplier, depth * NODE_VERTICAL_SPACING);
	}

	private void drawLine(MatrixStack matrices, int x1, int y1, int x2, int y2) {
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
		matrices.pop();
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
			this.onClose();
			return true;
		} else if (this.client.options.keyInventory.matchesKey(keyCode, scanCode)) {
			this.onClose();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		Node node = getNode((int) mouseX, (int) mouseY);
		if (node != null) {
			EmiApi.displayRecipes(node.node.ingredient);
			if (node.node.recipe != null) {
				EmiApi.focusRecipe(node.node.recipe);
			}
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
		if (button == 2) {
			float scale = getScale();
			offX += deltaX / scale;
			offY += deltaY / scale;
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	private static class Node {
		public Node parent = null;
		public MaterialNode node;
		public int amount, x, y;
		public boolean leaf = false;

		public Node(MaterialNode node, int amount, int x, int y) {
			this.node = node;
			this.amount = amount;
			this.x = x;
			this.y = y;
		}
	}

	private static record Line(int x1, int y1, int x2, int y2) {
	}

	private static class TreeVolume {
		public List<Width> widths = Lists.newArrayList();
		public List<Node> nodes = Lists.newArrayList();

		public TreeVolume(MaterialNode node, int amount, int y) {
			widths.add(new Width(-8, 8));
			Node head = new Node(node, amount, 0, y);
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
			widths.add(0, new Width(x - 8, x + 8));
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
