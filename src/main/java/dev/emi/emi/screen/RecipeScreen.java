package dev.emi.emi.screen;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.EmiClient;
import dev.emi.emi.EmiConfig;
import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRecipeFiller;
import dev.emi.emi.EmiRecipes;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.screen.widget.ResolutionButtonWidget;
import dev.emi.emi.screen.widget.SizedButtonWidget;
import dev.emi.emi.widget.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

public class RecipeScreen extends Screen implements EmiScreen {
	private static final Identifier TEXTURE = new Identifier("emi", "textures/gui/background.png");
	private static final int RECIPE_PADDING = 9;
	public static @Nullable EmiIngredient resolve = null;
	private Map<EmiRecipeCategory, List<EmiRecipe>> recipes;
	public HandledScreen<?> old;
	private List<RecipeTab> tabs = Lists.newArrayList();
	private int tabPageSize = 6;
	private int tabPage = 0, tab = 0, page = 0;
	private List<SizedButtonWidget> arrows;
	private List<WidgetGroup> currentPage = Lists.newArrayList();
	private int tabOff = 0;
	private Widget hoveredWidget = null;
	private ResolutionButtonWidget resolutionButton;
	private double scrollAcc = 0;
	int backgroundWidth = 176;
	int backgroundHeight = 200;
	int x = (this.width - backgroundWidth) / 2;
	int y = (this.height - backgroundHeight) / 2;

	public RecipeScreen(HandledScreen<?> old, Map<EmiRecipeCategory, List<EmiRecipe>> recipes) {
		super(EmiPort.translatable("screen.emi.recipe"));
		this.old = old;
		arrows = List.of(
			new SizedButtonWidget(x + 2, y - 18, 12, 12, 0, 64,
				() -> tabs.size() > tabPageSize, w -> setPage(tabPage - 1, tab, page)),
			new SizedButtonWidget(x + backgroundWidth - 14, y - 18, 12, 12, 12, 64,
				() -> tabs.size() > tabPageSize, w -> setPage(tabPage + 1, tab, page)),
			new SizedButtonWidget(x + 5, y + 5, 12, 12, 0, 64,
				() -> tabs.size() > 1, w -> setPage(tabPage, tab - 1, 0)),
			new SizedButtonWidget(x + backgroundWidth - 17, y + 5, 12, 12, 12, 64,
				() -> tabs.size() > 1, w -> setPage(tabPage, tab + 1, 0)),
			new SizedButtonWidget(x + 5, y + 18, 12, 12, 0, 64,
				() -> tabs.get(tab).recipes.size() > 1, w -> setPage(tabPage, tab, page - 1)),
			new SizedButtonWidget(x + backgroundWidth - 17, y + 18, 12, 12, 12, 64,
				() -> tabs.get(tab).recipes.size() > 1, w -> setPage(tabPage, tab, page + 1))
		);
		resolve = null;
		this.recipes = recipes;
	}

	@Override
	protected void init() {
		super.init();
		this.client.keyboard.setRepeatEvents(true);
		backgroundHeight = height - 52 - EmiConfig.verticalMargin;
		x = (this.width - backgroundWidth) / 2;
		y = (this.height - backgroundHeight) / 2 + 1;
		
		for (SizedButtonWidget widget : arrows) {
			addDrawableChild(widget);
		}
		EmiScreenManager.addWidgets(this);
		if (resolve != null) {
			resolutionButton = new ResolutionButtonWidget(x - 18, y + 10, 18, 18, resolve, () -> hoveredWidget);
			this.addDrawableChild(resolutionButton);
		}
		if (recipes != null) {
			EmiRecipe current = null;
			if (tab < tabs.size() && page < tabs.get(tab).recipes.size() && tabs.get(tab).recipes.get(page).size() > 0) {
				current = tabs.get(tab).recipes.get(page).get(0);
			}
			tabs.clear();
			if (!recipes.isEmpty()) {
				for (Map.Entry<EmiRecipeCategory, List<EmiRecipe>> entry : recipes.entrySet().stream()
						.sorted((a, b) -> {
							int ai = EmiRecipes.categories.indexOf(a.getKey());
							int bi = EmiRecipes.categories.indexOf(b.getKey());
							if (ai < 0) {
								ai = Integer.MAX_VALUE;
							}
							if (bi < 0) {
								bi = Integer.MAX_VALUE;
							}
							return ai - bi;
						}).toList()) {
					List<EmiRecipe> set = entry.getValue();
					if (!set.isEmpty()) {
						tabs.add(new RecipeTab(entry.getKey(), set));
					}
				}
				
				tab = -1;
				setPage(tabPage, 0, 0);
			}
			//setPages(recipes);
			if (current != null) {
				focusRecipe(current);
			}
		}
		setRecipePageWidth(backgroundWidth);
	}

