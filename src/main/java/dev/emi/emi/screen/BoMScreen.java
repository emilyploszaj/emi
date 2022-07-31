package dev.emi.emi.screen;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.compress.utils.Lists;
import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.EmiConfig;
import dev.emi.emi.EmiFavorites;
import dev.emi.emi.EmiHistory;
import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRecipes;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.EmiResolutionRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.bind.EmiBind;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.bom.FlatMaterialCost;
import dev.emi.emi.bom.FoldState;
import dev.emi.emi.bom.MaterialNode;
import dev.emi.emi.bom.MaterialTree;
import dev.emi.emi.bom.ProgressState;
import dev.emi.emi.mixin.accessor.ScreenAccessor;
import dev.emi.emi.screen.tooltip.RecipeTooltipComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

public class BoMScreen extends Screen {
	private static final int NODE_WIDTH = 30;
	private static final int NODE_HORIZONTAL_SPACING = 20;
	private static final int NODE_VERTICAL_SPACING = 20;
	private static final int COST_HORIZONTAL_SPACING = 10;
	private static int zoom = 0;
	private Bounds batches = new Bounds(-24, -50, 48, 26);
	private Bounds mode = new Bounds(-24, -50, 16, 16);
	private double offX, offY;
	private List<Node> nodes = Lists.newArrayList();
	private List<Cost> costs = Lists.newArrayList();
	private EmiPlayerInventory playerInv;
	public HandledScreen<?> old;
	private int nodeWidth = 0;
	private int nodeHeight = 0;
	private int lastMouseX, lastMouseY;
	private double scrollAcc = 0;

	public BoMScreen(HandledScreen<?> old) {
		super(EmiPort.translatable("screen.emi.bom"));
		this.old = old;
	}

	public void init() {
		if (BoM.tree != null) {
			offY = height / -3;
		} else {
			offY = 0;
		}
		recalculateTree();
	}

