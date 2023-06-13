package dev.emi.emi.api.render;

import net.minecraft.client.gui.DrawContext;

/**
 * Provides a method to render something at a position
 */
public interface EmiRenderable {
	
	void render(DrawContext draw, int x, int y, float delta);
}
