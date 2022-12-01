package dev.emi.emi.screen;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.EmiClient;
import dev.emi.emi.EmiDragDropHandlers;
import dev.emi.emi.EmiExclusionAreas;
import dev.emi.emi.EmiFavorite;
import dev.emi.emi.EmiFavorites;
import dev.emi.emi.EmiHistory;
import dev.emi.emi.EmiReloadLog;
import dev.emi.emi.EmiMain;
import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiReloadManager;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.EmiStackList;
import dev.emi.emi.EmiStackProviders;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.EmiFillAction;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.bind.EmiBind;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.chess.EmiChess;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.config.HeaderType;
import dev.emi.emi.config.IntGroup;
import dev.emi.emi.config.Margins;
import dev.emi.emi.config.ScreenAlign;
import dev.emi.emi.config.ScreenAlign.Horizontal;
import dev.emi.emi.config.SidebarPages;
import dev.emi.emi.config.SidebarSide;
import dev.emi.emi.config.SidebarTheme;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.screen.tooltip.RecipeTooltipComponent;
import dev.emi.emi.screen.widget.EmiSearchWidget;
import dev.emi.emi.screen.widget.SidebarButtonWidget;
import dev.emi.emi.screen.widget.SizedButtonWidget;
import dev.emi.emi.search.EmiSearch;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;

public class EmiScreenManager {
	private static final int PADDING_SIZE = 1;
	private static final int ENTRY_SIZE = 16 + PADDING_SIZE * 2;
	private static MinecraftClient client = MinecraftClient.getInstance();
	private static List<? extends EmiIngredient> searchedStacks = List.of();
	private static int lastWidth, lastHeight;
	private static List<Bounds> lastExclusion;
	private static List<SidebarPanel> panels = List.of(
			new SidebarPanel(SidebarSide.LEFT, EmiConfig.leftSidebarPages),
			new SidebarPanel(SidebarSide.RIGHT, EmiConfig.rightSidebarPages),
			new SidebarPanel(SidebarSide.TOP, EmiConfig.topSidebarPages),
			new SidebarPanel(SidebarSide.BOTTOM, EmiConfig.bottomSidebarPages));
	public static EmiPlayerInventory lastPlayerInventory;
	public static List<EmiIngredient> craftables = List.of();
	public static int lastMouseX, lastMouseY;
	// The stack that was clicked on, for determining when a drag properly starts
	public static EmiIngredient pressedStack = EmiStack.EMPTY;
	public static EmiIngredient draggedStack = EmiStack.EMPTY;
	// Prevent users from clicking on the wrong thing as their index changes under
	// them
	private static EmiStackInteraction lastHoveredCraftable = null;
	// Whether the craftable has been used multiple times, indicating it shouldn't
	// disappear
	// Even if the recipe it was focusing becomes invalid
	private static boolean lastHoveredCraftableSturdy = false;
	private static int lastHoveredCraftableOffset = -1;
	private static double scrollAcc = 0;

	public static EmiSearchWidget search = new EmiSearchWidget(client.textRenderer, 0, 0, 160, 18);
	public static SizedButtonWidget emi = new SizedButtonWidget(0, 0, 20, 20, 204, 64,
			() -> true, (w) -> client.setScreen(new ConfigScreen(client.currentScreen)),
			List.of(EmiPort.translatable("tooltip.emi.config", EmiRenderHelper.getEmiText())));
	public static SizedButtonWidget tree = new SizedButtonWidget(0, 0, 20, 20, 184, 64,
			() -> true, (w) -> EmiApi.viewRecipeTree(),
			List.of(EmiPort.translatable("tooltip.emi.recipe_tree")));

	private static boolean isDisabled() {
		return EmiReloadManager.isReloading() || !EmiConfig.enabled;
	}

	public static void recalculate() {
		EmiPlayerInventory inv = new EmiPlayerInventory(client.player);
		if (!inv.isEqual(lastPlayerInventory)) {
			lastPlayerInventory = inv;
			craftables = lastPlayerInventory.getCraftables();
			SidebarPanel panel = getSearchPanel();
			panel.batcher.repopulate();
			if (panel.getType() == SidebarType.CRAFTABLES) {
				EmiSearch.update();
			}
			EmiFavorites.updateSynthetic(inv);
			forPanel(SidebarType.CRAFTABLES, p -> p.batcher.repopulate());
		}
		if (searchedStacks != EmiSearch.stacks) {
			SidebarPanel panel = getSearchPanel();
			panel.batcher.repopulate();
			searchedStacks = EmiSearch.stacks;
		}

		Screen screen = client.currentScreen;
		if (screen == null) {
			return;
		}
		List<Bounds> exclusion = EmiExclusionAreas.getExclusion(screen);
		if (lastWidth == screen.width && lastHeight == screen.height && exclusion.size() == lastExclusion.size()) {
			boolean same = true;
			for (int i = 0; i < exclusion.size(); i++) {
				Bounds a = exclusion.get(i);
				Bounds b = lastExclusion.get(i);
				if (a.x() != b.x() || a.y() != b.y() || a.width() != b.width() || a.height() != b.height()) {
					same = false;
					break;
				}
			}
			if (same) {
				return;
			}
		}
		for (SidebarPanel panel : panels) {
			panel.batcher.repopulate();
		}
		lastWidth = screen.width;
		lastHeight = screen.height;
		lastExclusion = exclusion;
		if (screen instanceof EmiScreen emi) {
			int left = Math.max(ENTRY_SIZE * 2, emi.emi$getLeft());
			int right = Math.min(screen.width - ENTRY_SIZE * 2, emi.emi$getRight());
			int top = emi.emi$getTop();
			int bottom = emi.emi$getBottom();

			List<Bounds> spaceExclusion = Lists.newArrayList();
			spaceExclusion.addAll(exclusion);

			panels.get(0).space = createScreenSpace(screen, spaceExclusion, false,
					new Bounds(0, 0, left, screen.height),
					EmiConfig.leftSidebarSize, EmiConfig.leftSidebarMargins, EmiConfig.leftSidebarAlign,
					EmiConfig.leftSidebarTheme,
					EmiConfig.leftSidebarHeader == HeaderType.VISIBLE, panels.get(0));

			panels.get(1).space = createScreenSpace(screen, spaceExclusion, true,
					new Bounds(right, 0, screen.width - right, screen.height),
					EmiConfig.rightSidebarSize, EmiConfig.rightSidebarMargins, EmiConfig.rightSidebarAlign,
					EmiConfig.rightSidebarTheme,
					EmiConfig.rightSidebarHeader == HeaderType.VISIBLE, panels.get(1));

			spaceExclusion = Lists.newArrayList();
			if (panels.get(0).isVisible()) {
				spaceExclusion.add(panels.get(0).getBounds());
			}
			if (panels.get(1).isVisible()) {
				spaceExclusion.add(panels.get(1).getBounds());
			}
			spaceExclusion.addAll(exclusion);

			int topSpaceBottom = switch (EmiConfig.topSidebarAlign.horizontal) {
				case LEFT -> Math.max(panels.get(0).getBounds().top(), top);
				case CENTER -> top;
				case RIGHT -> Math.max(panels.get(1).getBounds().top(), top);
			};
			boolean topRtl = EmiConfig.topSidebarAlign.horizontal == Horizontal.RIGHT;

			panels.get(2).space = createScreenSpace(screen, spaceExclusion, topRtl,
					new Bounds(0, 0, screen.width, topSpaceBottom),
					EmiConfig.topSidebarSize, EmiConfig.topSidebarMargins, EmiConfig.topSidebarAlign,
					EmiConfig.topSidebarTheme,
					EmiConfig.topSidebarHeader == HeaderType.VISIBLE, panels.get(2));

			int bottomSpaceTop = switch (EmiConfig.bottomSidebarAlign.horizontal) {
				case LEFT -> Math.min(panels.get(0).getBounds().bottom(), bottom);
				case CENTER -> bottom;
				case RIGHT -> Math.min(panels.get(1).getBounds().bottom(), bottom);
			};
			boolean bottomRtl = EmiConfig.bottomSidebarAlign.horizontal == Horizontal.RIGHT;

			panels.get(3).space = createScreenSpace(screen, spaceExclusion, bottomRtl,
					new Bounds(0, bottomSpaceTop, screen.width, screen.height - bottomSpaceTop),
					EmiConfig.bottomSidebarSize, EmiConfig.bottomSidebarMargins, EmiConfig.bottomSidebarAlign,
					EmiConfig.bottomSidebarTheme,
					EmiConfig.bottomSidebarHeader == HeaderType.VISIBLE, panels.get(3));

			updateSidebarButtons();
		}
	}

