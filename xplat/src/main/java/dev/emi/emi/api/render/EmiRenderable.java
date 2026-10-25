package dev.emi.emi.api.render;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Provides a method to render something at a position
 */
public interface EmiRenderable {
	
	void render(GuiGraphics draw, int x, int y, float delta);
}
