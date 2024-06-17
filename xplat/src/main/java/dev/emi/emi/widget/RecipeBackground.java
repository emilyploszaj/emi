package dev.emi.emi.widget;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public class RecipeBackground extends Widget {
	private static final Identifier TEXTURE = EmiPort.id("emi", "textures/gui/background.png");
	private final int x, y, width, height;

	public RecipeBackground(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	@Override
	public Bounds getBounds() {
		return Bounds.EMPTY;
	}

	@Override
	public void render(DrawContext raw, int mouseX, int mouseY, float delta) {
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		EmiRenderHelper.drawNinePatch(context, TEXTURE, x, y, width, height, 27, 0, 4, 1);
	}
}
