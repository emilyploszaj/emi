package dev.emi.emi.jemi.impl.extras;

import java.util.List;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IScrollBoxWidget;
import net.minecraft.client.gui.ScreenPos;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.text.StringVisitable;

public class JemiScrollBoxWidget implements IScrollBoxWidget {
	private static final int SCROLL_BAR_WIDTH = 18;
	public int x, y;
	public int width, height;

	public JemiScrollBoxWidget(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width + SCROLL_BAR_WIDTH;
		this.height = height;
	}

	@Override
	public ScreenPos getPosition() {
		return new ScreenPos(x, y);
	}

	@Override
	public ScreenRect getArea() {
		return new ScreenRect(getPosition(), width, height);
	}

	@Override
	public int getContentAreaWidth() {
		return width - SCROLL_BAR_WIDTH;
	}

	@Override
	public int getContentAreaHeight() {
		return height;
	}

	@Override
	public IScrollBoxWidget setContents(IDrawable contents) {
		// Unimplemented
		return this;
	}

	@Override
	public IScrollBoxWidget setContents(List<StringVisitable> text) {
		// Unimplemented
		return this;
	}
	
}
