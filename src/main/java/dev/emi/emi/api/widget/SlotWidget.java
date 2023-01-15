package dev.emi.emi.api.widget;

import java.util.List;
import java.util.function.Supplier;

import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.ApiStatus;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.EmiHistory;
import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiResolutionRecipe;
import dev.emi.emi.api.render.EmiRender;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.screen.RecipeScreen;
import dev.emi.emi.screen.tooltip.RecipeCostTooltipComponent;
import net.minecraft.client.gui.DrawableHelper;
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

	@ApiStatus.Internal
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
	 * Display the slot as a 26x26 vanilla output slot.
	 * This is a purely visual change.
	 */
	public SlotWidget output(boolean output) {
		this.output = output;
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
	 * The size of the texture drawn is determined by whether the slot is visually an output,
	 * which is set by {@link SlotWidget#output()}.
	 * {@link SlotWidget#custom()} is an alternative for custom sizing.
	 */
	public SlotWidget backgroundTexture(Identifier id, int u, int v) {
		this.textureId = id;
		this.u = u;
		this.v = v;
		return this;
	}

	/**
	 * @see {@link SlotWidget#customBackground}
	 */
	@Deprecated
	public void custom(Identifier id, int u, int v, int width, int height) {
		backgroundTexture(id, u, v);
		this.custom = true;
		this.customWidth = width;
		this.customHeight = height;
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
		Bounds bounds = getBounds();
		EmiPort.setPositionTexShader();
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		int width = bounds.width();
		int height = bounds.height();
		if (drawBack) {
			if (textureId != null) {
				RenderSystem.setShaderTexture(0, textureId);
				DrawableHelper.drawTexture(matrices, bounds.x(), bounds.y(), width, height, u, v, width, height, 256, 256);
			} else {
				RenderSystem.setShaderTexture(0, EmiRenderHelper.WIDGETS);
				if (output) {
					DrawableHelper.drawTexture(matrices, bounds.x(), bounds.y(), 26, 26, 18, 0, 26, 26, 256, 256);
				} else {
					DrawableHelper.drawTexture(matrices, bounds.x(), bounds.y(), 18, 18, 0, 0, 18, 18, 256, 256);
				}
			}
		}

		int xOff = (width - 16) / 2;
		int yOff = (height - 16) / 2;
		getStack().render(matrices, bounds.x() + xOff, bounds.y() + yOff, delta);

		drawOverlay(matrices, mouseX, mouseY, delta);
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

		if (bounds.contains(mouseX, mouseY)) {
			drawSlotHighlight(matrices, bounds);
		}
	}

	public void drawSlotHighlight(MatrixStack matrices, Bounds bounds) {
		if (EmiConfig.showHoverOverlay) {
			RenderSystem.disableDepthTest();
			EmiRenderHelper.drawSlotHightlight(matrices, bounds.x() + 1, bounds.y() + 1, bounds.width() - 2, bounds.height() - 2);
			RenderSystem.enableDepthTest();
		}
	}
	
	@Override
	public List<TooltipComponent> getTooltip(int mouseX, int mouseY) {
		List<TooltipComponent> list = Lists.newArrayList();
		if (getStack().isEmpty()) {
			return list;
		}
		list.addAll(getStack().getTooltip());
		for (Supplier<TooltipComponent> supplier : tooltipSuppliers) {
			list.add(supplier.get());
		}
		EmiRecipe recipe = getRecipe();
		if (recipe != null) {
			if (recipe.getId() != null && EmiConfig.showRecipeIds) {
				list.add(TooltipComponent.of(EmiPort.ordered(EmiPort.literal(recipe.getId().toString(), Formatting.GRAY))));
			}
			if (EmiConfig.favorite.isBound()) {
				list.add(TooltipComponent.of(EmiPort.ordered(EmiPort.translatable("emi.favorite_recipe", EmiConfig.favorite.getBindText()))));
			}
			if (RecipeScreen.resolve != null) {
				list.add(TooltipComponent.of(EmiPort.ordered(EmiPort.translatable("emi.resolve", Formatting.GREEN))));
			}
			if (EmiConfig.showCostPerBatch && recipe.supportsRecipeTree() && !(recipe instanceof EmiResolutionRecipe)) {
				RecipeCostTooltipComponent rctc = new RecipeCostTooltipComponent(recipe);
				if (!rctc.tree.fractionalCosts.isEmpty()) {
					list.add(rctc);
				}
			}
		}
		return list;
	}

	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int button) {
		if (button == 0 && getRecipe() != null && getRecipe().supportsRecipeTree() && RecipeScreen.resolve != null) {
			BoM.addResolution(RecipeScreen.resolve, getRecipe());
			EmiHistory.pop();
			return true;
		} else if (EmiScreenManager.stackInteraction(new EmiStackInteraction(getStack(), getRecipe(), true),
				bind -> bind.matchesMouse(button))) {
			return true;
		}
		return false;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return EmiScreenManager.stackInteraction(new EmiStackInteraction(getStack(), getRecipe(), true),
			bind -> bind.matchesKey(keyCode, scanCode));
	}
}
