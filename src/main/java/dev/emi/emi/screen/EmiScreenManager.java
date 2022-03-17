package dev.emi.emi.screen;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.compress.utils.Lists;
import org.lwjgl.glfw.GLFW;

import dev.emi.emi.EmiFavorite;
import dev.emi.emi.EmiFavorites;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.mixin.accessor.HandledScreenAccessor;
import dev.emi.emi.mixin.accessor.ScreenAccessor;
import dev.emi.emi.search.EmiSearch;
import dev.emi.emi.search.QueryType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;

public class EmiScreenManager {
	private static final int ENTRY_SIZE = 18;
	private static final Pattern ESCAPE = Pattern.compile("\\\\.");
	private static MinecraftClient client = MinecraftClient.getInstance();
	private static List<Pair<Integer, Style>> styles;
	private static List<EmiStack> stacks;
	/*package*/ static int lastMouseX, lastMouseY;
	private static int left, right;
	private static int xMin, xMax, yMin, yMax;
	private static int tx, ty, tw, th;
	private static int fxMin, fxMax, fyMin, fyMax;
	private static int ftx, fty, ftw, fth;
	public static TextFieldWidget search;
	public static int currentPage = 0;

	static {
		search = new TextFieldWidget(client.textRenderer, 0, 0, 160, 18, new TranslatableText("emi.search"));
		search.setFocusUnlocked(true);
		search.setEditableColor(-1);
		search.setUneditableColor(-1);
		search.setDrawsBackground(true);
		search.setMaxLength(256);
		search.setRenderTextProvider((string, stringStart) -> {
			MutableText text = null;
			int s = 0;
			int last = 0;
			for (; s < styles.size(); s++) {
				Pair<Integer, Style> style = styles.get(s);
				int end = style.getLeft();
				if (end > stringStart) {
					if (end - stringStart >= string.length()) {
						text = new LiteralText(string.substring(0, string.length())).setStyle(style.getRight());
						// Skip second loop
						s = styles.size();
						break;
					}
					text = new LiteralText(string.substring(0, end - stringStart)).setStyle(style.getRight());
					last = end - stringStart;
					s++;
					break;
				}
			}
			for (; s < styles.size(); s++) {
				Pair<Integer, Style> style = styles.get(s);
				int end = style.getLeft();
				if (end - stringStart >= string.length()) {
					text.append(new LiteralText(string.substring(last, string.length())).setStyle(style.getRight()));
					break;
				}
				text.append(new LiteralText(string.substring(last, end - stringStart)).setStyle(style.getRight()));
				last = end - stringStart;
			}
			return text.asOrderedText();
		});
		search.setChangedListener(string -> {
			Matcher matcher = EmiSearch.TOKENS.matcher(string);
			List<Pair<Integer, Style>> styles = Lists.newArrayList();
			int last = 0;
			while (matcher.find()) {
				int start = matcher.start();
				int end = matcher.end();
				if (last < start) {
					styles.add(new Pair<Integer, Style>(start, Style.EMPTY.withFormatting(Formatting.WHITE)));
				}
				String group = matcher.group();
				QueryType type = QueryType.fromString(group);
				int subStart = type.prefix.length();
				if (group.length() > 1 + subStart && group.substring(subStart).startsWith("/") && group.endsWith("/")) {
					int rOff = start + subStart + 1;
					styles.add(new Pair<Integer, Style>(rOff, type.slashColor));
					Matcher rMatcher = ESCAPE.matcher(string.substring(rOff, end - 1));
					int rLast = 0;
					while (rMatcher.find()) {
						int rStart = rMatcher.start();
						int rEnd = rMatcher.end();
						if (rLast < rStart) {
							styles.add(new Pair<Integer, Style>(rStart + rOff, type.regexColor));
						}
						styles.add(new Pair<Integer, Style>(rEnd + rOff, type.escapeColor));
						rLast = rEnd;
					}
					if (rLast < end - 1) {
						styles.add(new Pair<Integer, Style>(end - 1, type.regexColor));
					}
					styles.add(new Pair<Integer, Style>(end, type.slashColor));
				} else {
					styles.add(new Pair<Integer, Style>(end, type.color));
				}

				last = end;
			}
			if (last < string.length()) {
				styles.add(new Pair<Integer, Style>(string.length(), Style.EMPTY.withFormatting(Formatting.WHITE)));
			}
			EmiScreenManager.styles = styles;
			
			EmiSearch.search(string);
		});
		search.setText("");
	}

