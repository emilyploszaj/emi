package dev.emi.emi.api.widget;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;

public class FillingArrowWidget extends AnimatedTextureWidget {

	public FillingArrowWidget(int x, int y, int time) {
		super(EmiRenderHelper.WIDGETS, x, y, 24, 17, 44, 17, time, true, false, false);
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		EmiPort.setPositionTexShader();
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		RenderSystem.setShaderTexture(0, this.texture);
		DrawableHelper.drawTexture(matrices, x, y, width, height, u, 0, regionWidth, regionHeight, textureWidth, textureHeight);
		super.render(matrices, mouseX, mouseY, delta);
	}
}
