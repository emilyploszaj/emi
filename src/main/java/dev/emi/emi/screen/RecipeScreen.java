package dev.emi.emi.screen;

import java.util.List;
import java.util.Map;

import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.EmiFavorite;
import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRecipeFiller;
import dev.emi.emi.EmiRecipes;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.DrawableWidget;
import dev.emi.emi.api.widget.RecipeFillButtonWidget;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.TextWidget.Alignment;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.config.SidebarSide;
import dev.emi.emi.screen.widget.ResolutionButtonWidget;
import dev.emi.emi.screen.widget.SizedButtonWidget;
import dev.emi.emi.widget.RecipeBackground;
import dev.emi.emi.widget.RecipeDefaultButtonWidget;
import dev.emi.emi.widget.RecipeScreenshotButtonWidget;
import dev.emi.emi.widget.RecipeTreeButtonWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class RecipeScreen extends Screen implements EmiScreen {
	private static final Identifier TEXTURE = new Identifier("emi", "textures/gui/background.png");
	private static final int RECIPE_PADDING = 10;
	public static @Nullable EmiIngredient resolve = null;
	private Map<EmiRecipeCategory, List<EmiRecipe>> recipes;
	public HandledScreen<?> old;
	private List<RecipeTab> tabs = Lists.newArrayList();
	private int tabPageSize = 6;
	private int tabPage = 0, tab = 0, page = 0;
	private List<SizedButtonWidget> arrows;
	private List<WidgetGroup> currentPage = Lists.newArrayList();
	private int buttonOff = 0, tabOff = 0;
	private Widget hoveredWidget = null, pressedSlot = null;
	private ResolutionButtonWidget resolutionButton;
	private double scrollAcc = 0;
	private int minimumWidth = 176;
	int backgroundWidth = minimumWidth;
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
		minimumWidth = Math.max(EmiConfig.minimumRecipeScreenWidth, 56);
		backgroundWidth = minimumWidth;
		backgroundHeight = height - 52 - EmiConfig.verticalMargin;
		x = (this.width - backgroundWidth) / 2;
		y = (this.height - backgroundHeight) / 2 + 1;
		this.tabPageSize = (minimumWidth - 32) / 24;
		
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
		if ((width & 1) == 1) {
			width++;
		}
		this.backgroundWidth = width;
		this.x = (this.width - backgroundWidth) / 2;
		this.buttonOff = (backgroundWidth - minimumWidth) / 2;
		int tabExtra = (minimumWidth - 32) % 24 / 2;
		this.tabOff = buttonOff + tabExtra;
		this.arrows.get(0).x = this.x + 2 + buttonOff + tabExtra;
		this.arrows.get(1).x = this.x + minimumWidth - 14 + buttonOff - tabExtra;
		this.arrows.get(2).x = this.x + 5 + buttonOff;
		this.arrows.get(3).x = this.x + minimumWidth - 17 + buttonOff;
		this.arrows.get(4).x = this.x + 5 + buttonOff;
		this.arrows.get(5).x = this.x + minimumWidth - 17 + buttonOff;

		this.arrows.get(0).y = this.y - 18;
		this.arrows.get(1).y = this.y - 18;
		this.arrows.get(2).y = this.y + 5;
		this.arrows.get(3).y = this.y + 5;
		this.arrows.get(4).y = this.y + 19;
		this.arrows.get(5).y = this.y + 19;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		EmiPort.setPositionTexShader();
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

		RenderSystem.setShaderTexture(0, TEXTURE);
		EmiRenderHelper.drawNinePatch(matrices, x + 19 + buttonOff, y + 5, minimumWidth - 38, 12, 0, 16, 3, 6);
		EmiRenderHelper.drawNinePatch(matrices, x + 19 + buttonOff, y + 19, minimumWidth - 38, 12, 0, 16, 3, 6);
		
		boolean categoryHovered = mouseX >= x + 19 + buttonOff && mouseY >= y + 5 && mouseX < x + minimumWidth + buttonOff - 19 && mouseY <= y + 5 + 12;
		int categoryNameColor = categoryHovered ? 0x22ffff : 0xffffff;

		RecipeTab tab = tabs.get(this.tab);
		Text text = tab.category.getName();
		if (client.textRenderer.getWidth(text) > minimumWidth - 40) {
			int extraWidth = client.textRenderer.getWidth("...");
			text = EmiPort.literal(client.textRenderer.trimToWidth(text, (minimumWidth - 40) - extraWidth).getString() + "...");
		}
		EmiPort.drawCenteredText(matrices, textRenderer, text, x + backgroundWidth / 2, y + 7, categoryNameColor);
		EmiPort.drawCenteredText(matrices, textRenderer,EmiRenderHelper.getPageText(this.page + 1, tab.recipes.size(), minimumWidth - 40),
			x + backgroundWidth / 2, y + 21, 0xffffff);

		List<EmiIngredient> workstations = EmiRecipes.workstations.getOrDefault(tab.category, List.of());
		int workstationAmount = Math.min(workstations.size(), getMaxWorkstations());
		if (workstationAmount > 0 || resolve != null) {
			RenderSystem.setShaderTexture(0, TEXTURE);
			Bounds bounds = getWorkstationBounds(-1);
			int offset = getResolveOffset();
			if (workstationAmount <= 0) {
				offset = 18;
			}
			if (EmiConfig.workstationLocation == SidebarSide.LEFT) {
				EmiRenderHelper.drawNinePatch(matrices, bounds.x() - 5, bounds.y() - 5, 28, 10 + 18 * workstationAmount + offset, 36, 0, 5, 1);
			} else if (EmiConfig.workstationLocation == SidebarSide.RIGHT) {
				EmiRenderHelper.drawNinePatch(matrices, bounds.x() - 5, bounds.y() - 5, 28, 10 + 18 * workstationAmount + offset, 47, 0, 5, 1);
			} else if (EmiConfig.workstationLocation == SidebarSide.BOTTOM) {
				EmiRenderHelper.drawNinePatch(matrices, bounds.x() - 5, bounds.y() - 5, 10 + 18 * workstationAmount + offset, 28, 58, 0, 5, 1);
			}
		}
		for (WidgetGroup group : currentPage) {
			int mx = mouseX - group.x();
			int my = mouseY - group.y();
			MatrixStack view = RenderSystem.getModelViewStack();
			view.push();
			view.translate(group.x(), group.y(), 0);
			RenderSystem.applyModelViewMatrix();
			try {
				for (Widget widget : group.widgets) {
					widget.render(matrices, mx, my, delta);
				}
			} catch (Throwable e) {
				e.printStackTrace();
				group.error(e);
			}
			for (Widget widget : group.widgets) {
				if (widget instanceof RecipeFillButtonWidget) {
					if (widget.getBounds().contains(mx, my)) {
						HandledScreen hs = EmiApi.getHandledScreen();
						EmiRecipeHandler handler = EmiRecipeFiller.getFirstValidHandler(group.recipe, hs);
						if (handler != null) {
							handler.render(group.recipe, new EmiCraftContext(hs, handler.getInventory(hs), EmiCraftContext.Type.FILL_BUTTON), group.widgets, matrices);
						} else if (EmiScreenManager.lastPlayerInventory != null) {
							StandardRecipeHandler.renderMissing(group.recipe, EmiScreenManager.lastPlayerInventory, group.widgets, matrices);
						}
						break;
					}
				}
			}
			view.pop();
			RenderSystem.applyModelViewMatrix();
		}
		EmiScreenManager.drawBackground(matrices, mouseX, mouseY, delta);
		EmiScreenManager.render(matrices, mouseX, mouseY, delta);
		super.render(matrices, mouseX, mouseY, delta);
		if (categoryHovered) {
			this.renderTooltip(matrices, List.of(
				tab.category.getName(),
				EmiPort.translatable("emi.view_all_recipes")
			), mouseX, mouseY);
		}
		hoveredWidget = null;
		outer:
		for (WidgetGroup group : currentPage) {
			try {
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
			} catch (Throwable e) {
				e.printStackTrace();
				group.error(e);
			}
		}

		RecipeTab rTab = getTabAt(mouseX, mouseY);
		if (rTab != null) {
			EmiRenderHelper.drawTooltip(this, matrices, rTab.category.getTooltip(), mouseX, mouseY);
		}
	}

	public RecipeTab getTabAt(int mx, int my) {
		if (mx >= x + 16 + tabOff && mx < x + backgroundWidth && my >= y - 24 && my < y) {
			int n = (mx - x - 16 - tabOff) / 24 + tabPage * tabPageSize;
			if (n < tabs.size() && n >= tabPage * tabPageSize && n < (tabPage + 1) * tabPageSize) {
				return tabs.get(n);
			}
		}
		return null;
	}

	public int getMaxWorkstations() {
		return switch (EmiConfig.workstationLocation) {
			case LEFT, RIGHT -> (this.backgroundHeight - getResolveOffset() - 18) / 18; 
			case BOTTOM -> (this.backgroundWidth - getResolveOffset() - 18) / 18;
			default -> 0;
		};
	}

	public int getVerticalRecipeSpace(EmiRecipeCategory category) {
		int height = backgroundHeight - 46;
		if (EmiConfig.workstationLocation == SidebarSide.BOTTOM) {
			if (!EmiRecipes.workstations.getOrDefault(category, List.of()).isEmpty()) {
				height -= 23;
			}
		}
		return height;
	}

	public int getResolveOffset() {
		return resolve == null ? 0 : 23;
	}

	public Bounds getWorkstationBounds(int i) {
		int offset = 0;
		if (i == -1) {
			i = 0;
			offset = -getResolveOffset();
		}
		if (EmiConfig.workstationLocation == SidebarSide.LEFT) {
			return new Bounds(this.x - 18, this.y + 9 + getResolveOffset() + i * 18 + offset, 18, 18);
		} else if (EmiConfig.workstationLocation == SidebarSide.RIGHT) {
			return new Bounds(this.x + this.backgroundWidth, this.y + 9 + getResolveOffset() + i * 18 + offset, 18, 18);
		} else if (EmiConfig.workstationLocation == SidebarSide.BOTTOM) {
			return new Bounds(this.x + 5 + getResolveOffset() + i * 18 + offset, this.y + this.backgroundHeight - 23, 18, 18);
		}
		return Bounds.EMPTY;
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
			int width = minimumWidth - 16;
			for (List<EmiRecipe> list : recipes) {
				for (EmiRecipe r : list) {
					try {
						int w = r.getDisplayWidth();
						if (r.supportsRecipeTree() || EmiRecipeFiller.isSupported(r) || EmiConfig.recipeScreenshotButton) {
							w += 26;
						}
						width = Math.max(width, w);
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			}
			setRecipePageWidth(width + 16);
			int off = 0;
			for (EmiRecipe r : recipes.get(page)) {
				List<Widget> widgets = Lists.newArrayList();
				int rWidth = 128;
				int rHeight = 64;
				int wx = x + (backgroundWidth - rWidth) / 2;
				int wy = y + 37 + off;
				try {
					rWidth = r.getDisplayWidth();
					wx = x + (backgroundWidth - rWidth) / 2;
					rHeight = Math.min(getVerticalRecipeSpace(tabs.get(tab).category), r.getDisplayHeight());
					final int recipeWidth = rWidth;
					final int recipeHeight = rHeight;
					widgets.add(new RecipeBackground(-4, -4, rWidth + 8, recipeHeight + 8));
					WidgetHolder holder = new WidgetHolder() {
			
						public int getWidth() {
							return recipeWidth;
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
					int by = rHeight - 12;
					if (rHeight <= 18) {
						by += 4;
					}
					int button = 0;
					int lbutton = 0;
					if (EmiRecipeFiller.isSupported(r)) {
						if (EmiConfig.recipeFillButton) {
							widgets.add(new RecipeFillButtonWidget(rWidth + 5, by + 14 * button++, r));
						}
					}
					if (r.supportsRecipeTree()) {
						if (EmiConfig.recipeTreeButton) {
							widgets.add(new RecipeTreeButtonWidget(rWidth + 5, by - 14 * button++, r));
						}
						if (EmiConfig.recipeDefaultButton) {
							widgets.add(new RecipeDefaultButtonWidget(rWidth + 5, by - 14 * button++, r));
						}
					}
					if (EmiConfig.recipeScreenshotButton) {
						widgets.add(new RecipeScreenshotButtonWidget(-5 - 12, by - 14 * lbutton++, r));
					}
				} catch (Throwable e) {
					widgets.clear();
					widgets.add(new RecipeBackground(-4, -4, rWidth + 8, rHeight + 8));
					widgets.add(new TextWidget(EmiPort.ordered(EmiPort.translatable("emi.error.recipe.initialize")),
						rWidth / 2, rHeight / 2 - 5, Formatting.RED.getColorValue(), true).horizontalAlign(Alignment.CENTER));
					widgets.add(new DrawableWidget(0, 0, rWidth, rHeight, (matrices, mouseX, mouseY, delta) -> {})
						.tooltip((i, j) -> EmiUtil.getStackTrace(e).stream()
							.map(EmiPort::literal).map(EmiPort::ordered).map(TooltipComponent::of).toList()));
				}
				off += rHeight + RECIPE_PADDING;
				currentPage.add(new WidgetGroup(r, widgets, wx, wy, rWidth, rHeight));
			}
			List<EmiIngredient> workstations = EmiRecipes.workstations.getOrDefault(tabs.get(tab).category, List.of());
			if (!workstations.isEmpty()) {
				List<Widget> widgets = Lists.newArrayList();
				for (int i = 0; i < workstations.size() && i < getMaxWorkstations(); i++) {
					Bounds bounds = getWorkstationBounds(i);
					widgets.add(new SlotWidget(workstations.get(i), bounds.x(), bounds.y()));
				}
				currentPage.add(new WidgetGroup(null, widgets, 0, 0, 0, 0));
			}
		}
		if (resolve != null && resolutionButton != null) {
			Bounds bounds = getWorkstationBounds(-1);
			resolutionButton.x = bounds.x();
			resolutionButton.y = bounds.y();
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		int mx = (int) mouseX;
		int my = (int) mouseY;
		pressedSlot = null;
		if (mouseX >= x + 19 + buttonOff && mouseY >= y + 5 && mouseX < x + minimumWidth + buttonOff - 19 && mouseY <= y + 5 + 12) {
			EmiApi.displayAllRecipes();
			MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
			return true;
		}
		for (WidgetGroup group : currentPage) {
			try {
				int ox = mx - group.x();
				int oy = my - group.y();
				for (Widget widget : group.widgets) {
					if (widget.getBounds().contains(ox, oy)) {
						if (widget instanceof SlotWidget slot) {
							pressedSlot = widget;
						} else {
							if (widget.mouseClicked(ox, oy, button)) {
								return true;
							}
						}
					}
				}
			} catch (Throwable e) {
				e.printStackTrace();
				group.error(e);
			}
		}
		if (EmiScreenManager.mouseClicked(mouseX, mouseY, button)) {
			return true;
		}
		RecipeTab rTab = getTabAt(mx, my);
		if (rTab != null) {
			MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
			focusCategory(rTab.category);
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (EmiScreenManager.mouseReleased(mouseX, mouseY, button)) {
			return true;
		}
		if (pressedSlot instanceof SlotWidget slot) {
			WidgetGroup group = getGroup(slot);
			if (group != null) {
				try {
					int ox = ((int) mouseX) - group.x();
					int oy = ((int) mouseY) - group.y();
					if (slot.getBounds().contains(ox, oy)) {
						if (slot.mouseClicked(ox, oy, button)) {
							return true;
						}
					}
				} catch (Throwable e) {
					e.printStackTrace();
					group.error(e);
				}
			}
			pressedSlot = null;
		}
		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (EmiScreenManager.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
			return true;
		}
		if (pressedSlot instanceof SlotWidget slot) {
			WidgetGroup group = getGroup(slot);
			if (group != null) {
				int ox = ((int) mouseX) - group.x();
				int oy = ((int) mouseY) - group.y();
				if (!slot.getBounds().contains(ox, oy) && button == 0) {
					EmiIngredient stack = slot.getStack();
					if (slot.getRecipe() != null) {
						stack = new EmiFavorite.Craftable(slot.getRecipe());
					}
					EmiScreenManager.pressedStack = stack;
					EmiScreenManager.draggedStack = stack;
					pressedSlot = null;
				}
			}
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
			try {
				int mx = EmiScreenManager.lastMouseX - group.x();
				int my = EmiScreenManager.lastMouseY - group.y();
				for (Widget widget : group.widgets) {
					if (widget.getBounds().contains(mx, my)) {
						if (widget.keyPressed(keyCode, scanCode, modifiers)) {
							return true;
						}
					}
				}
			} catch (Throwable e) {
				e.printStackTrace();
				group.error(e);
			}
		}
		if (keyCode == GLFW.GLFW_KEY_LEFT) {
			setPage(tabPage, tab - 1, 0);
		} else if (keyCode == GLFW.GLFW_KEY_RIGHT) {
			setPage(tabPage, tab + 1, 0);
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	public WidgetGroup getGroup(Widget widget) {
		for (WidgetGroup group : currentPage) {
			if (group.widgets.contains(widget)) {
				return group;
			}
		}
		return null;
	}

	@Override
	public void close() {
		this.client.setScreen(old);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public int emi$getLeft() {
		if (EmiConfig.workstationLocation == SidebarSide.LEFT) {
			return x - 22;
		}
		return x;
	}

	@Override
	public int emi$getRight() {
		if (EmiConfig.workstationLocation == SidebarSide.RIGHT) {
			return x + backgroundWidth + 22;
		}
		return x + backgroundWidth;
	}

	@Override
	public int emi$getTop() {
		return y - 26;
	}

	@Override
	public int emi$getBottom() {
		return y + backgroundHeight;
	}

	private class RecipeTab {
		private final EmiRecipeCategory category;
		private final List<List<EmiRecipe>> recipes;

		public RecipeTab(EmiRecipeCategory category, List<EmiRecipe> recipes) {
			this.category = category;
			this.recipes = getPages(recipes, getVerticalRecipeSpace(category));
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

	private static class WidgetGroup {
		public final EmiRecipe recipe;
		public final int x, y, width, height;
		public List<Widget> widgets;

		public WidgetGroup(EmiRecipe recipe, List<Widget> widgets, int x, int y, int width, int height) {
			this.recipe = recipe;
			this.widgets = widgets;
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}

		public void error(Throwable e) {
			List<Widget> widgets = Lists.newArrayList();
			if (!this.widgets.isEmpty()) {
				widgets.add(this.widgets.get(0));
			}
			widgets.add(new TextWidget(EmiPort.ordered(EmiPort.translatable("emi.error.recipe.render")),
				width / 2, height / 2 - 5, Formatting.RED.getColorValue(), true).horizontalAlign(Alignment.CENTER));
			widgets.add(new DrawableWidget(0, 0, width, height, (matrices, mouseX, mouseY, delta) -> {})
				.tooltip((i, j) -> EmiUtil.getStackTrace(e).stream()
					.map(EmiPort::literal).map(EmiPort::ordered).map(TooltipComponent::of).toList()));
			this.widgets = widgets;
		}

		public int x() {
			return x;
		}

		public int y() {
			return y;
		}
	}
}
