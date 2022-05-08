package dev.emi.emi.api;

import java.util.function.Consumer;

import dev.emi.emi.api.widget.Bounds;
import net.minecraft.client.gui.screen.Screen;

public interface EmiExclusionArea<T extends Screen> {
	
	void addExclusionArea(T screen, Consumer<Bounds> consumer);
}
