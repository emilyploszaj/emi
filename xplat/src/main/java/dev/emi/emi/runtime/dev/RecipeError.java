package dev.emi.emi.runtime.dev;

import java.util.List;

import net.minecraft.client.gui.tooltip.TooltipComponent;

public record RecipeError(Severity severity, List<TooltipComponent> tooltip) {
	
	public static enum Severity {
		ERROR,
		WARNING
	}
}