	public void recalculateTree() {
		if (BoM.tree != null) {
			TreeVolume volume = addNewNodes(BoM.tree.goal, BoM.tree.batches, 1, 0);
			nodes = volume.nodes;
			int horizontalOffset = (volume.getMaxRight() + volume.getMinLeft()) / 2;
			for (Node node : volume.nodes) {
				node.x -= horizontalOffset;
			}
			if (!volume.nodes.isEmpty()) {
				Node node = volume.nodes.get(0);
				int width = textRenderer.getWidth("x" + BoM.tree.batches);
				batches = new Bounds(node.x + node.width / 2 + 6, node.y - 10, width + 12, 22);
			}

			nodeWidth = volume.getMaxRight() - volume.getMinLeft();
			nodeHeight = getNodeHeight(BoM.tree.goal);
			playerInv = new EmiPlayerInventory(client.player);
			BoM.tree.calculateProgress(playerInv);
			BoM.tree.calculateCost(false);

			costs.clear();

			int costWidth = 0;
			for (FlatMaterialCost node : BoM.tree.costs) {
				costWidth += 16 + COST_HORIZONTAL_SPACING
						+ EmiRenderHelper.getAmountOverflow(new Cost(node, 0, 0, false).getAmountText());
			}
			int cy = nodeHeight * NODE_VERTICAL_SPACING * 2;
			int costX = (costWidth - COST_HORIZONTAL_SPACING) / -2;
			for (FlatMaterialCost node : BoM.tree.costs) {
				Cost cost = new Cost(node, costX, cy, false);
				costs.add(cost);
				costX += 16 + COST_HORIZONTAL_SPACING + EmiRenderHelper.getAmountOverflow(cost.getAmountText());
			}

			int totalCostWidth = textRenderer.getWidth(EmiPort.translatable("emi.total_cost"));
			mode = new Bounds(totalCostWidth / 2 + 4, cy - 20, 16, 16);

			int remainderWidth = 0;
			for (FlatMaterialCost node : BoM.tree.remainders.values()) {
				remainderWidth += 16 + COST_HORIZONTAL_SPACING
						+ EmiRenderHelper.getAmountOverflow(new Cost(node, 0, 0, true).getAmountText());
			}
			cy += 40;
			int remainderX = (remainderWidth - COST_HORIZONTAL_SPACING) / -2;
			for (FlatMaterialCost node : BoM.tree.remainders.values()) {
				Cost cost = new Cost(node, remainderX, cy, true);
				costs.add(cost);
				remainderX += 16 + COST_HORIZONTAL_SPACING
						+ EmiRenderHelper.getAmountOverflow(cost.getAmountText());
			}
		} else {
			nodes = Lists.newArrayList();
		}
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackgroundTexture(0);
		lastMouseX = mouseX;
		lastMouseY = mouseY;
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

		int mx = (int) ((mouseX - width / 2) / scale - offX);
		int my = (int) ((mouseY - height / 2) / scale - offY);

		MatrixStack viewMatrices = RenderSystem.getModelViewStack();
		viewMatrices.push();
		viewMatrices.translate(width / 2, height / 2, 0);
		viewMatrices.scale(scale, scale, 1);
		viewMatrices.translate(offX, offY, 0);
		RenderSystem.applyModelViewMatrix();
		if (BoM.tree != null) {
			int cy = nodeHeight * NODE_VERTICAL_SPACING * 2;
			DrawableHelper.drawCenteredText(matrices, textRenderer, EmiPort.translatable("emi.total_cost"), 0, cy - 16,
					-1);
			if (!BoM.tree.remainders.isEmpty()) {
				DrawableHelper.drawCenteredText(matrices, textRenderer, EmiPort.translatable("emi.leftovers"), 0,
						cy - 16 + 40, -1);
			}
			for (Cost cost : costs) {
				cost.render(matrices);
			}
			for (Node node : nodes) {
				node.render(matrices, mx, my, delta);
			}
			int color = -1;
			if (batches.contains(mx, my)) {
				color = 0xff8099ff;
			}
			drawTextWithShadow(matrices, textRenderer, EmiPort.literal("x" + BoM.tree.batches),
					batches.x() + 6, batches.y() + batches.height() / 2 - 4, color);

			if (mode.contains(mx, my)) {
				RenderSystem.setShaderColor(0.5f, 0.6f, 1f, 1f);
			}
			RenderSystem.setShaderTexture(0, EmiRenderHelper.WIDGETS);
			drawTexture(matrices, mode.x(), mode.y(), BoM.craftingMode ? 16 : 0, 146, mode.width(), mode.height());
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		} else {
			drawCenteredText(matrices, textRenderer,
					EmiPort.translatable("emi.tree_welcome", EmiRenderHelper.getEmiText()), 0, -72, -1);
			drawCenteredText(matrices, textRenderer, EmiPort.translatable("emi.no_tree"), 0, -48, -1);
			drawCenteredText(matrices, textRenderer, EmiPort.translatable("emi.random_tree"), 0, -24, -1);
			drawCenteredText(matrices, textRenderer, EmiPort.translatable("emi.random_tree_input"), 0, 0, -1);
		}

		viewMatrices.pop();
		RenderSystem.applyModelViewMatrix();
		Hover hover = getHoveredStack(mouseX, mouseY);
		if (hover != null) {
			hover.drawTooltip(this, matrices, mouseX, mouseY);
		} else if (BoM.tree != null && batches.contains(mx, my)) {
			List<TooltipComponent> list = Arrays
					.stream(I18n.translate("tooltip.emi.bom.batch_size", BoM.tree.batches).split("\n"))
					.map(s -> TooltipComponent.of(EmiPort.ordered(EmiPort.literal(s)))).toList();
			((ScreenAccessor) this).invokeRenderTooltipFromComponents(matrices, list, mouseX, Math.max(16, mouseY));
		} else if (BoM.tree != null && mode.contains(mx, my)) {
			String key = BoM.craftingMode ? "tooltip.emi.bom.mode.craft" : "tooltip.emi.bom.mode.view";
			List<TooltipComponent> list = Arrays
					.stream(I18n.translate(key, BoM.tree.batches).split("\n"))
					.map(s -> TooltipComponent.of(EmiPort.ordered(EmiPort.literal(s)))).toList();
			((ScreenAccessor) this).invokeRenderTooltipFromComponents(matrices, list, mouseX, Math.max(16, mouseY));
		}
	}

