package dev.emi.emi.api.recipe;

import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class EmiRecipeCategory {
	public Identifier id;
	public EmiStack icon;
	
	public EmiRecipeCategory(Identifier id, EmiStack icon) {
		this.id = id;
		this.icon = icon;
	}

	public EmiStack getIcon() {
		return icon;
	}

	public Identifier getId() {
		return id;
	}

	public void render(MatrixStack matrices, int x, int y, float delta) {
		icon.renderIcon(matrices, x, y, delta);
	}
}
