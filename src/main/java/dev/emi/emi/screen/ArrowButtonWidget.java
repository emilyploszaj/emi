package dev.emi.emi.screen;

import java.util.function.BooleanSupplier;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.EmiRenderHelper;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

public class ArrowButtonWidget extends ButtonWidget {
	private final int u, v;
	private final BooleanSupplier isActive;

	public ArrowButtonWidget(int x, int y, int width, int height, int u, int v, BooleanSupplier isActive, PressAction action) {
		super(x, y, width, height, LiteralText.EMPTY, action);
		this.u = u;
		this.v = v;
		this.isActive = isActive;
	}
	
	@Override
	public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, EmiRenderHelper.WIDGETS);
		int v = this.v;
		this.active = this.isActive.getAsBoolean();
		if (!this.active) {
			v += this.height * 2;
		} else if (this.isHovered()) {
			v += this.height;
		}
		RenderSystem.enableDepthTest();
		drawTexture(matrices, this.x, this.y, this.u, v, this.width, this.height, 256, 256);
		if (this.hovered) {
			this.renderTooltip(matrices, mouseX, Math.max(16, mouseY));
		}
	}
}
