package dev.emi.emi.screen;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.RecipeFillButtonWidget;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.config.SidebarSide;
import dev.emi.emi.input.EmiInput;
import dev.emi.emi.registry.EmiRecipeFiller;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.runtime.EmiFavorite;
import dev.emi.emi.runtime.EmiHistory;
import dev.emi.emi.screen.widget.ResolutionButtonWidget;
import dev.emi.emi.screen.widget.SizedButtonWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class RecipeScreen extends Screen {
	private static final Identifier TEXTURE = EmiPort.id("emi", "textures/gui/background.png");
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
			new SizedButtonWidget(x + 2, y - 18, 12, 12, 0, 0,
				() -> tabs.size() > tabPageSize, w -> setPage(tabPage - 1, tab, page)),
			new SizedButtonWidget(x + backgroundWidth - 14, y - 18, 12, 12, 12, 0,
				() -> tabs.size() > tabPageSize, w -> setPage(tabPage + 1, tab, page)),
			new SizedButtonWidget(x + 5, y + 5, 12, 12, 0, 0,
				() -> tabs.size() > 1, w -> setPage(tabPage, tab - 1, 0)),
			new SizedButtonWidget(x + backgroundWidth - 17, y + 5, 12, 12, 12, 0,
				() -> tabs.size() > 1, w -> setPage(tabPage, tab + 1, 0)),
			new SizedButtonWidget(x + 5, y + 18, 12, 12, 0, 0,
				() -> tabs.get(tab).getPageCount() > 1, w -> setPage(tabPage, tab, page - 1)),
			new SizedButtonWidget(x + backgroundWidth - 17, y + 18, 12, 12, 12, 0,
				() -> tabs.get(tab).getPageCount() > 1, w -> setPage(tabPage, tab, page + 1))
		);
		resolve = null;
		this.recipes = recipes;
	}

	@Override
	protected void init() {
		super.init();
		minimumWidth = Math.max(EmiConfig.minimumRecipeScreenWidth, 56);
		backgroundWidth = minimumWidth;
		backgroundHeight = Math.min(EmiConfig.maximumRecipeScreenHeight, height - 52 - EmiConfig.verticalMargin);
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
			if (tab < tabs.size() && page < tabs.get(tab).getPageCount() && tabs.get(tab).getPage(page).size() > 0) {
				current = tabs.get(tab).getPage(page).get(0).recipe;
			}
			tabs.clear();
			if (!recipes.isEmpty()) {
				for (Map.Entry<EmiRecipeCategory, List<EmiRecipe>> entry : recipes.entrySet().stream()
						.sorted((a, b) -> {
							int ai = EmiApi.getRecipeManager().getCategories().indexOf(a.getKey());
							int bi = EmiApi.getRecipeManager().getCategories().indexOf(b.getKey());
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
						RecipeTab tab = new RecipeTab(entry.getKey(), set);
						tab.bakePages(backgroundHeight);
						tabs.add(tab);
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
	public void render(MatrixStack raw, int mouseX, int mouseY, float delta) {
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		this.renderBackground(context.raw());
		context.resetColor();
		EmiRenderHelper.drawNinePatch(context, TEXTURE, x, y, backgroundWidth, backgroundHeight, 0, 0, 4, 1);

		int tp = tabPage * tabPageSize;
		int off = 0;
		for (int i = tp; i < tabs.size() && i < tp + tabPageSize; i++) {
			RecipeTab tab = tabs.get(i);
			int sOff = (i == this.tab ? 2 : 0);
			EmiRenderHelper.drawNinePatch(context, TEXTURE, x + tabOff + off * 24 + 16, y - 24 - sOff, 24, 27 + sOff,
				i == this.tab ? 9 : 18, 0, 4, 1);
			tab.category.render(context.raw(), x + tabOff + off++ * 24 + 20, y - 20 - (i == this.tab ? 2 : 0), delta);
		}

		EmiRenderHelper.drawNinePatch(context, TEXTURE, x + 19 + buttonOff, y + 5, minimumWidth - 38, 12, 0, 16, 3, 6);
		//EmiRenderHelper.drawScroll(context, x + 19 + buttonOff, y + 5 + 10, minimumWidth - 38, 2, tab, tabs.size(), -1);
		EmiRenderHelper.drawNinePatch(context, TEXTURE, x + 19 + buttonOff, y + 19, minimumWidth - 38, 12, 0, 16, 3, 6);
		//EmiRenderHelper.drawScroll(context, x + 19 + buttonOff, y + 19 + 10, minimumWidth - 38, 2, page, tabs.get(tab).getPageCount(), -1);
		
		boolean categoryHovered = mouseX >= x + 19 + buttonOff && mouseY >= y + 5 && mouseX < x + minimumWidth + buttonOff - 19 && mouseY < y + 5 + 12;
		int categoryNameColor = categoryHovered ? 0x22ffff : 0xffffff;

		RecipeTab tab = tabs.get(this.tab);
		Text text = tab.category.getName();
		if (client.textRenderer.getWidth(text) > minimumWidth - 40) {
			int extraWidth = client.textRenderer.getWidth("...");
			text = EmiPort.literal(client.textRenderer.trimToWidth(text, (minimumWidth - 40) - extraWidth).getString() + "...");
		}
		context.drawCenteredTextWithShadow(text, x + backgroundWidth / 2, y + 7, categoryNameColor);
		context.drawCenteredTextWithShadow(EmiRenderHelper.getPageText(this.page + 1, tab.getPageCount(), minimumWidth - 40),
			x + backgroundWidth / 2, y + 21, 0xffffff);

		List<EmiIngredient> workstations = EmiApi.getRecipeManager().getWorkstations(tab.category);
		int workstationAmount = Math.min(workstations.size(), getMaxWorkstations());
		if (workstationAmount > 0 || resolve != null) {
			Bounds bounds = getWorkstationBounds(-1);
			int offset = getResolveOffset();
			if (workstationAmount <= 0) {
				offset = 18;
			}
			if (EmiConfig.workstationLocation == SidebarSide.LEFT) {
				EmiRenderHelper.drawNinePatch(context, TEXTURE, bounds.x() - 5, bounds.y() - 5, 28, 10 + 18 * workstationAmount + offset, 36, 0, 5, 1);
			} else if (EmiConfig.workstationLocation == SidebarSide.RIGHT) {
				EmiRenderHelper.drawNinePatch(context, TEXTURE, bounds.x() - 5, bounds.y() - 5, 28, 10 + 18 * workstationAmount + offset, 47, 0, 5, 1);
			} else if (EmiConfig.workstationLocation == SidebarSide.BOTTOM) {
				EmiRenderHelper.drawNinePatch(context, TEXTURE, bounds.x() - 5, bounds.y() - 5, 10 + 18 * workstationAmount + offset, 28, 58, 0, 5, 1);
			}
		}
		for (WidgetGroup group : currentPage) {
			int mx = mouseX - group.x();
			int my = mouseY - group.y();
			context.push();
			context.matrices().translate(group.x(), group.y(), 0);
			EmiPort.applyModelViewMatrix();
			try {
				for (Widget widget : group.widgets) {
					widget.render(context.raw(), mx, my, delta);
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
							handler.render(group.recipe, new EmiCraftContext(hs, handler.getInventory(hs), EmiCraftContext.Type.FILL_BUTTON), group.widgets, context.raw());
						} else if (EmiScreenManager.lastPlayerInventory != null) {
							StandardRecipeHandler.renderMissing(group.recipe, EmiScreenManager.lastPlayerInventory, group.widgets, context.raw());
						}
						break;
					}
				}
			}
			context.pop();
			EmiPort.applyModelViewMatrix();
		}
		EmiScreenManager.drawBackground(context, mouseX, mouseY, delta);
		EmiScreenManager.render(context, mouseX, mouseY, delta);
		EmiScreenManager.drawForeground(context, mouseX, mouseY, delta);
		super.render(context.raw(), mouseX, mouseY, delta);
		if (categoryHovered) {
			this.renderTooltip(context.raw(), List.of(
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
							EmiRenderHelper.drawTooltip(this, context, tooltip, mouseX, mouseY);
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
			EmiRenderHelper.drawTooltip(this, context, rTab.category.getTooltip(), mouseX, mouseY);
		}
	}

	public EmiIngredient getHoveredStack() {
		if (hoveredWidget instanceof SlotWidget slot) {
			return slot.getStack();
		}
		return EmiStack.EMPTY;
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
			for (int j = 0; j < tab.getPageCount(); j++) {
				for (RecipeDisplay d : tab.getPage(j)) {
					if (d.recipe == recipe) {
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
		tab = wrap(t, tabs.size());
		if (snapTabPage) {
			tp = (tab) / tabPageSize;
		}
		tabPage = wrap(tp, (tabs.size() - 1) / tabPageSize + 1);
		RecipeTab tab = tabs.get(this.tab);
		page = wrap(p, tab.getPageCount());
		if (page < tab.getPageCount()) {
			int width = Math.max(minimumWidth - 16, tab.getWidth());
			setRecipePageWidth(width + 16);
			currentPage = Lists.newArrayList();
			currentPage.addAll(tab.constructWidgets(page, x, y, backgroundWidth, backgroundHeight));
			List<EmiIngredient> workstations = EmiApi.getRecipeManager().getWorkstations(tab.category);
			if (!workstations.isEmpty()) {
				WidgetGroup widgets = new WidgetGroup(null, 0, 0, 0, 0);
				for (int i = 0; i < workstations.size() && i < getMaxWorkstations(); i++) {
					Bounds bounds = getWorkstationBounds(i);
					widgets.add(new SlotWidget(workstations.get(i), bounds.x(), bounds.y()));
				}
				currentPage.add(widgets);
			}
		}
		if (resolve != null && resolutionButton != null) {
			Bounds bounds = getWorkstationBounds(-1);
			resolutionButton.x = bounds.x();
			resolutionButton.y = bounds.y();
		}
	}

	public int wrap(int value, int size) {
		if (value >= size) {
			return 0;
		} else if (value < 0) {
			return size - 1;
		}
		return value;
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
				boolean groupHovered = new Bounds(group.x(), group.y(), group.getWidth(), group.getHeight()).contains(mx, my);
				for (Widget widget : group.widgets) {
					if (widget.getBounds().contains(ox, oy)) {
						if (widget instanceof SlotWidget slot) {
							if (pressedSlot == null) {
								pressedSlot = widget;
							}
						} else {
							if (widget.mouseClicked(ox, oy, button)) {
								return true;
							}
						}
						groupHovered = true;
					}
				}
				if (groupHovered && EmiScreenManager.recipeInteraction(group.recipe, bind -> bind.matchesMouse(button))) {
					return true;
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
						stack = new EmiFavorite(stack, slot.getRecipe());
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
			if (EmiInput.isShiftDown() || mouseY < this.y) {
				setPage(tabPage, tab - sa, 0);
			} else {
				setPage(tabPage, tab, page - sa);
			}
		}
		return super.mouseScrolled(mouseX, mouseY, amount);
	}

	@Override
	public boolean charTyped(char chr, int modifiers) {
		if (EmiScreenManager.search.charTyped(chr, modifiers)) {
			return true;
		}
		return super.charTyped(chr, modifiers);
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
				boolean groupHovered = new Bounds(group.x(), group.y(), group.getWidth(), group.getHeight()).contains(EmiScreenManager.lastMouseX, EmiScreenManager.lastMouseY);
				for (Widget widget : group.widgets) {
					if (widget.getBounds().contains(mx, my)) {
						if (widget.keyPressed(keyCode, scanCode, modifiers)) {
							return true;
						}
						groupHovered = true;
					}
				}
				if (groupHovered && EmiScreenManager.recipeInteraction(group.recipe, bind -> bind.matchesKey(keyCode, scanCode))) {
					return true;
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
		EmiHistory.popUntil(s -> !(s instanceof RecipeScreen), old);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	public Bounds getBounds() {
		int top = y - 26;
		int bottom = y + backgroundHeight;
		int left = x;
		int right = x + backgroundWidth;
		if (EmiConfig.workstationLocation == SidebarSide.LEFT) {
			left -= 22;
		} else if (EmiConfig.workstationLocation == SidebarSide.RIGHT) {
			right += 22;
		}
		return new Bounds(left, top, right - left, bottom - top);
	}
}
