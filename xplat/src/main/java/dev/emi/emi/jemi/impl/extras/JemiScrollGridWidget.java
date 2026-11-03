package dev.emi.emi.jemi.impl.extras;

import mezz.jei.api.gui.widgets.ScrollbarVisibility;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import java.util.List;
import java.util.Optional;

import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.gui.widgets.IScrollGridWidget;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;

public class JemiScrollGridWidget implements IScrollGridWidget {
	private static final int SCROLL_BAR_WIDTH = 18;
	public List<IRecipeSlotDrawable> slots;
	public int x, y;
	public int gridWidth, gridHeight;
	public int width, height;

	public JemiScrollGridWidget(List<IRecipeSlotDrawable> slots, int x, int y, int gridWidth, int gridHeight) {
		this.slots = slots;
		this.x = x;
		this.y = y;
		this.gridWidth = gridWidth;
		this.gridHeight = gridHeight;
		this.width = gridWidth * 18 + SCROLL_BAR_WIDTH;
		this.height = gridHeight * 18;
	}

	@Override
	public Optional<RecipeSlotUnderMouse> getSlotUnderMouse(double mouseX, double mouseY) {
		// Unimplemented
		return Optional.empty();
	}

	@Override
	public ScreenPosition getPosition() {
		return new ScreenPosition(x, y);
	}

	@Override
	public IScrollGridWidget setPosition(int xPos, int yPos) {
		this.x = xPos;
		this.y = yPos;
		return this;
	}

	@Override
	public IScrollGridWidget setPosition(int areaX, int areaY, int areaWidth, int areaHeight, HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment) {
		return setPosition(areaX + horizontalAlignment.getXPos(areaWidth, getWidth()), areaY + verticalAlignment.getYPos(areaHeight, getHeight()));
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public ScreenRectangle getScreenRectangle() {
		return new ScreenRectangle(getPosition(), getWidth(), getHeight());
	}

	@Override
	public IScrollGridWidget setScrollbarVisibility(ScrollbarVisibility visibility) {
		// EMI always shows scrollbars
		return this;
	}

	@Override
	public IScrollGridWidget setSlots(List<IRecipeSlotDrawable> slots) {
		this.slots = slots;
		return this;
	}
}
