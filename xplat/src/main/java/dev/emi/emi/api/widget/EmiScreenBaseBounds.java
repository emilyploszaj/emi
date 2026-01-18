package dev.emi.emi.api.widget;

import net.minecraft.client.gui.screen.Screen;

public record EmiScreenBaseBounds(Screen screen, Bounds bounds) {
	public static final EmiScreenBaseBounds EMPTY = new EmiScreenBaseBounds(null, Bounds.EMPTY);
}
