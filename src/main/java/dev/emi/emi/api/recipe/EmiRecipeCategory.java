package dev.emi.emi.api.recipe;

import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class EmiRecipeCategory {
	public Identifier id;
	public Renderer renderer;
	
	public EmiRecipeCategory(Identifier id, EmiStack icon) {
		this(id, new Renderer() {

			public void render(MatrixStack matrices, int x, int y, float delta) {
				icon.renderIcon(matrices, x, y, delta);
			}
		});
	}

	public EmiRecipeCategory(Identifier id, Renderer drawable) {
		this.id = id;
		this.renderer = drawable;
	}

	public Identifier getId() {
		return id;
	}

	public void render(MatrixStack matrices, int x, int y, float delta) {
		renderer.render(matrices, x, y, delta);
	}

	public static interface Renderer {

		void render(MatrixStack matrices, int x, int y, float delta);
	}
}
