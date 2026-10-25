package dev.emi.emi.data;

import java.util.List;
import net.minecraft.network.chat.Component;
import dev.emi.emi.api.stack.EmiIngredient;

public record EmiAlias(List<EmiIngredient> stacks, List<String> keys) {

	public static record Baked(List<EmiIngredient> stacks, List<Component> text) {
	}
}
