package dev.emi.emi.screen.widget.config;

import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.widget.SizedButtonWidget;

public class ConfigJumpButton extends SizedButtonWidget {

	public ConfigJumpButton(int x, int y, int u, int v, OnPress action, List<Component> text) {
		super(x, y, 16, 16, u, v, () -> true, action, text);
		this.texture = EmiRenderHelper.CONFIG;
	}

	@Override
	protected int getV(int mouseX, int mouseY) {
		return this.v;
	}

	@Override
	public void renderWidget(GuiGraphics raw, int mouseX, int mouseY, float delta) {
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		if (this.isMouseOver(mouseX, mouseY)) {
			context.setColor(0.5f, 0.6f, 1f);
		}
		context.push();
		context.matrices().translate(0, 0, 100);
		super.renderWidget(raw, mouseX, mouseY, delta);
		context.pop();
		context.resetColor();
	}
}
