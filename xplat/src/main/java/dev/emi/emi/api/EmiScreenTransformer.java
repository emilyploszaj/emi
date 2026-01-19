package dev.emi.emi.api;

import dev.emi.emi.api.widget.EmiScreenBaseBounds;
import net.minecraft.client.gui.screen.Screen;

public interface EmiScreenTransformer {


	/**
	 * Attempts to transform the given screen into EMI screen data.
	 *
	 * @param screen The screen to transform
	 * @return The transformed screen data, or EMPTY if this transformer cannot handle the screen
	 */
	EmiScreenBaseBounds transform(Screen screen);

	/**
	 * Returns whether this transformer can handle the given screen.
	 * This method is optional - if not overridden, EMI will call transform() and check for null.
	 * Override this for better performance when transform() is expensive.
	 *
	 * @param screen The screen to check
	 * @return true if this transformer can handle the screen
	 */
	default boolean canTransform(Screen screen) {
		return true; // Default implementation accepts all screens
	}

	/**
	 * Gets the priority of this transformer.
	 * Transformers with higher priority are checked first.
	 * The default priority is 0.
	 *
	 * @return The priority of this transformer
	 */
	default int getPriority() {
		return 0;
	}
}
