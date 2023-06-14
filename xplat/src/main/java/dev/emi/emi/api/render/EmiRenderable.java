package dev.emi.emi.api.render;

import net.minecraft.client.util.math.MatrixStack;

/**
 * Provides a method to render something at a position
 */
public interface EmiRenderable {
	
	void render(MatrixStack matrices, int x, int y, float delta);
}