	private void setRecipePageWidth(int width) {
		this.backgroundWidth = width;
		this.x = (this.width - backgroundWidth) / 2;
		this.tabOff = (backgroundWidth - 176) / 2;
		this.arrows.get(0).x = this.x + 2;
		this.arrows.get(1).x = this.x + this.backgroundWidth - 14;
		this.arrows.get(2).x = this.x + 5;
		this.arrows.get(3).x = this.x + this.backgroundWidth - 17;
		this.arrows.get(4).x = this.x + 5;
		this.arrows.get(5).x = this.x + this.backgroundWidth - 17;

		this.arrows.get(0).y = this.y - 18;
		this.arrows.get(1).y = this.y - 18;
		this.arrows.get(2).y = this.y + 5;
		this.arrows.get(3).y = this.y + 5;
		this.arrows.get(4).y = this.y + 18;
		this.arrows.get(5).y = this.y + 18;
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		RenderSystem.setShaderTexture(0, TEXTURE);
		EmiRenderHelper.drawNinePatch(matrices, x, y, backgroundWidth, backgroundHeight, 0, 0, 4, 1);

		int tp = tabPage * tabPageSize;
		int off = 0;
		for (int i = tp; i < tabs.size() && i < tp + tabPageSize; i++) {
			RecipeTab tab = tabs.get(i);
			RenderSystem.setShaderTexture(0, TEXTURE);
			int sOff = (i == this.tab ? 2 : 0);
			EmiRenderHelper.drawNinePatch(matrices, x + tabOff + off * 24 + 16, y - 24 - sOff, 24, 27 + sOff,
				i == this.tab ? 9 : 18, 0, 4, 1);
			tab.category.render(matrices, x + tabOff + off++ * 24 + 20, y - 20 - (i == this.tab ? 2 : 0), delta);
		}
		fillGradient(matrices, x + 19, y + 5, x + backgroundWidth - 19, y + 5 + 12, 0xff999999, 0xff999999);
		fillGradient(matrices, x + 19, y + 18, x + backgroundWidth - 19, y + 18 + 12, 0xff999999, 0xff999999);
		
		boolean categoryHovered = mouseX >= x + 19 && mouseY >= y + 5 && mouseX < x + backgroundWidth - 19 && mouseY <= y + 5 + 12;
		int categoryNameColor = categoryHovered ? 0x22ffff : 0xffffff;

		RecipeTab tab = tabs.get(this.tab);
		drawCenteredText(matrices, textRenderer, tab.category.getName(),
			x + backgroundWidth / 2, y + 7, categoryNameColor);
		drawCenteredText(matrices, textRenderer, EmiPort.translatable("emi.page", this.page + 1, tab.recipes.size()),
			x + backgroundWidth / 2, y + 20, 0xffffff);

		List<EmiIngredient> workstations = EmiRecipes.workstations.getOrDefault(tab.category, List.of());
		if (!workstations.isEmpty()) {
			int size = Math.min(workstations.size(), (backgroundHeight - 30) / 18 - 1);
			RenderSystem.setShaderTexture(0, TEXTURE);
			EmiRenderHelper.drawNinePatch(matrices, x - 21, y + getWorkstationsY() - 3, 24, 6 + 18 * size, 27, 0, 3, 1);
		}
		if (resolve != null) {
			RenderSystem.setShaderTexture(0, TEXTURE);
			EmiRenderHelper.drawNinePatch(matrices, x - 21, y + 7, 24, 24, 27, 0, 3, 1);
		}
		for (WidgetGroup group : currentPage) {
			int mx = mouseX - group.x();
			int my = mouseY - group.y();
			for (Widget widget : group.widgets) {
				if (widget instanceof RecipeFillButtonWidget) {
					if (widget.getBounds().contains(mx, my)) {
						EmiClient.getAvailable(group.recipe());
						break;
					}
				}
			}
			MatrixStack view = RenderSystem.getModelViewStack();
			view.push();
			view.translate(group.x(), group.y(), 0);
			RenderSystem.applyModelViewMatrix();
			for (Widget widget : group.widgets) {
				widget.render(matrices, mx, my, delta);
			}
			view.pop();
			RenderSystem.applyModelViewMatrix();
			EmiClient.availableForCrafting.clear();
		}
		EmiScreenManager.render(matrices, mouseX, mouseY, delta);
		super.render(matrices, mouseX, mouseY, delta);
		if (categoryHovered) {
			this.renderTooltip(matrices, EmiPort.translatable("emi.view_all_recipes"), mouseX, mouseY);
		}
		hoveredWidget = null;
		outer:
		for (WidgetGroup group : currentPage) {
			int mx = mouseX - group.x();
			int my = mouseY - group.y();
			for (Widget widget : group.widgets) {
				if (widget.getBounds().contains(mx, my)) {
					List<TooltipComponent> tooltip = widget.getTooltip(mx, my);
					if (!tooltip.isEmpty()) {
						EmiRenderHelper.drawTooltip(this, matrices, tooltip, mouseX, mouseY);
						hoveredWidget = widget;
						break outer;
					}
				}
			}
		}

		if (mouseX >= x + 16 + tabOff && mouseX < x + backgroundWidth && mouseY >= y - 24 && mouseY < y) {
			int n = (mouseX - x - 16 - tabOff) / 24 + tabPage * tabPageSize;
			if (n < tabs.size() && n >= tabPage * tabPageSize && n < (tabPage + 1) * tabPageSize) {
				EmiRenderHelper.drawTooltip(this, matrices, tabs.get(n).category.getTooltip(), mouseX, mouseY);
			}
		}
	}

