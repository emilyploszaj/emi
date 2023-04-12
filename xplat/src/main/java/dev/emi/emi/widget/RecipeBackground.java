package dev.emi.emi.widget;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class RecipeBackground extends Widget {
	private static final Identifier TEXTURE = new Identifier("emi", "textures/gui/background.png");
	private final int x, y, width, height;

	public RecipeBackground(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	@Override
	public Bounds getBounds() {
		return new Bounds(0, 0, 0, 0);
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		RenderSystem.setShaderTexture(0, TEXTURE);
		EmiRenderHelper.drawNinePatch(matrices, x, y, width, height, 27, 0, 4, 1);
	}
}
