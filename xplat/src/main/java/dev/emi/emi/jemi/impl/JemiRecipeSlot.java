package dev.emi.emi.jemi.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.jemi.JemiUtil;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.DrawContext;

@SuppressWarnings("unchecked")
public class JemiRecipeSlot implements IRecipeSlotView {
	public final RecipeIngredientRole role;
	public final boolean large;
	public final int x, y;
	public final Optional<String> name;
	public final IRecipeSlotTooltipCallback tooltipCallback;
	public final IRecipeSlotRichTooltipCallback richTooltipCallback;
	public final OffsetDrawable background, overlay;
	public final Map<IIngredientType<?>, IngredientRenderer<?>> renderers;
	public final TankInfo tankInfo;
	public final EmiIngredient stack;
	public SlotWidget widget;
	public int highlight = 0;

	public JemiRecipeSlot(JemiRecipeSlotBuilder builder) {
		this.role = builder.acceptor.role;
		this.large = builder.large;
		this.x = builder.x;
		this.y = builder.y;
		this.name = builder.name;
		this.tooltipCallback = builder.tooltipCallback;
		this.richTooltipCallback = builder.richTooltipCallback;
		this.background = builder.background;
		this.overlay = builder.overlay;
		this.renderers = builder.renderers;
		this.tankInfo = builder.tankInfo;
		this.stack = builder.acceptor.build();
	}

	public JemiRecipeSlot(RecipeIngredientRole role, EmiStack stack) {
		this.role = role;
		this.large = false;
		this.x = 0;
		this.y = 0;
		this.name = Optional.empty();
		this.tooltipCallback = null;
		this.richTooltipCallback = null;
		this.background = null;
		this.overlay = null;
		this.renderers = null;
		this.tankInfo = null;
		this.stack = stack;
	}

	@Override
	public <T> Stream<T> getIngredients(IIngredientType<T> ingredientType) {
		return (Stream<T>) getAllIngredients().filter(t -> t.getType() == ingredientType).map(t -> t.getIngredient());
	}

	@Override
	public @Unmodifiable List<@Nullable ITypedIngredient<?>> getAllIngredientsList() {
		return getAllIngredients().toList();
	}

	@Override
	public Stream<ITypedIngredient<?>> getAllIngredients() {
		return stack.getEmiStacks().stream().map(JemiUtil::getTyped).filter(Optional::isPresent).map(Optional::get);
	}

	@Override
	public boolean isEmpty() {
		return stack.isEmpty();
	}

	@Override
	public <T> Optional<T> getDisplayedIngredient(IIngredientType<T> ingredientType) {
		Optional<ITypedIngredient<?>> ing = getDisplayedIngredient();
		if (ing.isPresent() && ing.get().getType() == ingredientType) {
			return (Optional<T>) Optional.of(ing.get().getIngredient());
		}
		return Optional.empty();
	}

	@Override
	public Optional<ITypedIngredient<?>> getDisplayedIngredient() {
		return JemiUtil.getTyped(stack.getEmiStacks().get(0));
	}

	@Override
	public Optional<String> getSlotName() {
		return name;
	}

	@Override
	public RecipeIngredientRole getRole() {
		return role;
	}

	@Override
	public void drawHighlight(DrawContext raw, int color) {
		this.highlight = color;
	}

	public static record OffsetDrawable(IDrawable drawable, int xOff, int yOff){
	}

	public static record IngredientRenderer<T>(IIngredientType<T> type, IIngredientRenderer<T> renderer){
	}

	public static record TankInfo(int width, int height, long capacity, boolean showCapacity) {
	}
}
