package dev.emi.emi.api;

import dev.emi.emi.api.stack.EmiIngredient;
import net.minecraft.client.gui.screen.Screen;

public interface EmiDragDropHandler<T extends Screen> {
	
	/**
	 * Called when a stack is released while being dragged.
	 * @return Whether to consume the event.
	 */
	boolean dropStack(T screen, EmiIngredient stack, int x, int y);
}
