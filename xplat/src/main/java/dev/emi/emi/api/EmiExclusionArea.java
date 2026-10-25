package dev.emi.emi.api;

import java.util.function.Consumer;
import net.minecraft.client.gui.screens.Screen;
import dev.emi.emi.api.widget.Bounds;

public interface EmiExclusionArea<T extends Screen> {
	
	void addExclusionArea(T screen, Consumer<Bounds> consumer);
}