	private static ScreenSpace createScreenSpace(Screen screen, List<Bounds> exclusion, boolean rtl,
			Bounds bounds, IntGroup size, Margins margins, ScreenAlign align, SidebarTheme theme,
			boolean header, SidebarPanel panel) {
		int maxWidth = size.values.getInt(0);
		int maxHeight = size.values.getInt(1);
		if (panel.getType() == SidebarType.CHESS) {
			maxWidth = 8;
			maxHeight = 8;
			theme = SidebarTheme.MODERN;
		}
		int cx = bounds.x() + bounds.width() / 2;
		int cy = bounds.y() + bounds.height() / 2;
		int headerOffset = header ? 18 : 0;

		// Try a more optimistic approach to position the bounding box slightly more
		// pleasantly if applicable
		int idealWidth = Math.min(
				maxWidth * ENTRY_SIZE + margins.left() + margins.right() + theme.horizontalPadding * 2, bounds.width());
		int idealHeight = Math.min(
				maxHeight * ENTRY_SIZE + margins.top() + margins.bottom() + theme.verticalPadding * 2 + headerOffset,
				bounds.height());
		int idealX = switch (align.horizontal) {
			case LEFT -> bounds.x();
			case CENTER -> bounds.x() + bounds.width() / 2 - idealWidth / 2;
			case RIGHT -> bounds.right() - idealWidth;
		};
		int idealY = switch (align.horizontal) {
			case LEFT -> bounds.y();
			case CENTER -> bounds.y() + bounds.height() / 2 - idealHeight / 2;
			case RIGHT -> bounds.bottom() - idealHeight;
		};
		Bounds idealBounds = constrainBounds(exclusion, new Bounds(idealX, idealY, idealWidth, idealHeight), align,
				headerOffset);

		bounds = constrainBounds(exclusion, bounds, align, headerOffset);

		if (Math.min(idealWidth, idealBounds.width()) * Math.min(idealHeight, idealBounds.height()) > Math
				.min(idealWidth, bounds.width()) * Math.min(idealHeight, bounds.height())) {
			bounds = idealBounds;
		}

		int xMin = bounds.left() + margins.left() + theme.horizontalPadding;
		int xMax = bounds.right() - margins.right() - theme.horizontalPadding;
		int yMin = bounds.top() + margins.top() + theme.verticalPadding;
		int yMax = bounds.bottom() - margins.bottom() - theme.verticalPadding;
		int xSpan = xMax - xMin;
		int ySpan = yMax - yMin;
		int tw = Math.max(0, Math.min((xSpan) / ENTRY_SIZE, maxWidth));
		int th = Math.max(0, Math.min((ySpan - headerOffset) / ENTRY_SIZE, maxHeight));
		int hl = xMin;
		int hr = xMax - tw * ENTRY_SIZE;
		int tx = switch (align.horizontal) {
			case LEFT -> hl;
			case CENTER -> MathHelper.clamp(cx - (tw * ENTRY_SIZE) / 2, hl, hr);
			case RIGHT -> hr;
		};
		int vt = yMin + headerOffset;
		int vb = yMax - th * ENTRY_SIZE;
		int ty = switch (align.vertical) {
			case TOP -> vt;
			case CENTER -> MathHelper.clamp(cy - (th * ENTRY_SIZE - headerOffset) / 2, vt, vb);
			case BOTTOM -> vb;
		};
		return new ScreenSpace(tx, ty, tw, th, rtl, exclusion, theme, header);
	}

	private static Bounds constrainBounds(List<Bounds> exclusion, Bounds bounds, ScreenAlign align, int headerOffset) {
		for (int i = 0; i < exclusion.size(); i++) {
			Bounds overlap = exclusion.get(i).overlap(bounds);
			if (!overlap.empty()) {
				if (overlap.top() < bounds.top() + ENTRY_SIZE + headerOffset || overlap.width() >= bounds.width() / 2
						|| overlap.height() >= bounds.height() / 3) {
					int widthFactor = overlap.width() * 10 / bounds.width();
					int heightFactor = overlap.height() * 10 / bounds.height();
					if (heightFactor < widthFactor) {
						int cy = bounds.y() + bounds.height() / 2;
						int ocy = overlap.y() + overlap.height() / 2;
						cy += switch (align.vertical) {
							case TOP -> -bounds.height() / 4;
							case CENTER -> 0;
							case BOTTOM -> bounds.height() / 4;
						};
						if (cy < ocy) {
							bounds = new Bounds(bounds.x(), bounds.y(), bounds.width(), overlap.top() - bounds.top());
						} else {
							bounds = new Bounds(bounds.x(), overlap.bottom(), bounds.width(),
									bounds.bottom() - overlap.bottom());
						}
					} else {
						int cx = bounds.x() + bounds.width() / 2;
						int ocx = overlap.x() + overlap.width() / 2;
						cx += switch (align.horizontal) {
							case LEFT -> -bounds.width() / 4;
							case CENTER -> 0;
							case RIGHT -> bounds.width() / 4;
						};
						if (cx < ocx) {
							bounds = new Bounds(bounds.x(), bounds.y(), overlap.left() - bounds.left(),
									bounds.height());
						} else {
							bounds = new Bounds(overlap.right(), bounds.y(), bounds.right() - overlap.right(),
									bounds.height());
						}
					}
					i = -1;
				}
			}
		}
		if (bounds.empty()) {
			return Bounds.EMPTY;
		}
		return bounds;
	}

