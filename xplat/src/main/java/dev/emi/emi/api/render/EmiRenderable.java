package dev.emi.emi.api.render;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Provides a method to render something at a position
 */
public interface EmiRenderable {
	
	void render(GuiGraphicsExtractor draw, int x, int y, float delta);
}
