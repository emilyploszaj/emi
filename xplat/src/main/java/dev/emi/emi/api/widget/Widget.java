package dev.emi.emi.api.widget;

import java.util.List;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.tooltip.TooltipComponent;

public abstract class Widget implements Drawable {

	public abstract Bounds getBounds();
	
	public abstract void render(DrawContext draw, int mouseX, int mouseY, float delta);

	public List<TooltipComponent> getTooltip(int mouseX, int mouseY) {
		return List.of();
	}
	
	public boolean mouseClicked(int mouseX, int mouseY, int button) {
		return false;
	}

	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return false;
	}
}
