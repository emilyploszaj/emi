package dev.emi.emi.api.widget;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.util.math.MatrixStack;

public class FillingArrowWidget extends AnimatedTextureWidget {

	public FillingArrowWidget(int x, int y, int time) {
		super(EmiRenderHelper.WIDGETS, x, y, 24, 17, 44, 17, time, true, false, false);
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		EmiDrawContext context = EmiDrawContext.wrap(matrices);
		context.drawTexture(this.texture, x, y, width, height, u, 0, regionWidth, regionHeight, textureWidth, textureHeight);
		super.render(context.raw(), mouseX, mouseY, delta);
	}
}
