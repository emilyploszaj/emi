package dev.emi.emi.jemi.impl.extras;

import mezz.jei.api.gui.placement.IPlaceable;

public class JemiPlaceable<T extends IPlaceable<T>> implements IPlaceable<T> {
	public int x = 0, y = 0;
	public int width, height;

	public JemiPlaceable(int width, int height) {
		this.width = width;
		this.height = height;
	}

	@Override
	public T setPosition(int xPos, int yPos) {
		this.x = xPos;
		this.y = yPos;
		return (T) this;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}
}
