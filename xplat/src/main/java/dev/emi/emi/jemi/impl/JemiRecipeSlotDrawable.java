package dev.emi.emi.jemi.impl;

import java.util.List;
import java.util.Optional;
import net.minecraft.tags.TagKey;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import com.google.common.collect.Lists;

import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.jemi.widget.JemiSlotWidget;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;

public class JemiRecipeSlotDrawable implements IRecipeSlotDrawable {
	public JemiSlotWidget widget;
	public List<JemiIngredientAcceptor> overrides = Lists.newArrayList();

	@Override
	public Stream<ITypedIngredient<?>> getAllIngredients() {
		return widget.slot.getAllIngredients();
	}

	@Override
	public @Unmodifiable List<@Nullable ITypedIngredient<?>> getAllIngredientsList() {
		return widget.slot.getAllIngredientsList();
	}

	@Override
	public Optional<ITypedIngredient<?>> getDisplayedIngredient() {
		return widget.slot.getDisplayedIngredient();
	}

	@Override
	public Stream<ITypedIngredient<?>> getDisplayedIngredients() {
		return getDisplayedIngredient().stream();
	}

	@Override
	public Optional<TagKey<?>> getTagKey() {
		return widget.slot.getTagKey();
	}

	@Override
	public RecipeIngredientRole getRole() {
		return widget.slot.getRole();
	}

	@Override
	public void drawHighlight(GuiGraphicsExtractor raw, int color) {
		widget.slot.drawHighlight(raw, color);
	}

	@Override
	public Optional<String> getSlotName() {
		return widget.slot.getSlotName();
	}

	@Override
	public void draw(GuiGraphicsExtractor raw) {
		// I don't think I will
	}

	@Override
	public void draw(GuiGraphicsExtractor raw, boolean hovered) {
		// I don't think I will
	}

	@Override
	public void drawTooltip(GuiGraphicsExtractor raw, int mouseX, int mouseY) {
		// EMI draws its own tooltips
	}

	@Override
	public void drawHoverOverlays(GuiGraphicsExtractor raw) {
		// I don't think I will
	}

	@Override
	public List<Component> getTooltip() {
		// Unimplemented
		// Mutable because who knows
		return Lists.newArrayList();
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltipBuilder) {
		// Unimplemented
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return widget.getBounds().contains((int) mouseX, (int) mouseY);
	}

	@Override
	public void setPosition(int x, int y) {
		// Nope
	}

	@Override
	public IIngredientAcceptor<?> createDisplayOverrides() {
		// "Implemented" but also just ignored
		JemiIngredientAcceptor consumer = new JemiIngredientAcceptor(getRole());
		overrides.add(consumer);
		return consumer;
	}

	@Override
	public void clearDisplayOverrides() {
		overrides.clear();
	}

	@Override
	public Rect2i getAreaIncludingBackground() {
		Bounds bounds = widget.getBounds();
		return new Rect2i(bounds.x(), bounds.y(), bounds.width(), bounds.height());
	}
}
