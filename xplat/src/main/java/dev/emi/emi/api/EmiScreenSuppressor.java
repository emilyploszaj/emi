package dev.emi.emi.api;

import net.minecraft.client.gui.screen.Screen;

/**
 * Allows a mod to suppress all EMI rendering and input handling for a given screen.
 * When any registered suppressor returns {@code true}, EMI will not render its widgets
 * or handle mouse and keyboard events for that screen.
 *
 * <p>Register via {@link EmiRegistry#addScreenSuppressor} or
 * {@link EmiRegistry#addGenericScreenSuppressor}.
 */
@FunctionalInterface
public interface EmiScreenSuppressor<T extends Screen> {

	/**
	 * @param screen The current screen
	 * @return {@code true} if EMI should be fully suppressed for this screen this frame
	 */
	boolean isSuppressed(T screen);
}
