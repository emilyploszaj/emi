package dev.emi.emi.api.widget;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiResolutionRecipe;
import dev.emi.emi.api.render.EmiRender;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.config.HelpLevel;
import dev.emi.emi.input.EmiBind;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.runtime.EmiFavorites;
import dev.emi.emi.runtime.EmiHistory;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.screen.RecipeScreen;
import dev.emi.emi.screen.tooltip.EmiTooltip;
import dev.emi.emi.screen.tooltip.RecipeCostTooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class SlotWidget extends Widget {
	protected final EmiIngredient stack;
	protected final int x, y;
	protected Identifier textureId;
	protected int u, v;
	protected int customWidth, customHeight;
	protected boolean drawBack = true, output = false, catalyst = false, custom = false;
	protected List<Supplier<TooltipComponent>> tooltipSuppliers = Lists.newArrayList();
	protected Bounds bounds;
	private EmiRecipe recipe;

	public SlotWidget(EmiIngredient stack, int x, int y) {
		this.stack = stack;
		this.x = x;
		this.y = y;
	}

	public EmiIngredient getStack() {
		return stack;
	}

	/**
	 * @return The recipe associated with a slot.
	 *	Logical output slots will return the recipe they represent.
	 *  Otherwise, the result will be null.
	 */
	public EmiRecipe getRecipe() {
		return recipe;
	}

	/**
	 * Whether to draw the background texture of a slot.
	 */
	public SlotWidget drawBack(boolean drawBack) {
		this.drawBack = drawBack;
		return this;
	}

	/**
	 * Whether to the slot as the large 26x26 or small 18x18 slot.
	 * This is a purely visual change.
	 */
	public SlotWidget large(boolean large) {
		this.output = large;
		return this;
	}

	/**
	 * Whether to draw a catalyst icon on the slot.
	 */
	public SlotWidget catalyst(boolean catalyst) {
		this.catalyst = catalyst;
		return this;
	}

	/**
	 * Provides a function for appending {@link TooltipComponent}s to the slot's tooltip.
	 */
	public SlotWidget appendTooltip(Function<EmiIngredient, TooltipComponent> function) {
		return appendTooltip(() -> function.apply(getStack()));
	}

	/**
	 * Provides a supplier for appending {@link TooltipComponent}s to the slot's tooltip.
	 */
	public SlotWidget appendTooltip(Supplier<TooltipComponent> supplier) {
		tooltipSuppliers.add(supplier);
		return this;
	}

	/**
	 * Provides a shorthand for appending text to the slot's tooltip.
	 */
	public SlotWidget appendTooltip(Text text) {
		tooltipSuppliers.add(() -> TooltipComponent.of(EmiPort.ordered(text)));
		return this;
	}

	/**
	 * Provides EMI context that the slot contains the provided recipe's output.
	 * This is used for resolving recipes in the recipe tree, displaying extra information in tooltips,
	 * adding recipe context to favorites, and more.
	 */
	public SlotWidget recipeContext(EmiRecipe recipe) {
		this.recipe = recipe;
		return this;
	}

	/**
	 * Sets the slot to use a custom texture.
	 * The size of the texture drawn is 18x18, or 26x26 if the slot is large,
	 * which is set by {@link SlotWidget#large()}.
	 * {@link SlotWidget#custom()} is an alternative for custom sizing.
	 */
	public SlotWidget backgroundTexture(Identifier id, int u, int v) {
		this.textureId = id;
		this.u = u;
		this.v = v;
		return this;
	}

	/**
	 * Sets the slot to use a custom texture and custom sizing
	 * @param id The texture identifier to use to draw the background
	 */
	public SlotWidget customBackground(Identifier id, int u, int v, int width, int height) {
		backgroundTexture(id, u, v);
		this.custom = true;
		this.customWidth = width;
		this.customHeight = height;
		return this;
	}

	@Override
	public Bounds getBounds() {
		if (custom) {
			return new Bounds(x, y, customWidth, customHeight);
		} else if (output) {
			return new Bounds(x, y, 26, 26);
		} else {
			return new Bounds(x, y, 18, 18);
		}
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		EmiPort.setPositionTexShader();
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		drawBackground(matrices, mouseX, mouseY, delta);
		drawStack(matrices, mouseX, mouseY, delta);
		RenderSystem.disableDepthTest();
		drawOverlay(matrices, mouseX, mouseY, delta);
	}

	public void drawBackground(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		EmiDrawContext context = EmiDrawContext.wrap(matrices);
		Bounds bounds = getBounds();
		int width = bounds.width();
		int height = bounds.height();
		if (drawBack) {
			if (textureId != null) {
				context.drawTexture(textureId, bounds.x(), bounds.y(), width, height, u, v, width, height, 256, 256);
			} else {
				int v = getStack().getChance() != 1 ? bounds.height() : 0;
				if (output) {
					context.drawTexture(EmiRenderHelper.WIDGETS, bounds.x(), bounds.y(), 26, 26, 18, v, 26, 26, 256, 256);
				} else {
					context.drawTexture(EmiRenderHelper.WIDGETS, bounds.x(), bounds.y(), 18, 18, 0, v, 18, 18, 256, 256);
				}
			}
		}
	}

	public void drawStack(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		Bounds bounds = getBounds();
		int xOff = (bounds.width() - 16) / 2;
		int yOff = (bounds.height() - 16) / 2;
		getStack().render(matrices, bounds.x() + xOff, bounds.y() + yOff, delta);
	}

	public void drawOverlay(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		Bounds bounds = getBounds();
		int width = bounds.width();
		int height = bounds.height();
		int xOff = (width - 16) / 2;
		int yOff = (height - 16) / 2;
		if (catalyst) {
			EmiRender.renderCatalystIcon(getStack(), matrices, x + xOff, y + yOff);
		}

		if (shouldDrawSlotHighlight(mouseX, mouseY)) {
			drawSlotHighlight(matrices, bounds);
		}
	}

	public boolean shouldDrawSlotHighlight(int mouseX, int mouseY) {
		return getBounds().contains(mouseX, mouseY) && EmiConfig.showHoverOverlay;
	}

	public void drawSlotHighlight(MatrixStack matrices, Bounds bounds) {
		EmiRenderHelper.drawSlotHightlight(EmiDrawContext.wrap(matrices), bounds.x() + 1, bounds.y() + 1, bounds.width() - 2, bounds.height() - 2, 200);
	}
	
	@Override
	public List<TooltipComponent> getTooltip(int mouseX, int mouseY) {
		List<TooltipComponent> list = Lists.newArrayList();
		if (getStack().isEmpty()) {
			return list;
		}
		list.addAll(getStack().getTooltip());
		addSlotTooltip(list);
		return list;
	}

	protected void addSlotTooltip(List<TooltipComponent> list) {
		for (Supplier<TooltipComponent> supplier : tooltipSuppliers) {
			list.add(supplier.get());
		}
		if (getStack().getChance() != 1) {
			list.add(EmiTooltip.chance((recipe != null ? "produce" : "consume"), getStack().getChance()));
		}
		EmiRecipe recipe = getRecipe();
		if (recipe != null) {
			if (recipe.getId() != null && EmiConfig.showRecipeIds) {
				list.add(TooltipComponent.of(EmiPort.ordered(EmiPort.literal(recipe.getId().toString(), Formatting.GRAY))));
			}
			if (canResolve() && EmiConfig.helpLevel.has(HelpLevel.NORMAL)) {
				if (EmiConfig.viewRecipes.isBound()) {
					list.add(TooltipComponent.of(EmiPort.ordered(EmiPort.translatable("emi.resolve.resolve", EmiConfig.viewRecipes.getBindText()))));
				}
				if (EmiConfig.defaultStack.isBound()) {
					list.add(TooltipComponent.of(EmiPort.ordered(EmiPort.translatable("emi.resolve.default", EmiConfig.defaultStack.getBindText()))));
				}
			} else if (EmiConfig.favorite.isBound() && EmiConfig.helpLevel.has(HelpLevel.NORMAL) && EmiFavorites.canFavorite(getStack(), getRecipe())) {
				list.add(TooltipComponent.of(EmiPort.ordered(EmiPort.translatable("emi.favorite_recipe", EmiConfig.favorite.getBindText()))));
			}
			if (EmiConfig.showCostPerBatch && recipe.supportsRecipeTree() && !(recipe instanceof EmiResolutionRecipe)) {
				RecipeCostTooltipComponent rctc = new RecipeCostTooltipComponent(recipe);
				if (rctc.shouldDisplay()) {
					list.add(rctc);
				}
			}
		}
	}

	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int button) {
		if (slotInteraction(bind -> bind.matchesMouse(button))) {
			return true;
		}
		return EmiScreenManager.stackInteraction(new EmiStackInteraction(getStack(), getRecipe(), true),
			bind -> bind.matchesMouse(button));
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (slotInteraction(bind -> bind.matchesKey(keyCode, scanCode))) {
			return true;
		}
		return EmiScreenManager.stackInteraction(new EmiStackInteraction(getStack(), getRecipe(), true),
			bind -> bind.matchesKey(keyCode, scanCode));
	}

	private boolean canResolve() {
		EmiRecipe recipe = getRecipe();
		return recipe != null && recipe.supportsRecipeTree() && RecipeScreen.resolve != null;
	}

	private boolean slotInteraction(Function<EmiBind, Boolean> function) {
		EmiRecipe recipe = getRecipe();
		if (canResolve()) {
			if (function.apply(EmiConfig.defaultStack)) {
				if (BoM.isDefaultRecipe(getStack(), recipe)) {
					BoM.removeRecipe(getStack(), recipe);
				} else {
					BoM.addRecipe(RecipeScreen.resolve, recipe);
				}
				EmiHistory.pop();
				return true;
			} else if (function.apply(EmiConfig.viewRecipes)) {
				BoM.addResolution(RecipeScreen.resolve, recipe);
				EmiHistory.pop();
				return true;
			}
		} else if (recipe != null && recipe.supportsRecipeTree()) {
			if (function.apply(EmiConfig.defaultStack)) {
				if (BoM.isDefaultRecipe(getStack(), recipe)) {
					BoM.removeRecipe(getStack(), recipe);
				} else {
					BoM.addRecipe(getStack(), recipe);
				}
				return true;
			}
		}
		return false;
	}
}
