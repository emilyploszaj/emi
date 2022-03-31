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
import net.minecraft.client.MinecraftClient;
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
	private static int zoom = 0;
	private double offX, offY;
	private List<Line> lines = Lists.newArrayList();
	private List<Node> nodes = Lists.newArrayList();
	public HandledScreen<?> old;
	private int nodeWidth = 0;
	private int nodeHeight = 0;

	public BoMScreen(HandledScreen<?> old) {
		super(new TranslatableText("screen.emi.bom"));
		this.old = old;
	}

	public void init() {
		offY = height / -3;
		if (BoM.goal != null) {
			nodes.clear();
			lines.clear();
			nodeWidth = getNodeWidth(BoM.goal);
			nodeHeight = getNodeHeight(BoM.goal);
			addNodes(BoM.goal, 1, 1, nodeWidth * NODE_WIDTH / -2, 0, nodeWidth);
		}
		MinecraftClient.getInstance().setScreen(new ConfigScreen(old));
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
			int contentHeight = nodeHeight * 30 + 80;
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
			int cy = nodeHeight * 30 + 10;
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
				node.node.ingredient.render(matrices, node.x, node.y, 0);
				textRenderer.drawWithShadow(matrices, "" + node.amount, node.x + 18, node.y + 4, -1);
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
				((ScreenAccessor) FakeScreen.INSTANCE).invokeRenderTooltipFromComponents(matrices, list, mouseX, mouseY);
			}
		}
	}

	public Node getNode(int mx, int my) {
		float scale = getScale();
		mx = (int) ((mx - width / 2) / scale - offX);
		my = (int) ((my - height / 2) / scale - offY);
		for (Node node : nodes) {
			if (mx >= node.x && mx < node.x + 16 && my >= node.y && my < node.y + 16) {
				return node;
			}
		}
		return null;
	}

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

	private static record Node(MaterialNode node, int amount, int x, int y) {
	}

	private static record Line(int x1, int y1, int x2, int y2) {
	}
}