	public static void focusSearchSidebarType(SidebarType type) {
		if (getSearchPanel().supportsType(type)) {
			getSearchPanel().setType(type);
		}
	}

	public static void focusSidebarType(SidebarType type) {
		for (SidebarPanel panel : panels) {
			if (panel.supportsType(type)) {
				panel.setType(type);
			}
		}
	}

	public static @Nullable SidebarPanel getPanelFor(SidebarSide side) {
		for (SidebarPanel panel : panels) {
			if (panel.side == side) {
				return panel;
			}
		}
		return null;
	}

	public static @Nullable SidebarPanel getHoveredPanel(int mouseX, int mouseY) {
		for (SidebarPanel panel : panels) {
			if (panel.getBounds().contains(mouseX, mouseY)) {
				return panel;
			}
		}
		return null;
	}

	public static boolean hasSidebarAvailable(SidebarType type) {
		for (SidebarPanel panel : panels) {
			if (panel.supportsType(type)) {
				return true;
			}
		}
		return false;
	}

	public static void forPanel(SidebarType type, Consumer<SidebarPanel> consumer) {
		for (SidebarPanel panel : panels) {
			if (panel.getType() == type) {
				consumer.accept(panel);
			}
		}
	}

	public static SidebarPanel getSearchPanel() {
		for (SidebarPanel panel : panels) {
			if (panel.isSearch()) {
				return panel;
			}
		}
		return panels.get(1);
	}

	public static List<? extends EmiIngredient> getSearchSource() {
		return SidebarPanel.getFromSidebarType(getSearchPanel().getType());
	}

	public static EmiStackInteraction getHoveredStack(int mouseX, int mouseY, boolean notClick) {
		return getHoveredStack(mouseX, mouseY, notClick, false);
	}

	public static EmiStackInteraction getHoveredStack(int mouseX, int mouseY, boolean notClick,
			boolean ignoreLastHoveredCraftable) {
		if (client.currentScreen == null) {
			return EmiStackInteraction.EMPTY;
		}
		EmiStackInteraction stack = EmiStackProviders.getStackAt(client.currentScreen, mouseX, mouseY, notClick);
		if (!stack.isEmpty()) {
			return stack;
		}
		if (!ignoreLastHoveredCraftable) {
			if (lastHoveredCraftable != null) {
				EmiPlayerInventory inv = new EmiPlayerInventory(client.player);
				if (lastHoveredCraftable.getRecipeContext() == null
						|| (!lastHoveredCraftableSturdy && !inv.canCraft(lastHoveredCraftable.getRecipeContext()))) {
					lastHoveredCraftable = null;
				} else {
					return lastHoveredCraftable;
				}
			}
		}
		for (SidebarPanel panel : panels) {
			ScreenSpace space = panel.space;
			if (panel.isVisible() && space.pageSize > 0 && space.contains(mouseX, mouseY)
					&& mouseX >= space.tx && mouseY >= space.ty) {
				int x = (mouseX - space.tx) / ENTRY_SIZE;
				int y = (mouseY - space.ty) / ENTRY_SIZE;
				int n = space.getRawOffset(x, y) + space.pageSize * panel.page;
				if (n >= 0 && n < panel.getStacks().size()) {
					return of(panel.getStacks().get(n));
				}
			}
		}
		return EmiStackInteraction.EMPTY;
	}

	private static EmiStackInteraction of(EmiIngredient stack) {
		if (stack instanceof EmiFavorite fav) {
			return new SidebarEmiStackInteraction(stack, fav.getRecipe(), true);
		}
		return new SidebarEmiStackInteraction(stack);
	}

	private static void updateMouse(int mouseX, int mouseY) {
		if (lastHoveredCraftable != null) {
			SidebarPanel panel = getHoveredPanel(mouseX, mouseY);
			if (panel != null && panel.getType() == SidebarType.CRAFTABLES) {
				int offset = panel.space.getRawOffsetFromMouse(mouseX, mouseY);
				if (offset != lastHoveredCraftableOffset) {
					lastHoveredCraftable = null;
				}
			} else {
				lastHoveredCraftable = null;
			}
		}
		lastMouseX = mouseX;
		lastMouseY = mouseY;
	}

	public static void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		client.getProfiler().push("emi");
		updateMouse(mouseX, mouseY);
		recalculate();
		Screen screen = client.currentScreen;
		if (screen == null) {
			return;
		}
		boolean visible = !isDisabled();
		emi.visible = visible;
		tree.visible = visible;
		for (SidebarPanel panel : panels) {
			panel.updateWidgetVisibility();
		}
		if (isDisabled()) {
			if (EmiReloadManager.isReloading()) {
				client.textRenderer.drawWithShadow(matrices, "EMI Reloading...", 4, screen.height - 16, -1);
			}
			client.getProfiler().pop();
			lastHoveredCraftable = null;
			return;
		}
		renderWidgets(matrices, mouseX, mouseY, delta, screen);
		client.getProfiler().push("sidebars");
		if (screen instanceof EmiScreen emi) {
			client.getProfiler().push("sidebar");
			for (SidebarPanel panel : panels) {
				panel.render(matrices, mouseX, mouseY, delta);
			}

			renderLastHoveredCraftable(matrices, mouseX, mouseY, delta, screen);

			renderDraggedStack(matrices, mouseX, mouseY, delta, screen);

			renderCurrentTooltip(matrices, mouseX, mouseY, delta, screen);
			client.getProfiler().pop();
		}

		renderDevMode(matrices, mouseX, mouseY, delta, screen);
		client.getProfiler().pop();