	public Hover getHoveredStack(int mx, int my) {
		float scale = getScale();
		mx = (int) ((mx - width / 2) / scale - offX);
		my = (int) ((my - height / 2) / scale - offY);
		for (Cost cost : costs) {
			if (mx >= cost.x && mx < cost.x + 16 && my >= cost.y && my < cost.y + 16) {
				return new Hover(cost.cost.ingredient);
			}
		}
		for (Node node : nodes) {
			Hover hover = node.getHover(mx, my);
			if (hover != null) {
				return hover;
			}
		}
		return null;
	}

	public int getNodeHeight(MaterialNode node) {
		if (node.recipe != null && node.state == FoldState.EXPANDED) {
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

	public TreeVolume addNewNodes(MaterialNode node, long multiplier, long divisor, int depth) {
		if (MaterialTree.isCatalyst(node.ingredient)) {
			multiplier = node.amount;
		} else {
			multiplier = node.amount * (int) Math.ceil(multiplier / (float) divisor);
		}
		if (node.recipe != null && node.children.size() > 0 && node.state == FoldState.EXPANDED) {
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
		Hover hover = getHoveredStack(lastMouseX, lastMouseY);
		if (hover != null && hover.stack != null && !hover.stack.isEmpty()) {
			if (function.apply(EmiConfig.favorite)) {
				EmiFavorites.addFavorite(hover.stack, hover.node == null ? null : hover.node.recipe);
			}
		}
		if (EmiUtil.isControlDown() && keyCode == GLFW.GLFW_KEY_R) {
			if (EmiRecipes.recipes.size() > 0) {
				for (int i = 0; i < 100_000; i++) {
					EmiRecipe recipe = EmiRecipes.recipes.get(EmiUtil.RANDOM.nextInt(EmiRecipes.recipes.size()));
					if (recipe.supportsRecipeTree()) {
						BoM.setGoal(recipe);
						init();
						return true;
					}
				}
			}
		} else if (EmiUtil.isControlDown() && keyCode == GLFW.GLFW_KEY_C) {
			BoM.tree = null;
			init();
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		Hover hover = getHoveredStack((int) mouseX, (int) mouseY);
		float scale = getScale();
		int mx = (int) ((mouseX - width / 2) / scale - offX);
		int my = (int) ((mouseY - height / 2) / scale - offY);
		if (hover != null) {
			if (button == 1 && hover.node != null && hover.node.recipe != null && !(hover.node.recipe instanceof EmiResolutionRecipe)) {
				if (hover.node.state == FoldState.EXPANDED) {
					hover.node.state = FoldState.COLLAPSED;
				} else {
					hover.node.state = FoldState.EXPANDED;
				}
				recalculateTree();
				return true;
			}
			if (hover.stack != null) {
				EmiApi.displayRecipes(hover.stack);
				RecipeScreen.resolve = hover.stack;
				MinecraftClient client = MinecraftClient.getInstance();
				// The first init doesn't realize a resolution exists so we do it again. What
				// could go wrong.
				client.currentScreen.init(client, client.currentScreen.width, client.currentScreen.height);
				if (hover.node != null) {
					if (hover.node.recipe != null) {
						EmiApi.focusRecipe(hover.node.recipe);
					}
				}
				return true;
			}
		} else if (mode.contains(mx, my)) {
			MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
			BoM.craftingMode = !BoM.craftingMode;
			recalculateTree();
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
		scrollAcc += amount;
		amount = scrollAcc;
		scrollAcc %= 1;
		float scale = getScale();
		int mx = (int) ((mouseX - width / 2) / scale - offX);
		int my = (int) ((mouseY - height / 2) / scale - offY);
		if (BoM.tree != null && batches.contains(mx, my)) {
			if (EmiUtil.isShiftDown()) {
				amount *= 16;
			}
			if (BoM.tree.batches == 1 && amount > 1) {
				BoM.tree.batches = (int) amount;
			} else {
				BoM.tree.batches += (int) amount;
			}
			BoM.tree.batches = Math.max(1, BoM.tree.batches);
			recalculateTree();
			return true;
		}
		zoom += (int) amount;
		return true;
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (button == 0 || button == 2) {
			float scale = getScale();
			offX += deltaX / scale;
			offY += deltaY / scale;
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public void close() {
		MinecraftClient.getInstance().setScreen(old);
	}

	private class Cost {
		public FlatMaterialCost cost;
		public int x, y;
		public boolean remainder;

		public Cost(FlatMaterialCost cost, int x, int y, boolean remainder) {
			this.cost = cost;
			this.x = x;
			this.y = y;
			this.remainder = remainder;
		}

		public void render(MatrixStack matrices) {
			cost.ingredient.render(matrices, x, y, 0, ~(EmiIngredient.RENDER_AMOUNT | EmiIngredient.RENDER_REMAINDER));
			EmiRenderHelper.renderAmount(matrices, x, y, getAmountText());
		}

		public Text getAmountText() {
			if (!remainder && BoM.craftingMode) {
				long amount = 0;
				for (EmiStack stack : cost.ingredient.getEmiStacks()) {
					if (playerInv.inventory.containsKey(stack)) {
						amount += playerInv.inventory.get(stack).getAmount();
					}
				}
				if (amount < cost.amount) {
					Text amountText = amount == 0 ? EmiPort.literal("0") : (cost.ingredient.getAmountText(amount));
					MutableText text = EmiPort.append(EmiPort.literal("", Formatting.RED), amountText);
					text = EmiPort.append(text, EmiPort.literal("/"));
					text = EmiPort.append(text, cost.ingredient.getAmountText(cost.amount));
					return text;
				}
			}
			return cost.ingredient.getAmountText(cost.amount);
		}
	}

	private static class Hover {
		public EmiIngredient stack;
		public MaterialNode node;
		public EmiRecipeCategory category;

		public Hover(EmiIngredient stack) {
			this.stack = stack;
		}

		public Hover(EmiIngredient stack, MaterialNode node) {
			this.stack = stack;
			this.node = node;
		}

		public Hover(EmiRecipeCategory category, MaterialNode node) {
			this.category = category;
			this.node = node;
		}

		public Hover(MaterialNode node) {
			this.node = node;
		}

		public boolean drawTooltip(Screen screen, MatrixStack matrices, int mouseX, int mouseY) {
			if (stack != null) {
				List<TooltipComponent> list = Lists.newArrayList();
				list.addAll(stack.getTooltip());
				if (node != null && node.recipe != null) {
					list.add(new RecipeTooltipComponent(node.recipe));
				}
				((ScreenAccessor) screen).invokeRenderTooltipFromComponents(matrices, list, mouseX,
						Math.max(16, mouseY));
				return true;
			} else if (category != null) {
				((ScreenAccessor) screen).invokeRenderTooltipFromComponents(matrices,
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
		public int width, x, y, midOffset;
		public long amount;
		public boolean leaf = false;

		public Node(MaterialNode node, long amount, int x, int y) {
			this.node = node;
			if (node.recipe != null) {
				width = 36;
			} else {
				width = 16;
			}
			int tw = EmiRenderHelper.getAmountOverflow(node.ingredient.getAmountText(amount));
			width += tw;
			midOffset = tw / -2;
			this.amount = amount;
			this.x = x;
			this.y = y;
		}

		public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
			if (parent != null) {
				matrices.push();
				if (BoM.craftingMode) {
					if (parent.node.progress == ProgressState.COMPLETED) {
						RenderSystem.setShaderColor(0.1f, 0.8f, 0.5f, 1f);
					} else if (parent.node.progress == ProgressState.PARTIAL) {
						RenderSystem.setShaderColor(0.8f, 0.2f, 0.9f, 1f);
					}
				}
				
				int nx = x;
				int ny = y;
				int px = parent.x;
				int py = parent.y;
				int off = NODE_VERTICAL_SPACING - 1;
				drawLine(matrices, px, py + 12, px, py + off);
				drawLine(matrices, px, py + off, nx, py + off);
				if (resolution != null) {
					RenderSystem.setShaderTexture(0, EmiRenderHelper.WIDGETS);
					DrawableHelper.drawTexture(matrices, x - 3, y - 19, 9, 192, 7, 7, 256, 256);
					drawLine(matrices, nx, y - 12, nx, ny - 11);
					drawLine(matrices, nx, py + off, nx, y - 19);
				} else {
					drawLine(matrices, nx, ny - 11, nx, py + off);
				}
				matrices.pop();
			}
			if (leaf) {
				// fill(matrices, x - 9, node.y - 9, node.x + 9, node.y + 9, 0x66ff0000);
				// drawLine(matrices, x - 9, node.y + 10, node.x + 9, node.y + 10);
				// drawLine(matrices, x - 9, node.y + 10, node.x - 9, node.y + 6);
				// drawLine(matrices, node.x + 9, node.y + 10, node.x + 9, node.y + 6);
			}
			int xo = 0;
			if (node.recipe != null) {
				int lx = x - width / 2 - 4;
				int ly = y - 11;
				int hx = x + width / 2 + 4;
				int hy = y + 11;
				if (node.state != FoldState.EXPANDED) {
					drawLine(matrices, x, hy + 1, x, hy + 3);
				}
				boolean hovered = mouseX >= lx && mouseY >= ly && mouseX <= hx && mouseY <= hy;
				matrices.push();
				if (BoM.craftingMode) {
					if (node.progress == ProgressState.COMPLETED) {
						RenderSystem.setShaderColor(0.1f, 0.8f, 0.5f, 1f);
					} else if (node.progress == ProgressState.PARTIAL) {
						RenderSystem.setShaderColor(0.8f, 0.2f, 0.9f, 1f);
					}
				}
				
				if (hovered) {
					RenderSystem.setShaderColor(0.5f, 0.6f, 1f, 1f);
				}
				drawLine(matrices, lx, ly, lx, hy);
				drawLine(matrices, hx, ly, hx, hy);
				drawLine(matrices, lx, ly, hx, ly);
				drawLine(matrices, lx, hy, hx, hy);
				EmiRecipeCategory cat = node.recipe.getCategory();
				cat.renderSimplified(matrices, x - 18 + midOffset, y - 8, delta);
				xo = 10;
				matrices.pop();
			}
			node.ingredient.render(matrices, x + xo - 8 + midOffset, y - 8, 0);
			EmiRenderHelper.renderAmount(matrices, x + xo - 8 + midOffset, y - 8,
					node.ingredient.getAmountText(amount));
		}

		public Hover getHover(int mouseX, int mouseY) {
			if (resolution != null) {
				if (mouseX >= x - 4 && mouseX < x + 4 && mouseY >= y - 19 && mouseY < y - 11) {
					return new Hover(resolution.ingredient, resolution);
				}
			}
			int imx = mouseX;
			if (node.recipe != null) {
				if (mouseX >= x - 18 + midOffset && mouseX < x - 2 + midOffset && mouseY >= y - 8 && mouseY < y + 8) {
					return new Hover(node.recipe.getCategory(), node);
				}
				imx -= 10;
			}
			if (imx >= x - 8 + midOffset && imx < x + 8 + midOffset && mouseY >= y - 8 && mouseY < y + 8) {
				return new Hover(node.ingredient, node);
			}
			int lx = x - width / 2 - 4;
			int ly = y - 11;
			int hx = x + width / 2 + 4;
			int hy = y + 11;
			if (mouseX >= lx && mouseY >= ly && mouseX <= hx && mouseY <= hy) {
				return new Hover(node);
			}
			return null;
		}
	}

	private static class TreeVolume {
		public List<Width> widths = Lists.newArrayList();
		public List<Node> nodes = Lists.newArrayList();

		public TreeVolume(MaterialNode node, long amount, int y) {
			Node head = new Node(node, amount, 0, y);
			int l = head.width / 2;
			widths.add(new Width(-l, head.width - l));
			head.leaf = true;
			nodes.add(head);
		}

		public void addHead(MaterialNode node, long amount, int y) {
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