	public int getWorkstationsY() {
		return resolve == null ? 10 : 36;
	}

	public EmiRecipeCategory getFocusedCategory() {
		return tabs.get(tab).category;
	}

	public void focusCategory(EmiRecipeCategory category) {
		for (int i = 0; i < tabs.size(); i++) {
			if (tabs.get(i).category == category) {
				setPage(tabPage, i, 0);
				return;
			}
		}
	}

	public void focusRecipe(EmiRecipe recipe) {
		for (int i = 0; i < tabs.size(); i++) {
			RecipeTab tab = tabs.get(i);
			for (int j = 0; j < tab.recipes.size(); j++) {
				for (EmiRecipe r : tab.recipes.get(j)) {
					if (r == recipe) {
						setPage(tabPage, i, j);
						return;
					}
				}
			}
		}
	}

	public void setPage(int tp, int t, int p) {
		currentPage.clear();
		if (tabs.isEmpty()) {
			return;
		}
		boolean snapTabPage = tp == tabPage && t != tab;
		tab = t;
		if (tab >= tabs.size()) {
			tab = 0;
		} else if (tab < 0) {
			tab = tabs.size() - 1;
		}
		if (snapTabPage) {
			tp = (tab) / tabPageSize;
		}
		tabPage = tp;
		if (tabPage >= (tabs.size() - 1) / tabPageSize + 1) {
			tabPage = 0;
		} else if (tabPage < 0) {
			tabPage = (tabs.size() - 1) / tabPageSize;
		}
		List<List<EmiRecipe>> recipes = tabs.get(tab).recipes;
		page = p;
		if (page >= recipes.size()) {
			page = 0;
		} else if (page < 0) {
			page = recipes.size() - 1;
		}
		if (page < recipes.size()) {
			int width = 160;
			for (List<EmiRecipe> list : recipes) {
				for (EmiRecipe r : list) {
					int w = r.getDisplayWidth();
					if (r.supportsRecipeTree() || EmiRecipeFiller.isSupported(r)) {
						w += 26;
					}
					width = Math.max(width, w);
				}
			}
			setRecipePageWidth(width + 16);
			int off = 0;
			for (EmiRecipe r : recipes.get(page)) {
				List<Widget> widgets = Lists.newArrayList();
				int xOff = (backgroundWidth - r.getDisplayWidth()) / 2;
				int wx = x + xOff;
				int wy = y + 36 + off;
				final int recipeHeight = Math.min(backgroundHeight - 52, r.getDisplayHeight());
				widgets.add(new RecipeBackground(-4, -4, r.getDisplayWidth() + 8, recipeHeight + 8));
				WidgetHolder holder = new WidgetHolder() {
		
					public int getWidth() {
						return r.getDisplayWidth();
					}
		
					public int getHeight() {
						return recipeHeight;
					}
		
					public <T extends Widget> T add(T widget) {
						widgets.add(widget);
						return widget;
					}
				};
				r.addWidgets(holder);
				int by = recipeHeight - 12;
				if (recipeHeight <= 18) {
					by += 4;
				}
				int button = 0;
				if (EmiRecipeFiller.isSupported(r)) {
					if (EmiConfig.recipeFillButton) {
						widgets.add(new RecipeFillButtonWidget(r.getDisplayWidth() + 5, by + 14 * button++, r));
					}
				}
				if (r.supportsRecipeTree()) {
					if (EmiConfig.recipeTreeButton) {
						widgets.add(new RecipeTreeButtonWidget(r.getDisplayWidth() + 5, by - 14 * button++, r));
					}
					if (EmiConfig.recipeDefaultButton) {
						widgets.add(new RecipeDefaultButtonWidget(r.getDisplayWidth() + 5, by - 14 * button++, r));
					}
				}
				if (EmiConfig.recipeScreenshotButton) {
					widgets.add(new RecipeScreenshotButtonWidget(-5 - 12, 0, r));
				}
				off += recipeHeight + RECIPE_PADDING;
				currentPage.add(new WidgetGroup(r, widgets, wx, wy));
			}
			List<EmiIngredient> workstations = EmiRecipes.workstations.getOrDefault(tabs.get(tab).category, List.of());
			if (!workstations.isEmpty()) {
				List<Widget> widgets = Lists.newArrayList();
				for (int i = 0; i < workstations.size(); i++) {
					int y = getWorkstationsY() + i * 18;
					if (y + 30 > backgroundHeight) {
						break;
					}
					widgets.add(new SlotWidget(workstations.get(i), x - 18, this.y + y));
				}
				currentPage.add(new WidgetGroup(null, widgets, 0, 0));
			}
		}
		if (resolve != null && resolutionButton != null) {
			resolutionButton.x = this.x - 18;
			resolutionButton.y = this.y + 10;
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		int mx = (int) mouseX;
		int my = (int) mouseY;
		if (mouseX >= x + 19 && mouseY >= y + 5 && mouseX < x + backgroundWidth - 19 && mouseY <= y + 5 + 12) {
			EmiApi.displayAllRecipes();
			MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
			return true;
		}
		for (WidgetGroup group : currentPage) {
			int ox = mx - group.x();
			int oy = my - group.y();
			for (Widget widget : group.widgets) {
				if (widget.getBounds().contains(ox, oy)) {
					if (widget.mouseClicked(ox, oy, button)) {
						return true;
					}
				}
			}
		}
		if (EmiScreenManager.mouseClicked(mouseX, mouseY, button)) {
			return true;
		} else if (mx >= x + 16 + tabOff && mx < x + backgroundWidth && my >= y - 24 && my < y) {
			int n = (mx - x - 16 - tabOff) / 24 + tabPage * tabPageSize;
			if (n < tabs.size() && n >= tabPage * tabPageSize && n < (tabPage + 1) * tabPageSize) {
				setPage(tabPage, n, 0);
				return true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (EmiScreenManager.mouseReleased(mouseX, mouseY, button)) {
			return true;
		}
		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (EmiScreenManager.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		if (EmiScreenManager.mouseScrolled(mouseX, mouseY, amount)) {
			return true;
		} else if (mouseX > x && mouseX < x + backgroundWidth && mouseY < x + backgroundHeight) {
			scrollAcc += amount;
			int sa = (int) scrollAcc;
			scrollAcc %= 1;
			if (EmiUtil.isShiftDown()) {
				setPage(tabPage, tab - sa, 0);
			} else {
				setPage(tabPage, tab, page - sa);
			}
		}
		return super.mouseScrolled(mouseX, mouseY, amount);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			this.close();
			return true;
		} else if (EmiScreenManager.keyPressed(keyCode, scanCode, modifiers)) {
			return true;
		} else if (this.client.options.inventoryKey.matchesKey(keyCode, scanCode)) {
			this.close();
			return true;
		}

		for (WidgetGroup group : currentPage) {
			int mx = EmiScreenManager.lastMouseX - group.x();
			int my = EmiScreenManager.lastMouseY - group.y();
			for (Widget widget : group.widgets) {
				if (widget.getBounds().contains(mx, my)) {
					if (widget.keyPressed(keyCode, scanCode, modifiers)) {
						return true;
					}
				}
			}
		}
		if (keyCode == GLFW.GLFW_KEY_LEFT) {
			setPage(tabPage, tab - 1, 0);
		} else if (keyCode == GLFW.GLFW_KEY_RIGHT) {
			setPage(tabPage, tab + 1, 0);
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void close() {
		this.client.setScreen(old);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	public void getExclusion(Consumer<Bounds> consumer) {
		if (this.tab < tabs.size()) {
			RecipeTab tab = tabs.get(this.tab);
			List<EmiIngredient> workstations = EmiRecipes.workstations.getOrDefault(tab.category, List.of());
			if (!workstations.isEmpty()) {
				int size = Math.min(workstations.size(), (backgroundHeight - 30) / 18);
				RenderSystem.setShaderTexture(0, TEXTURE);
				consumer.accept(new Bounds(x - 21, y, 24, 6 + 18 * size + getWorkstationsY()));
			}
		}
	}

	@Override
	public int emi$getLeft() {
		return x;
	}

	@Override
	public int emi$getRight() {
		return x + backgroundWidth;
	}

	private class RecipeTab {
		private final EmiRecipeCategory category;
		private final List<List<EmiRecipe>> recipes;

		public RecipeTab(EmiRecipeCategory category, List<EmiRecipe> recipes) {
			this.category = category;
			this.recipes = getPages(recipes, backgroundHeight - 44);
		}

		private List<List<EmiRecipe>> getPages(List<EmiRecipe> recipes, int height) {
			List<List<EmiRecipe>> list = Lists.newArrayList();
			List<EmiRecipe> current = Lists.newArrayList();
			int h = 0;
			for (EmiRecipe recipe : recipes) {
				int rh = recipe.getDisplayHeight();
				if (!current.isEmpty() && h + rh > height) {
					list.add(current);
					current = Lists.newArrayList();
					h = 0;
				}
				h += rh + RECIPE_PADDING;
				current.add(recipe);
			}
			if (!current.isEmpty()) {
				list.add(current);
			}
			return list;
		}
	}

	private static record WidgetGroup(EmiRecipe recipe, List<Widget> widgets, int x, int y) {
	}
}