	private static void recalculate() {
		Screen screen = client.currentScreen;
		if (screen instanceof EmiScreen emi) {
			stacks = EmiSearch.stacks;
			left = emi.emi$getLeft();
			right = emi.emi$getRight();
			xMin = right;
			xMax = screen.width;
			yMin = 16;
			yMax = screen.height - 2;
			tx = xMin + (xMax - xMin) % ENTRY_SIZE / 2;
			ty = yMin;
			tw = (xMax - xMin) / ENTRY_SIZE;
			th = (yMax - yMin) / ENTRY_SIZE;
			fxMin = 0;
			fxMax = left;
			fyMin = 16;
			fyMax = screen.height - 2;
			ftx = fxMin + (fxMax - fxMin) % ENTRY_SIZE / 2;
			fty = fyMin;
			ftw = (fxMax - fxMin) / ENTRY_SIZE;
			fth = (fyMax - fyMin) / ENTRY_SIZE;
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
		if (mouseX >= tx && mouseX < tx + tw * ENTRY_SIZE && mouseY >= ty && mouseY < ty + th * ENTRY_SIZE) {
			int x = (mouseX - tx) / ENTRY_SIZE;
			int y = (mouseY - ty) / ENTRY_SIZE;
			int pageSize = tw * th;
			int n = pageSize * currentPage + y * tw + x;
			if (n < stacks.size()) {
				return stacks.get(n);
			}
		}
		if (mouseX >= ftx && mouseX < ftx + ftw * ENTRY_SIZE && mouseY >= fty && mouseY < fty + fth * ENTRY_SIZE) {
			int x = (mouseX - ftx) / ENTRY_SIZE;
			int y = (mouseY - fty) / ENTRY_SIZE;
			int n = y * ftw + x;
			if (n < EmiFavorites.favorites.size()) {
				return EmiFavorites.favorites.get(n);
			}
		}
		return EmiStack.EMPTY;
	}
	
	public static void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		lastMouseX = mouseX;
		lastMouseY = mouseY;
		Screen screen = client.currentScreen;
		if (screen instanceof EmiScreen emi) {
			recalculate();
			int pageSize = tw * th;
			int totalPages = (stacks.size() - 1) / pageSize + 1;
			if (currentPage >= totalPages) {
				currentPage = totalPages - 1;
			} else if (currentPage < 0) {
				currentPage = 0;
			}
	
			DrawableHelper.drawCenteredText(matrices, client.textRenderer, new TranslatableText("emi.page", currentPage + 1, totalPages),
				xMin + (xMax - xMin) / 2, 5, 0xFFFFFF);
			int i = pageSize * currentPage;
			outer:
			for (int yo = 0; yo < th; yo++) {
				for (int xo = 0; xo < tw; xo++) {
					if (i >= stacks.size()) {
						break outer;
					}
					int cx = tx + xo * ENTRY_SIZE;
					int cy = ty + yo * ENTRY_SIZE;
					stacks.get(i++).renderIcon(matrices, cx + 1, cy + 1, delta);
				}
			}
			
			i = 0;
			outer: 
			for (int yo = 0; yo < fth; yo++) {
				for (int xo = 0; xo < ftw; xo++) {
					if (i >= EmiFavorites.favorites.size()) {
						break outer;
					}
					int cx = ftx + xo * ENTRY_SIZE;
					int cy = fty + yo * ENTRY_SIZE;
					EmiFavorites.favorites.get(i++).render(matrices, cx + 1, cy + 1, delta);
				}
			}

			EmiIngredient hov = getHoveredStack(mouseX, mouseY, false);
			((ScreenAccessor) screen).invokeRenderTooltipFromComponents(matrices, hov.getTooltip(), mouseX, mouseY);
	
			EmiScreenManager.search.render(matrices, mouseX, mouseY, delta);
		}
	}

	public static boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		recalculate();
		if (mouseX > xMin && mouseX < xMax && mouseY > yMin && mouseY < yMax) {
			EmiScreenManager.currentPage += (int) -amount;
			return true;
		}
		return false;
	}

	public static boolean mouseClicked(double mouseX, double mouseY, int button) {
		recalculate();
		EmiIngredient ingredient = getHoveredStack((int) mouseX, (int) mouseY, false);
		if (!ingredient.isEmpty()) {
			if (button == 0) {
				if (ingredient instanceof EmiFavorite fav && fav.getRecipe() != null) {
					EmiApi.performFill(fav.getRecipe(), EmiUtil.isShiftDown());
				} else {
					EmiApi.displayRecipes(ingredient);
				}
			} else if (button == 1) {
				EmiApi.displayUses(ingredient);
			}
			return true;
		}
		return false;
	}

	public static boolean mouseReleased(double mouseX, double mouseY, int button) {
		recalculate();
		EmiIngredient ingredient = getHoveredStack((int) mouseX, (int) mouseY, false);
		if (!ingredient.isEmpty()) {
			return true;
		}
		return false;
	}

	public static boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (EmiScreenManager.search.keyPressed(keyCode, scanCode, modifiers) || EmiScreenManager.search.isActive()) {
			return true;
		} else {
			recalculate();
			EmiIngredient ingredient = getHoveredStack(lastMouseX, lastMouseY, true);
			if (!ingredient.isEmpty()) {
				if (keyCode == GLFW.GLFW_KEY_R) {
					EmiApi.displayRecipes(ingredient);
				} else if (keyCode == GLFW.GLFW_KEY_U) {
					EmiApi.displayUses(ingredient);
				} else if (keyCode == GLFW.GLFW_KEY_A) {
					EmiFavorites.addFavorite(ingredient);
				} else if (keyCode == GLFW.GLFW_KEY_Y) {
					EmiApi.displayAllRecipes();
				}
			}
		}
		return false;
	}
}
