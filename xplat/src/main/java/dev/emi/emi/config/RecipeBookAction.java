package dev.emi.emi.config;

import dev.emi.emi.EmiPort;
import net.minecraft.text.Text;

public enum RecipeBookAction implements ConfigEnum {
	DEFAULT("default"),
	TOGGLE_CRAFTABLES("toggle-craftables"),
	TOGGLE_VISIBILITY("toggle-visibility"),
	;

	public final String name;

	private RecipeBookAction(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Text getText() {
		return EmiPort.translatable("emi.recipe_book_action." + name.replace("-", "_"));
	}
}
