package dev.emi.emi.data;

import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.util.Identifier;

public record RecipeDefault(Identifier id, EmiStack stack) {
	
	public boolean matchAll() {
		return stack == null;
	}
}
