package dev.emi.emi.api.widget;

import java.util.List;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;

public abstract class Widget implements Drawable {

	public abstract Bounds getBounds();
	
	public abstract void render(MatrixStack matrices, int mouseX, int mouseY, float delta);

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