		renderExclusionAreas(matrices, mouseX, mouseY, delta, screen);
		client.getProfiler().pop();
	}

	private static void renderWidgets(MatrixStack matrices, int mouseX, int mouseY, float delta, Screen screen) {
		matrices.push();
		matrices.translate(0, 0, 100);
		emi.render(matrices, mouseX, mouseY, delta);
		tree.render(matrices, mouseX, mouseY, delta);
		search.render(matrices, mouseX, mouseY, delta);
		matrices.pop();
	}

	private static void renderLastHoveredCraftable(MatrixStack matrices, int mouseX, int mouseY, float delta,
			Screen screen) {
		if (lastHoveredCraftable != null && lastHoveredCraftableOffset != -1) {
			EmiStackInteraction cur = getHoveredStack(mouseX, mouseY, false, true);
			if (cur.getRecipeContext() != lastHoveredCraftable.getRecipeContext()) {
				SidebarPanel panel = getHoveredPanel(mouseX, mouseY);
				if (panel != null && panel.getType() == SidebarType.CRAFTABLES) {
					MatrixStack view = RenderSystem.getModelViewStack();
					view.push();
					view.translate(0, 0, 200);
					RenderSystem.applyModelViewMatrix();
					int lhx = panel.space.getRawX(lastHoveredCraftableOffset);
					int lhy = panel.space.getRawY(lastHoveredCraftableOffset);
					DrawableHelper.fill(matrices, lhx, lhy, lhx + 18, lhy + 18, 0x44AA00FF);
					lastHoveredCraftable.getStack().render(matrices, lhx + 1, lhy + 1, delta,
							EmiIngredient.RENDER_ICON);
					view.pop();
					RenderSystem.applyModelViewMatrix();
				}
			}
		}
	}

	private static void renderDraggedStack(MatrixStack matrices, int mouseX, int mouseY, float delta, Screen screen) {
		if (!draggedStack.isEmpty()) {
			SidebarPanel panel = getHoveredPanel(mouseX, mouseY);
			if (panel != null && panel.getType() == SidebarType.FAVORITES) {
				int pageSize = panel.space.pageSize;
				int page = panel.page;
				if (panel.space.containsNotExcluded(mouseX, mouseY)) {
					int index = panel.space.getClosestEdge(mouseX, mouseY);
					if (index + pageSize * page > EmiFavorites.favorites.size()) {
						index = EmiFavorites.favorites.size() - pageSize * page;
					}
					if (index + pageSize * page > panel.getStacks().size()) {
						index = panel.getStacks().size() - pageSize * page;
					}
					if (index >= 0) {
						matrices.push();
						matrices.translate(0, 0, 200);
						int dx = panel.space.getEdgeX(index);
						int dy = panel.space.getEdgeY(index);
						DrawableHelper.fill(matrices, dx - 1, dy, dx + 1, dy + 18, 0xFF00FFFF);
						matrices.pop();
					}
				}
			}
			EmiDragDropHandlers.render(screen, draggedStack, matrices, mouseX, mouseY, delta);
			MatrixStack view = RenderSystem.getModelViewStack();
			view.push();
			view.translate(0, 0, 200);
			RenderSystem.applyModelViewMatrix();
			draggedStack.render(matrices, mouseX - 8, mouseY - 8, delta, EmiIngredient.RENDER_ICON);
			view.pop();
			RenderSystem.applyModelViewMatrix();
		}
	}

	private static void renderCurrentTooltip(MatrixStack matrices, int mouseX, int mouseY, float delta, Screen screen) {
		if (draggedStack.isEmpty()) {
			client.getProfiler().swap("hover");
			MatrixStack view = RenderSystem.getModelViewStack();
			view.push();
			view.translate(0, 0, 200);
			RenderSystem.applyModelViewMatrix();
			EmiIngredient hov = getHoveredStack(mouseX, mouseY, false).getStack();
			List<TooltipComponent> list = Lists.newArrayList();
			list.addAll(hov.getTooltip());
			if (EmiApi.getRecipeContext(hov) == null && EmiConfig.showCraft.isHeld()) {
				List<EmiRecipe> recipes = EmiUtil.getValidRecipes(hov, lastPlayerInventory, true);
				if (!recipes.isEmpty()) {
					list.add(new RecipeTooltipComponent(EmiUtil.getPreferredRecipe(recipes), false));
				} else {
					recipes = EmiUtil.getValidRecipes(hov, lastPlayerInventory, false);
					if (!recipes.isEmpty()) {
						list.add(new RecipeTooltipComponent(EmiUtil.getPreferredRecipe(recipes), true));
					}
				}
			}
			SidebarPanel panel = getHoveredPanel(mouseX, mouseY);
			if (panel != null && panel.space.rtl) {
				EmiRenderHelper.drawLeftTooltip(screen, matrices, list, mouseX, mouseY);
			} else {
				EmiRenderHelper.drawTooltip(screen, matrices, list, mouseX, mouseY);
			}
			view.pop();
			RenderSystem.applyModelViewMatrix();
			client.getProfiler().pop();
		}
	}

	private static void renderDevMode(MatrixStack matrices, int mouseX, int mouseY, float delta, Screen screen) {
		if (EmiConfig.devMode) {
			client.getProfiler().swap("dev");
			int color = 0xFFFFFF;
			String title = "EMI Dev Mode";
			int off = -16;
			if (EmiReloadLog.WARNINGS.size() > 0) {
				color = 0xFF0000;
				off = -11;
				String warnCount = EmiReloadLog.WARNINGS.size() + " Warnings";
				client.textRenderer.drawWithShadow(matrices, warnCount, 48, screen.height - 21, color);
				int width = Math.max(client.textRenderer.getWidth(title), client.textRenderer.getWidth(warnCount));
				if (mouseX >= 48 && mouseX < width + 48 && mouseY > screen.height - 28) {
					screen.renderTooltip(matrices, Stream.concat(Stream.of("See log for more information"),
							EmiReloadLog.WARNINGS.stream()).map(s -> {
								String a = s;
								if (a.length() > 10 && client.textRenderer.getWidth(a) > screen.width - 20) {
									a = client.textRenderer.trimToWidth(a, screen.width - 30) + "...";
								}
								return EmiPort.literal(a);
							})
							.collect(Collectors.toList()), 0, 20);
				}
			}
			client.textRenderer.drawWithShadow(matrices, title, 48, screen.height + off, color);
		}
	}

	private static void renderExclusionAreas(MatrixStack matrices, int mouseX, int mouseY, float delta, Screen screen) {
		if (EmiConfig.highlightExclusionAreas) {
			if (screen instanceof EmiScreen emi) {
				for (SidebarPanel panel : panels) {
					if (panel.isVisible()) {
						Bounds bounds = panel.getBounds();
						DrawableHelper.fill(matrices, bounds.left(), bounds.top(), bounds.right(), bounds.bottom(),
								0x440000ff);
					}
				}
				List<Bounds> exclusions = EmiExclusionAreas.getExclusion(screen);
				if (exclusions.size() == 0) {
					return;
				}
				for (int i = 0; i < exclusions.size(); i++) {
					Bounds b = exclusions.get(i);
					DrawableHelper.fill(matrices, b.x(), b.y(), b.x() + b.width(), b.y() + b.height(),
							i == 0 ? 0x4400ff00 : 0x44ff0000);
				}
			}
		}
	}

	public static void addWidgets(Screen screen) {
		// force recalculation
		lastWidth = -1;
		lastPlayerInventory = null;
		recalculate();
		if (EmiConfig.centerSearchBar) {
			search.x = (screen.width - 160) / 2;
			search.y = screen.height - 22;
			search.setWidth(160);
		} else {
			search.x = panels.get(1).space.tx;
			search.y = screen.height - 21;
			search.setWidth(panels.get(1).space.tw * ENTRY_SIZE);
		}
		search.setTextFieldFocused(false);

		emi.x = 2;
		emi.y = screen.height - 22;

		tree.x = 24;
		tree.y = screen.height - 22;

		updateSidebarButtons();

		screen.addSelectableChild(search);
		screen.addSelectableChild(emi);
		screen.addSelectableChild(tree);

		for (SidebarPanel panel : panels) {
			panel.updateWidgetPosition();
			panel.updateWidgetVisibility();
			screen.addSelectableChild(panel.pageLeft);
			screen.addSelectableChild(panel.cycle);
			screen.addSelectableChild(panel.pageRight);
		}
	}

	private static void updateSidebarButtons() {
		for (SidebarPanel panel : panels) {
			panel.updateWidgetPosition();
		}
	}

	public static boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		scrollAcc += amount;
		int sa = (int) scrollAcc;
		scrollAcc %= 1;
		if (isDisabled()) {
			return false;
		}
		recalculate();
		for (SidebarPanel panel : panels) {
			if (panel.getBounds().contains((int) mouseX, (int) mouseY)) {
				panel.scroll(-sa);
				return true;
			}
		}
		return false;
	}

	public static boolean mouseClicked(double mouseX, double mouseY, int button) {
		// TODO This makes sure focus always goes away, but might double fire, is that a problem?
		if (!search.isMouseOver(mouseX, mouseY)) {
			EmiScreenManager.search.mouseClicked(mouseX, mouseY, button);
		}
		if (isDisabled()) {
			if (EmiConfig.toggleVisibility.matchesMouse(button)) {
				EmiConfig.enabled = !EmiConfig.enabled;
				EmiConfig.writeConfig();
				return true;
			}
			return false;
		}
		recalculate();
		EmiIngredient ingredient = getHoveredStack((int) mouseX, (int) mouseY, false).getStack();
		pressedStack = ingredient;
		if (!ingredient.isEmpty()) {
			return true;
		}
		return false;
	}

	public static boolean mouseReleased(double mouseX, double mouseY, int button) {
		try {
			if (isDisabled()) {
				return false;
			}
			int mx = (int) mouseX;
			int my = (int) mouseY;
			recalculate();
			if (EmiConfig.cheatMode) {
				if (client.currentScreen instanceof HandledScreen<?> handled) {
					ItemStack cursor = handled.getScreenHandler().getCursorStack();
					SidebarPanel panel = getHoveredPanel(mx, my);
					if (!cursor.isEmpty() && panel != null && panel.getType() == SidebarType.INDEX
							&& panel.space.containsNotExcluded(lastMouseX, lastMouseY)) {
						handled.getScreenHandler().setCursorStack(ItemStack.EMPTY);
						ClientPlayNetworking.send(EmiMain.DESTROY_HELD, new PacketByteBuf(Unpooled.buffer()));
						// Returning false here makes the handled screen do something and removes a bug,
						// oh well.
						return false;
					}
				}
			}
			if (!pressedStack.isEmpty()) {
				SidebarPanel panel = getHoveredPanel(mx, my);
				if (!draggedStack.isEmpty()) {
					if (panel != null) {
						if (panel.getType() == SidebarType.FAVORITES && panel.space.containsNotExcluded(mx, my)) {
							int page = panel.page;
							int pageSize = panel.space.pageSize;
							int index = Math.min(panel.space.getClosestEdge(mx, my), EmiFavorites.favorites.size());
							if (index + pageSize * page > EmiFavorites.favorites.size()) {
								index = EmiFavorites.favorites.size() - pageSize * page;
							}
							if (index >= 0) {
								EmiFavorites.addFavoriteAt(draggedStack, index + pageSize * page);
								panel.batcher.repopulate();
							}
							return true;
						} else if (panel.getType() == SidebarType.CHESS) {
							EmiChess.drop(draggedStack, getHoveredStack(mx, my, true).getStack());
						}
					} else if (client.currentScreen != null) {
						if (EmiDragDropHandlers.dropStack(client.currentScreen, draggedStack, mx, my)) {
							return true;
						}
					}
				} else {
					if (panel != null && panel.getType() == SidebarType.CHESS) {
						EmiChess.interact(pressedStack, button);
					}
					EmiStackInteraction hovered = getHoveredStack((int) mouseX, (int) mouseY, false);
					if (draggedStack.isEmpty() && stackInteraction(hovered, bind -> bind.matchesMouse(button))) {
						return true;
					}
				}
				if (genericInteraction(bind -> bind.matchesMouse(button))) {
					return true;
				}
			}
			return false;
		} finally {
			pressedStack = EmiStack.EMPTY;
			draggedStack = EmiStack.EMPTY;
		}
	}

	public static boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (isDisabled()) {
			return false;
		}
		if (draggedStack.isEmpty() && button == 0) {
			if (client.currentScreen instanceof HandledScreen<?> handled) {
				if (!handled.getScreenHandler().getCursorStack().isEmpty()) {
					return false;
				}
			}
			recalculate();
			EmiStackInteraction hovered = getHoveredStack((int) mouseX, (int) mouseY, false);
			if (hovered.getStack() != pressedStack && !(pressedStack instanceof EmiFavorite.Synthetic)) {
				draggedStack = pressedStack;
			}
		}
		return false;
	}

	public static boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (isDisabled()) {
			if (EmiConfig.toggleVisibility.matchesKey(keyCode, scanCode)) {
				EmiConfig.enabled = !EmiConfig.enabled;
				EmiConfig.writeConfig();
				return true;
			}
			return false;
		}
		if (EmiScreenManager.search.keyPressed(keyCode, scanCode, modifiers) || EmiScreenManager.search.isActive()) {
			return true;
		} else if (EmiUtil.isControlDown() && keyCode == GLFW.GLFW_KEY_Y) {
			EmiApi.displayAllRecipes();
			return true;
		} else {
			recalculate();
			if (stackInteraction(getHoveredStack(lastMouseX, lastMouseY, true),
					bind -> bind.matchesKey(keyCode, scanCode))) {
				return true;
			}
			if (genericInteraction(bind -> bind.matchesKey(keyCode, scanCode))) {
				return true;
			}
		}
		return false;
	}

	public static boolean genericInteraction(Function<EmiBind, Boolean> function) {
		if (function.apply(EmiConfig.toggleVisibility)) {
			EmiConfig.enabled = !EmiConfig.enabled;
			EmiConfig.writeConfig();
			return true;
		}
		boolean searchBreak = false;
		if (function.apply(EmiConfig.focusSearch)) {
			if (client.currentScreen != null) {
				client.currentScreen.setFocused(search);
				search.setTextFieldFocused(true);
				searchBreak = true;
			}
		}
		if (function.apply(EmiConfig.clearSearch)) {
			search.setText("");
			searchBreak = true;
		}
		if (searchBreak) {
			return true;
		}
		if (function.apply(EmiConfig.viewTree)) {
			EmiApi.viewRecipeTree();
			return true;
		} else if (function.apply(EmiConfig.back)) {
			if (!EmiHistory.isEmpty()) {
				EmiHistory.pop();
				return true;
			}
		}
		return false;
	}

	public static boolean stackInteraction(EmiStackInteraction stack, Function<EmiBind, Boolean> function) {
		EmiIngredient ingredient = stack.getStack();
		EmiRecipe context = EmiApi.getRecipeContext(ingredient);
		if (!ingredient.isEmpty()) {
			if (craftInteraction(ingredient, () -> context, stack, function)) {
				return true;
			}
			if (EmiConfig.cheatMode) {
				if (ingredient.getEmiStacks().size() == 1 && stack instanceof SidebarEmiStackInteraction) {
					if (function.apply(EmiConfig.cheatOneToInventory)) {
						return give(ingredient.getEmiStacks().get(0), 1, 0);
					} else if (function.apply(EmiConfig.cheatStackToInventory)) {
						return give(ingredient.getEmiStacks().get(0),
								ingredient.getEmiStacks().get(0).getItemStack().getMaxCount(), 0);
					} else if (function.apply(EmiConfig.cheatOneToCursor)) {
						return give(ingredient.getEmiStacks().get(0), 1, 1);
					} else if (function.apply(EmiConfig.cheatStackToCursor)) {
						return give(ingredient.getEmiStacks().get(0),
								ingredient.getEmiStacks().get(0).getItemStack().getMaxCount(), 1);
					}
				}
			}
			if (function.apply(EmiConfig.viewRecipes)) {
				EmiApi.displayRecipes(ingredient);
				if (stack.getRecipeContext() != null) {
					EmiApi.focusRecipe(stack.getRecipeContext());
				}
				return true;
			} else if (function.apply(EmiConfig.viewUses)) {
				EmiApi.displayUses(ingredient);
				return true;
			} else if (function.apply(EmiConfig.favorite)) {
				EmiFavorites.addFavorite(ingredient, stack.getRecipeContext());
				forPanel(SidebarType.FAVORITES, panel -> panel.batcher.repopulate());
				return true;
			} else if (function.apply(EmiConfig.viewStackTree) && stack.getRecipeContext() != null) {
				BoM.setGoal(stack.getRecipeContext());
				EmiApi.viewRecipeTree();
				return true;
			}
			Supplier<EmiRecipe> supplier = () -> {
				return EmiUtil.getPreferredRecipe(EmiUtil.getValidRecipes(ingredient, lastPlayerInventory, true));
			};
			if (craftInteraction(ingredient, supplier, stack, function)) {
				return true;
			}
		}
		return false;
	}

	private static boolean craftInteraction(EmiIngredient ingredient, Supplier<EmiRecipe> contextSupplier,
			EmiStackInteraction stack, Function<EmiBind, Boolean> function) {
		if (!(stack instanceof SidebarEmiStackInteraction)) {
			return false;
		}
		EmiFillAction action = null;
		boolean all = false;
		if (function.apply(EmiConfig.craftAllToInventory)) {
			action = EmiFillAction.QUICK_MOVE;
			all = true;
		} else if (function.apply(EmiConfig.craftOneToInventory)) {
			action = EmiFillAction.QUICK_MOVE;
		} else if (function.apply(EmiConfig.craftOneToCursor)) {
			action = EmiFillAction.CURSOR;
		} else if (function.apply(EmiConfig.craftAll)) {
			action = EmiFillAction.FILL;
			all = true;
		} else if (function.apply(EmiConfig.craftOne)) {
			action = EmiFillAction.FILL;
		}
		if (action != null) {
			EmiRecipe context = contextSupplier.get();
			if (context != null) {
				if (EmiConfig.miscraftPrevention) {
					SidebarPanel panel = getHoveredPanel(lastMouseX, lastMouseY);
					if (panel != null && panel.getType() == SidebarType.CRAFTABLES) {
						lastHoveredCraftableOffset = panel.space.getRawOffsetFromMouse(lastMouseX, lastMouseY);
						if (lastHoveredCraftableOffset != -1) {
							lastHoveredCraftableSturdy = lastHoveredCraftable != null;
							lastHoveredCraftable = stack;
							if (!all) {
								lastHoveredCraftableSturdy = true;
							}
						}
					}
				}
				int amount = all ? Integer.MAX_VALUE : 1;
				if (stack.getStack() instanceof EmiFavorite.Synthetic syn) {
					amount = Math.min(amount, (int) syn.amount);
				}
				if (EmiApi.performFill(context, action, amount)) {
					MinecraftClient.getInstance().getSoundManager()
							.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
					return true;
				}
			}
		}
		return false;
	}

	private static boolean give(EmiStack stack, int amount, int mode) {
		if (EmiClient.onServer) {
			if (stack.getItemStack().isEmpty()) {
				return false;
			}
			ItemStack is = stack.getItemStack().copy();
			if (mode == 1 && client.player.getAbilities().creativeMode) {
				client.player.currentScreenHandler.setCursorStack(is);
				return true;
			}
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeByte(mode);
			is.setCount(amount);
			buf.writeItemStack(is);
			ClientPlayNetworking.send(EmiMain.CREATE_ITEM, buf);
			return true;
		} else {
			ItemStack is = stack.getItemStack();
			if (!is.isEmpty()) {
				Identifier id = Registry.ITEM.getId(is.getItem());
				String command = "/give @s " + id;
				if (is.hasNbt()) {
					command += is.getNbt().toString();
				}
				command += " " + amount;
				if (command.length() < 256) {
					// client.world.sendPacket(new ChatMessageC2SPacket(command));
					return true;
				}
			}
			return false;
		}
	}

	public static class SidebarPanel {
		public final SidebarSide side;
		public final StackBatcher batcher = new StackBatcher();
		public final SizedButtonWidget pageLeft, pageRight;
		public final SidebarButtonWidget cycle;
		public SidebarPages pages;
		public int sidebarPage;
		public ScreenSpace space;
		public int page;

		public SidebarPanel(SidebarSide side, SidebarPages pages) {
			this.side = side;
			this.pages = pages;
			pageLeft = new SizedButtonWidget(0, 0, 16, 16, 224, 64, this::hasMultiplePages, (w) -> scroll(-1));
			pageRight = new SizedButtonWidget(0, 0, 16, 16, 240, 64, this::hasMultiplePages, (w) -> scroll(1));
			cycle = new SidebarButtonWidget(0, 0, 16, 16, this);
		}

		public SidebarType getType() {
			if (sidebarPage >= 0 && sidebarPage < pages.pages.size()) {
				return pages.pages.get(sidebarPage).type;
			}
			return SidebarType.NONE;
		}

		public boolean supportsType(SidebarType type) {
			for (SidebarPages.SidebarPage page : pages.pages) {
				if (page.type == type) {
					return true;
				}
			}
			return false;
		}

		public void setSidebarPage(int page) {
			if (page == sidebarPage) {
				return;
			}
			boolean forceRecalculate = getType() == SidebarType.CHESS;
			this.sidebarPage = page;
			forceRecalculate |= getType() == SidebarType.CHESS;
			if (forceRecalculate) {
				if (client.currentScreen != null) {
					// Force recalculation
					lastWidth = -1;
					recalculate();
				}
			}
			if (isSearch()) {
				EmiSearch.search(search.getText());
			}
			batcher.repopulate();
		}

		public void setType(SidebarType type) {
			for (int i = 0; i < pages.pages.size(); i++) {
				SidebarPages.SidebarPage page = pages.pages.get(i);
				if (page.type == type) {
					setSidebarPage(i);
				}
			}
		}

		public void cycleType(int amount) {
			int page = sidebarPage + amount;
			if (page >= pages.pages.size()) {
				page = 0;
			} else if (page < 0) {
				page = Math.max(pages.pages.size() - 1, 0);
			}
			setSidebarPage(page);
		}

		public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
			cycleType(0);
			if (getType() == SidebarType.CHESS) {
				EmiChess.get().update();
				if (space.tw != 8 || space.th != 8) {
					cycleType(1);
				}
			}
			if (isVisible()) {
				client.getProfiler().swap(side.getName());
				matrices.push();
				matrices.translate(0, 0, 100);
				pageLeft.render(matrices, mouseX, mouseY, delta);
				cycle.render(matrices, mouseX, mouseY, delta);
				pageRight.render(matrices, mouseX, mouseY, delta);
				matrices.pop();
				int totalPages = (getStacks().size() - 1) / space.pageSize + 1;
				wrapPage();
				space.render(matrices, mouseX, mouseY, delta, batcher, page, totalPages, getStacks(),
						space.pageSize * page);
			}
		}

		private void wrapPage() {
			int totalPages = (getStacks().size() - 1) / space.pageSize + 1;
			if (page >= totalPages) {
				page = 0;
				batcher.repopulate();
			} else if (page < 0) {
				page = totalPages - 1;
				batcher.repopulate();
			}
		}

		public boolean isSearch() {
			return side == SidebarSide.RIGHT;
		}

		public List<? extends EmiIngredient> getStacks() {
			if (isSearch() && getType() != SidebarType.CHESS) {
				return searchedStacks;
			} else {
				return getFromSidebarType(getType());
			}
		}

		public static List<? extends EmiIngredient> getFromSidebarType(SidebarType type) {
			if (type != null) {
				return switch (type) {
					case NONE -> List.of();
					case INDEX -> EmiStackList.stacks;
					case CRAFTABLES -> lastPlayerInventory == null ? List.of() : craftables;
					case FAVORITES -> EmiFavorites.favoriteSidebar;
					case CHESS -> EmiChess.SIDEBAR;
				};
			}
			return List.of();
		}

		public void updateWidgetPosition() {
			pageLeft.x = space.tx;
			pageLeft.y = space.ty - 18;
			pageRight.x = space.tx + space.tw * ENTRY_SIZE - 16;
			pageRight.y = pageLeft.y;
			cycle.x = space.tx + 18;
			cycle.y = pageLeft.y - 1;
		}

		public boolean isVisible() {
			if (getType() == SidebarType.CHESS && (space.tw != 8 || space.th != 8)) {
				return false;
			}
			return !isDisabled() && space.pageSize > 0 && pages.pages.size() > 0;
		}

		public void updateWidgetVisibility() {
			boolean visible = space.header && isVisible();

			pageLeft.visible = visible;
			cycle.visible = visible;
			pageRight.visible = visible;
		}

		public boolean hasMultiplePages() {
			return getStacks().size() > space.pageSize;
		}

		public void scroll(int delta) {
			if (space.pageSize == 0) {
				return;
			}
			page += delta;
			int pageSize = space.pageSize;
			int totalPages = (getStacks().size() - 1) / pageSize + 1;
			if (totalPages <= 1) {
				return;
			}
			if (page >= totalPages) {
				page = 0;
			} else if (page < 0) {
				page = totalPages - 1;
			}
			batcher.repopulate();
		}

		public Bounds getBounds() {
			int headerOffset = (space.header ? 18 : 0);
			return new Bounds(
					space.tx - space.theme.horizontalPadding,
					space.ty - space.theme.verticalPadding - headerOffset,
					space.tw * ENTRY_SIZE + space.theme.horizontalPadding * 2,
					space.th * ENTRY_SIZE + space.theme.verticalPadding * 2 + headerOffset);
		}
	}

	private static class ScreenSpace {
		public final SidebarTheme theme;
		public final int tx, ty, tw, th;
		public final int pageSize;
		public final boolean rtl;
		public final int[] widths;
		public final boolean header;

		public ScreenSpace(int tx, int ty, int tw, int th, boolean rtl, List<Bounds> exclusion, SidebarTheme theme,
				boolean header) {
			this.tx = tx;
			this.ty = ty;
			this.tw = tw;
			this.th = th;
			this.rtl = rtl;
			this.theme = theme;
			this.header = header;
			int[] widths = new int[th];
			int pageSize = 0;
			for (int y = 0; y < th; y++) {
				int width = 0;
				int cy = ty + y * ENTRY_SIZE;
				outer: for (int x = 0; x < tw; x++) {
					int cx = tx + (rtl ? (tw - 1 - x) : x) * ENTRY_SIZE;
					int rx = cx + ENTRY_SIZE - 1;
					int ry = cy + ENTRY_SIZE - 1;
					for (Bounds rect : exclusion) {
						if (rect.contains(cx, cy) || rect.contains(rx, cy) || rect.contains(cx, ry)
								|| rect.contains(rx, ry)) {
							break outer;
						}
					}
					width++;
				}
				widths[y] = width;
				pageSize += width;
			}
			this.pageSize = pageSize;
			this.widths = widths;
		}

		public void render(MatrixStack matrices, int mouseX, int mouseY, float delta, StackBatcher batcher,
				int page, int totalPages, List<? extends EmiIngredient> stacks, int startIndex) {
			if (this.pageSize > 0) {
				RenderSystem.enableDepthTest();
				RenderSystem.setShader(GameRenderer::getPositionTexShader);
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
				int headerOffset = header ? 18 : 0;
				if (theme == SidebarTheme.VANILLA) {
					RenderSystem.setShaderTexture(0, EmiRenderHelper.BACKGROUND);
					EmiRenderHelper.drawNinePatch(matrices, tx - 9, ty - 9 - headerOffset, tw * ENTRY_SIZE + 18,
							th * ENTRY_SIZE + 18 + headerOffset, 0, 32, 8, 1);
				} else if (theme == SidebarTheme.MODERN) {
					RenderSystem.setShaderTexture(0, EmiRenderHelper.GRID);
					RenderSystem.enableBlend();
					DrawableHelper.drawTexture(matrices, tx, ty, tw * ENTRY_SIZE, th * ENTRY_SIZE, 0, 0, tw, th, 2, 2);
					RenderSystem.disableBlend();
				}
				int hx = -1, hy = -1;
				batcher.begin(this.tx + PADDING_SIZE, this.ty + PADDING_SIZE, 0);
				if (header) {
					Text text = EmiRenderHelper.getPageText(page + 1, totalPages, (this.tw - 3) * ENTRY_SIZE);
					int x = this.tx + (this.tw * ENTRY_SIZE) / 2;
					int maxLeft = (this.tw - 2) * ENTRY_SIZE / 2 - ENTRY_SIZE;
					int w = client.textRenderer.getWidth(text) / 2;
					if (w > maxLeft) {
						x += (w - maxLeft);
					}
					DrawableHelper.drawCenteredText(matrices, client.textRenderer, text, x, ty - 15, 0xFFFFFF);
					if (totalPages > 1 && this.tw > 2) {
						int scrollLeft = this.tx + 18;
						int scrollWidth = this.tw * ENTRY_SIZE - 36;
						int scrollY = this.ty - 4;
						int start = scrollLeft + scrollWidth * page / totalPages;
						int end = start + Math.max(scrollWidth / totalPages, 1);
						if (page == totalPages - 1) {
							end = scrollLeft + scrollWidth;
							start = end - Math.max(scrollWidth / totalPages, 1);
						}
						DrawableHelper.fill(matrices, scrollLeft, scrollY, scrollLeft + scrollWidth, scrollY + 2,
								0x55555555);
						DrawableHelper.fill(matrices, start, scrollY, end, scrollY + 2, 0xFFFFFFFF);
					}
				}
				int i = startIndex;
				outer: for (int yo = 0; yo < this.th; yo++) {
					for (int xo = 0; xo < this.getWidth(yo); xo++) {
						if (i >= stacks.size()) {
							break outer;
						}
						int cx = this.getX(xo, yo);
						int cy = this.getY(xo, yo);
						EmiIngredient stack = stacks.get(i++);
						batcher.render(stack, matrices, cx + 1, cy + 1, delta);
						if (EmiConfig.highlightDefaulted) {
							if (BoM.getRecipe(stack) != null) {
								RenderSystem.enableDepthTest();
								DrawableHelper.fill(matrices, cx, cy, cx + ENTRY_SIZE, cy + ENTRY_SIZE, 0x3300ff00);
							}
						}
						if (EmiConfig.showHoverOverlay
								&& mouseX >= cx && mouseY >= cy && mouseX < cx + ENTRY_SIZE
								&& mouseY < cy + ENTRY_SIZE) {
							hx = cx;
							hy = cy;
						}
					}
				}
				batcher.draw();
				if (hx != -1 && hx != -1) {
					RenderSystem.enableDepthTest();
					EmiRenderHelper.drawSlotHightlight(matrices, hx, hy, ENTRY_SIZE, ENTRY_SIZE);
				}
			}
		}

		public int getWidth(int y) {
			return widths[y];
		}

		public int getX(int x, int y) {
			return tx + (rtl ? x + tw - getWidth(y) : x) * ENTRY_SIZE;
		}

		public int getY(int x, int y) {
			return ty + y * ENTRY_SIZE;
		}

		public int getEdgeX(int off) {
			int t = 0;
			int y = 0;
			while (y < th && t + getWidth(y) < off) {
				t += getWidth(y++);
			}
			return getX(off - t, y);
		}

		public int getEdgeY(int off) {
			int t = 0;
			int y = 0;
			while (y < th && t + getWidth(y) < off) {
				t += getWidth(y++);
			}
			return ty + y * ENTRY_SIZE;
		}

		public int getRawX(int off) {
			int t = 0;
			int y = 0;
			while (y < th && t + getWidth(y) <= off) {
				t += getWidth(y++);
			}
			return getX(off - t, y);
		}

		public int getRawY(int off) {
			int t = 0;
			int y = 0;
			while (y < th && t + getWidth(y) <= off) {
				t += getWidth(y++);
			}
			return ty + y * ENTRY_SIZE;
		}

		public int getClosestEdge(int x, int y) {
			if (y < ty) {
				return 0;
			} else if (y >= ty + th * ENTRY_SIZE) {
				return pageSize;
			} else {
				x = (x - tx) / ENTRY_SIZE;
				y = (y - ty) / ENTRY_SIZE;
				int off = 0;
				for (int i = 0; i < y; i++) {
					off += widths[i];
				}
				if (x < 0) {
					return y;
				} else if (x >= widths[y]) {
					return y + widths[y];
				}
				if (rtl) {
					int to = tw - widths[y];
					if (x >= to) {
						off += x - to;
					}
				} else {
					if (x < widths[y]) {
						off += x;
					} else {
						off += widths[y];
					}
				}
				return off;
			}
		}

		public int getRawOffsetFromMouse(int mouseX, int mouseY) {
			if (mouseX < tx || mouseY < ty) {
				return -1;
			}
			return getRawOffset((mouseX - tx) / ENTRY_SIZE, (mouseY - ty) / ENTRY_SIZE);
		}

		public int getRawOffset(int x, int y) {
			if (x >= 0 && y >= 0 && x < tw && y < th) {
				int off = 0;
				for (int i = 0; i < y; i++) {
					off += widths[i];
				}
				if (rtl) {
					int to = tw - widths[y];
					if (x >= to) {
						return off + x - to;
					}
				} else {
					if (x < widths[y]) {
						return off + x;
					}
				}
			}
			return -1;
		}

		public boolean contains(int x, int y) {
			return x >= tx && x < tx + tw * ENTRY_SIZE && y >= ty && y < ty + th * ENTRY_SIZE;
		}

		public boolean containsNotExcluded(int x, int y) {
			if (this.contains(lastMouseX, lastMouseY)) {
				for (Bounds bounds : EmiExclusionAreas.getExclusion(client.currentScreen)) {
					if (bounds.contains(x, y)) {
						return false;
					}
				}
				return true;
			}
			return false;
		}
	}

	public static class SidebarEmiStackInteraction extends EmiStackInteraction {

		public SidebarEmiStackInteraction(EmiIngredient stack) {
			super(stack);
		}

		public SidebarEmiStackInteraction(EmiIngredient stack, EmiRecipe recipe, boolean clickable) {
			super(stack, recipe, clickable);
		}
	}
}
