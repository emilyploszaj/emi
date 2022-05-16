package dev.emi.emi.api;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.gui.screen.Screen;

public interface EmiStackProvider<T extends Screen> {
	
	/**
	 * Gets the EmiIngredient at the provided location.
	 * Should <b>never</b> return null, instead use {@link EmiStack.EMPTY} to indicate the absence of a stack.
	 */
	EmiIngredient getStackAt(T screen, int x, int y);
}
