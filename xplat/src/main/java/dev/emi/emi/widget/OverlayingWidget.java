package dev.emi.emi.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * A recipe widget that draws its own hover overlays and tooltips, drawn after the rest of the screen while hovered
 */
public interface OverlayingWidget {

	void renderOverlay(GuiGraphicsExtractor draw, int mouseX, int mouseY);
}
