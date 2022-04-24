package dev.emi.emi.screen;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.lwjgl.glfw.GLFW;

import dev.emi.emi.EmiConfig;
import dev.emi.emi.EmiExclusionAreas;
import dev.emi.emi.EmiFavorite;
import dev.emi.emi.EmiFavorites;
import dev.emi.emi.EmiLog;
import dev.emi.emi.EmiMain;
import dev.emi.emi.EmiReloadManager;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.EmiFillAction;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.bind.EmiBind;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.mixin.accessor.HandledScreenAccessor;
import dev.emi.emi.mixin.accessor.ScreenAccessor;
import dev.emi.emi.screen.widget.EmiSearchWidget;
import dev.emi.emi.search.EmiSearch;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class EmiScreenManager {
	private static final int ENTRY_SIZE = 18;
	private static MinecraftClient client = MinecraftClient.getInstance();
	private static List<EmiStack> stacks;
	/*package*/ static int lastMouseX, lastMouseY;
	private static int left, right;
	private static int lastWidth, lastHeight;
	private static List<Rect2i> lastExclusion;
	private static ScreenSpace searchSpace;
	private static ScreenSpace favoriteSpace;
	public static EmiSearchWidget search = new EmiSearchWidget(client.textRenderer, 0, 0, 160, 18);;
	public static int currentPage = 0;
	private static final StackBatcher searchBatcher = new StackBatcher();
	private static final StackBatcher favoriteBatcher = new StackBatcher();

	private static boolean isDisabled() {
		return EmiReloadManager.isReloading() || !EmiConfig.enabled;
	}

	private static void recalculate() {
		if (stacks != EmiSearch.stacks) {
			searchBatcher.repopulate();
		}
		stacks = EmiSearch.stacks;
		Screen screen = client.currentScreen;
		List<Rect2i> exclusion = EmiExclusionAreas.getExclusion(screen);
		if (lastWidth == screen.width && lastHeight == screen.height && exclusion.size() == lastExclusion.size()) {
			boolean same = true;
			for (int i = 0; i < exclusion.size(); i++) {
				Rect2i a = exclusion.get(i);
				Rect2i b = lastExclusion.get(i);
				if (a.getX() != b.getX() || a.getY() != b.getY() || a.getWidth() != b.getWidth() || a.getHeight() != b.getHeight()) {
					same = false;
					break;
				}
			}
			if (same) {
				return;
			}
		}
		searchBatcher.repopulate();
		favoriteBatcher.repopulate();
		lastWidth = screen.width;
		lastHeight = screen.height;
		lastExclusion = exclusion;
		if (screen instanceof EmiScreen emi) {
			left = emi.emi$getLeft();
			right = emi.emi$getRight();
			int xMin = right;
			int xMax = screen.width;
			int yMin = 16;
			int yMax = screen.height - 2;
			int tx = xMin + (xMax - xMin) % ENTRY_SIZE / 2;
			int ty = yMin;
			int tw = (xMax - xMin) / ENTRY_SIZE;
			int th = (yMax - yMin) / ENTRY_SIZE;
			searchSpace = new ScreenSpace(xMin, xMax, yMin, yMax, tx, ty, tw, th, true, exclusion);
			int fxMin = 0;
			int fxMax = left;
			int fyMin = 16;
			int fyMax = screen.height - 2;
			int ftx = fxMin + (fxMax - fxMin) % ENTRY_SIZE / 2;
			int fty = fyMin;
			int ftw = (fxMax - fxMin) / ENTRY_SIZE;
			int fth = (fyMax - fyMin) / ENTRY_SIZE;
			favoriteSpace = new ScreenSpace(fxMin, fxMax, fyMin, fyMax, ftx, fty, ftw, fth, false, exclusion);
		}
	}

	private static EmiIngredient getHoveredStack(int mouseX, int mouseY, boolean checkHandled) {
		Screen screen = client.currentScreen;
		if (checkHandled && screen instanceof HandledScreenAccessor handled) {
			Slot s = handled.getFocusedSlot();
			if (s != null) {
				ItemStack stack = s.getStack();
				if (!stack.isEmpty()) {
					return EmiStack.of(stack);
				}
			}
		}
		if (searchSpace.contains(mouseX, mouseY) && mouseX >= searchSpace.tx && mouseY >= searchSpace.ty) {
			int x = (mouseX - searchSpace.tx) / ENTRY_SIZE;
			int y = (mouseY - searchSpace.ty) / ENTRY_SIZE;
			int n = searchSpace.getRawOffset(x, y);
			if (n != -1 && (n = n + searchSpace.pageSize * currentPage) < stacks.size()) {
				return stacks.get(n);
			}
		}
		if (favoriteSpace.contains(mouseX, mouseY) && mouseX >= favoriteSpace.tx && mouseY >= favoriteSpace.ty) {
			int x = (mouseX - favoriteSpace.tx) / ENTRY_SIZE;
			int y = (mouseY - favoriteSpace.ty) / ENTRY_SIZE;
			int n = favoriteSpace.getRawOffset(x, y);
			if (n != -1 && (n = n + favoriteSpace.pageSize * 0) < EmiFavorites.favorites.size()) {
				return EmiFavorites.favorites.get(n);
			}
		}
		return EmiStack.EMPTY;
	}
	
	public static void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		client.getProfiler().push("emi");
		lastMouseX = mouseX;
		lastMouseY = mouseY;
		Screen screen = client.currentScreen;
		if (isDisabled()) {
			if (EmiReloadManager.isReloading()) {
				client.textRenderer.drawWithShadow(matrices, "EMI Reloading...", 4, screen.height - 12, -1);
			}
			client.getProfiler().pop();
			return;
		}
		client.getProfiler().push("sidebars");
		if (screen instanceof EmiScreen emi) {
			client.getProfiler().push("prep");
			EmiScreenManager.search.setZOffset(0);
			EmiScreenManager.search.render(matrices, mouseX, mouseY, delta);
			recalculate();
			int pageSize = searchSpace.pageSize;
			int totalPages = (stacks.size() - 1) / pageSize + 1;
			if (currentPage >= totalPages) {
				currentPage = totalPages - 1;
				searchBatcher.repopulate();
			} else if (currentPage < 0) {
				currentPage = 0;
				searchBatcher.repopulate();
			}
	
			client.getProfiler().swap("search");
			searchBatcher.begin(searchSpace.xMin, searchSpace.yMin, 0);
			DrawableHelper.drawCenteredText(matrices, client.textRenderer, new TranslatableText("emi.page", currentPage + 1, totalPages),
				searchSpace.xMin + (searchSpace.xMax - searchSpace.xMin) / 2, 5, 0xFFFFFF);
			int i = pageSize * currentPage;
			outer:
			for (int yo = 0; yo < searchSpace.th; yo++) {
				for (int xo = 0; xo < searchSpace.getWidth(yo); xo++) {
					if (i >= stacks.size()) {
						break outer;
					}
					int cx = searchSpace.getX(xo, yo);
					int cy = searchSpace.getY(xo, yo);
					EmiStack stack = stacks.get(i++);
					searchBatcher.render(stack, matrices, cx + 1, cy + 1, delta);
					if (EmiConfig.devMode) {
						if (BoM.getRecipe(stack) != null) {
							DrawableHelper.fill(matrices, cx, cy, cx + ENTRY_SIZE, cy + ENTRY_SIZE, 0x3300ff00);
						}
					}
				}
			}
			searchBatcher.draw();
			client.getProfiler().swap("favorite");
			favoriteBatcher.begin(favoriteSpace.xMin, favoriteSpace.yMin, 0);
			i = 0;
			outer:
			for (int yo = 0; yo < favoriteSpace.th; yo++) {
				for (int xo = 0; xo < favoriteSpace.getWidth(yo); xo++) {
					if (i >= EmiFavorites.favorites.size()) {
						break outer;
					}
					int cx = favoriteSpace.getX(xo, yo);
					int cy = favoriteSpace.getY(xo, yo);
					favoriteBatcher.render(EmiFavorites.favorites.get(i++).getStack(), matrices, cx + 1, cy + 1, delta);
				}
			}
			favoriteBatcher.draw();
			client.getProfiler().swap("hover");
			EmiIngredient hov = getHoveredStack(mouseX, mouseY, false);
			((ScreenAccessor) screen).invokeRenderTooltipFromComponents(matrices, hov.getTooltip(), mouseX, mouseY);
			client.getProfiler().pop();
		}
		if (EmiConfig.devMode) {
			client.getProfiler().swap("dev");
			int color = 0xFFFFFF;
			String title = "EMI Dev Mode";
			if (EmiLog.WARNINGS.size() > 0) {
				color = 0xFF0000;
				String warnCount = EmiLog.WARNINGS.size() + " Warnings";
				int width = Math.max(client.textRenderer.getWidth(title), client.textRenderer.getWidth(warnCount));
				if (mouseX < width + 8 && mouseY > screen.height - 28) {
					screen.renderTooltip(matrices, Stream.concat(Stream.of("See log for more information"),
						EmiLog.WARNINGS.stream()).map(s -> {
							String a = s;
							if (a.length() > 10 && client.textRenderer.getWidth(a) > screen.width - 20) {
								a = client.textRenderer.trimToWidth(a, screen.width - 30) + "...";
							}
							return new LiteralText(a);
						})
						.collect(Collectors.toList()), 0, 20);
				}
				client.textRenderer.drawWithShadow(matrices, warnCount, 4, screen.height - 24, color);
			}
			client.textRenderer.drawWithShadow(matrices, title, 4, screen.height - 12, color);
		}
		client.getProfiler().pop();
		client.getProfiler().pop();
	}

	public static boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		if (isDisabled()) {
			return false;
		}
		recalculate();
		if (searchSpace.contains((int) mouseX, (int) mouseY)) {
			EmiScreenManager.currentPage += (int) -amount;
			searchBatcher.repopulate();
			return true;
		}
		return false;
	}

	public static boolean mouseClicked(double mouseX, double mouseY, int button) {
		// TODO This makes sure focus always goes away, but might double fire, is that a problem?
		EmiScreenManager.search.mouseClicked(mouseX, mouseY, button);
		if (isDisabled()) {
			if (EmiConfig.toggleVisibility.matchesMouse(button)) {
				EmiConfig.enabled = !EmiConfig.enabled;
				EmiConfig.writeConfig();
				return true;
			}
			return false;
		}
		recalculate();
		if (EmiConfig.cheatMode) {
			if (client.currentScreen instanceof HandledScreen<?> handled) {
				ItemStack cursor = handled.getScreenHandler().getCursorStack();
				if (!cursor.isEmpty() && searchSpace.contains(lastMouseX, lastMouseY)) {
					handled.getScreenHandler().setCursorStack(ItemStack.EMPTY);
					ClientPlayNetworking.send(EmiMain.DESTROY_HELD, new PacketByteBuf(Unpooled.buffer()));
					return true;
				}
			}
		}
		if (stackInteraction(getHoveredStack((int) mouseX, (int) mouseY, false), bind -> bind.matchesMouse(button))) {
			return true;
		}
		if (genericInteraction(bind -> bind.matchesMouse(button))) {
			return true;
		}
		return false;
	}

	public static boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (isDisabled()) {
			return false;
		}
		recalculate();
		EmiIngredient ingredient = getHoveredStack((int) mouseX, (int) mouseY, false);
		if (!ingredient.isEmpty()) {
			return true;
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
		} else if (EmiUtil.isControlDown() && keyCode == GLFW.GLFW_KEY_C) {
			client.setScreen(new ConfigScreen(client.currentScreen));
			return true;
		} else if (EmiUtil.isControlDown() && keyCode == GLFW.GLFW_KEY_Y) {
			EmiApi.displayAllRecipes();
			return true;
		} else {
			recalculate();
			if (stackInteraction(getHoveredStack(lastMouseX, lastMouseY, true), bind -> bind.matchesKey(keyCode, scanCode))) {
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
		} else if (function.apply(EmiConfig.focusSearch)) {
			if (client.currentScreen != null) {
				client.currentScreen.setFocused(search);
				search.setTextFieldFocused(true);
				return true;
			}
		} else if (function.apply(EmiConfig.viewTree)) {
			EmiApi.viewRecipeTree();
			return true;
		}
		return false;
	}

	public static boolean stackInteraction(EmiIngredient ingredient, Function<EmiBind, Boolean> function) {
		return stackInteraction(ingredient, null, function);
	}

	public static boolean stackInteraction(EmiIngredient ingredient, EmiRecipe recipe, Function<EmiBind, Boolean> function) {
		if (!ingredient.isEmpty()) {
			if (ingredient instanceof EmiFavorite fav && fav.getRecipe() != null) {
				if (function.apply(EmiConfig.craftAllToInventory)) {
					EmiApi.performFill(fav.getRecipe(), EmiFillAction.QUICK_MOVE, true);
					return true;
				} else if (function.apply(EmiConfig.craftOneToInventory)) {
					EmiApi.performFill(fav.getRecipe(), EmiFillAction.QUICK_MOVE, false);
					return true;
				} else if (function.apply(EmiConfig.craftOneToCursor)) {
					EmiApi.performFill(fav.getRecipe(), EmiFillAction.CURSOR, false);
					return true;
				} else if (function.apply(EmiConfig.craftAll)) {
					EmiApi.performFill(fav.getRecipe(), EmiFillAction.FILL, true);
					return true;
				} else if (function.apply(EmiConfig.craftAll)) {
					EmiApi.performFill(fav.getRecipe(), EmiFillAction.FILL, false);
					return true;
				}
			}
			if (EmiConfig.cheatMode) {
				if (ingredient.getEmiStacks().size() == 1) {
					if (function.apply(EmiConfig.cheatOne)) {
						return give(ingredient.getEmiStacks().get(0), 1);
					} else if (function.apply(EmiConfig.cheatStack)) {
						return give(ingredient.getEmiStacks().get(0), ingredient.getEmiStacks().get(0).getItemStack().getMaxCount());
					}
				}
			}
			if (function.apply(EmiConfig.viewRecipes)) {
				EmiApi.displayRecipes(ingredient);
				if (recipe != null) {
					EmiApi.focusRecipe(recipe);
				}
				return true;
			} else if (function.apply(EmiConfig.viewUses)) {
				EmiApi.displayUses(ingredient);
				return true;
			} else if (function.apply(EmiConfig.favorite)) {
				EmiFavorites.addFavorite(ingredient, recipe);
				favoriteBatcher.repopulate();
				return true;
			}
		}
		return false;
	}

	// TODO make a custom packet and fall back to this if client only
	// Yes I'm just putting strings together
	private static boolean give(EmiStack stack, int amount) {
		ItemStack is = stack.getItemStack();
		if (!is.isEmpty()) {
			Identifier id = Registry.ITEM.getId(is.getItem());
			String command = "/give @s " + id;
			if (is.hasNbt()) {
				command += is.getNbt().toString();
			}
			command += " " + amount;
			if (command.length() < 256) {
				client.world.sendPacket(new ChatMessageC2SPacket(command));
				return true;
			}
		}
		return false;
	}

	private static class ScreenSpace {
		public final int xMin, xMax, yMin, yMax;
		public final int tx, ty, tw, th;
		public final int pageSize;
		public final boolean rtl;
		public final int[] widths;

		public ScreenSpace(int xMin, int xMax, int yMin, int yMax, int tx, int ty, int tw, int th, boolean rtl, List<Rect2i> exclusion) {
			this.xMin = xMin;
			this.xMax = xMax;
			this.yMin = yMin;
			this.yMax = yMax;
			this.tx = tx;
			this.ty = ty;
			this.tw = tw;
			this.th = th;
			this.rtl = rtl;
			int[] widths = new int[th];
			int pageSize = 0;
			for (int y = 0; y < th; y++) {
				int width = 0;
				int cy = ty + y * ENTRY_SIZE;
				outer:
				for (int x = 0; x < tw; x++) {
					int cx = tx + (rtl ? (tw - 1 - x) : x) * ENTRY_SIZE;
					int rx = cx + 18;
					int ry = cy + 18;
					for (Rect2i rect : exclusion) {
						if (rect.contains(cx, cy) || rect.contains(rx, cy) || rect.contains(cx, ry) || rect.contains(rx, ry)) {
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

		public int getWidth(int y) {
			return widths[y];
		}

		public int getX(int x, int y) {
			return tx + (rtl ? x + tw - getWidth(y) : x) * ENTRY_SIZE;
		}

		public int getY(int x, int y) {
			return ty + y * ENTRY_SIZE;
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
			return x >= xMin && x < xMax && y >= yMin && y < yMax;
		}
	}
}
