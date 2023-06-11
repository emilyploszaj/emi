package dev.emi.emi.screen.widget.config;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.screen.widget.SizedButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class ConfigJumpButton extends SizedButtonWidget {

	public ConfigJumpButton(int x, int y, int u, int v, PressAction action, List<Text> text) {
		super(x, y, 16, 16, u, v, () -> true, action, text);
		this.texture = EmiRenderHelper.CONFIG;
	}

	@Override
	protected int getV(int mouseX, int mouseY) {
		return this.v;
	}

	@Override
	public void renderButton(MatrixStack raw, int mouseX, int mouseY, float delta) {
		if (this.isMouseOver(mouseX, mouseY)) {
			RenderSystem.setShaderColor(0.5f, 0.6f, 1f, 1f);
		}
		super.renderButton(raw, mouseX, mouseY, delta);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
	}
}
