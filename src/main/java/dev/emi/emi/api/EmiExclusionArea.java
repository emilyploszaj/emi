package dev.emi.emi.api;

import java.util.function.Consumer;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.Rect2i;

public interface EmiExclusionArea<T extends Screen> {
	
	void addExclusionArea(T screen, Consumer<Rect2i> consumer);
}
