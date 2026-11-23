package dev.emi.emi.runtime.dev;

import java.util.List;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

public record RecipeError(Severity severity, List<ClientTooltipComponent> tooltip) {
	
	public static enum Severity {
		ERROR,
		WARNING
	}
}
