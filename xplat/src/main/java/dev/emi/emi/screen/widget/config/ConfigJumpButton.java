package dev.emi.emi.screen.widget.config;

import java.util.List;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.widget.SizedButtonWidget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;

public class ConfigJumpButton extends SizedButtonWidget {

	public ConfigJumpButton(int x, int y, int u, int v, PressAction action, List<net.minecraft.text.Text> text) {
		super(x, y, 16, 16, u, v, () -> true, action, text);
		this.texture = EmiRenderHelper.CONFIG;
	}

	@Override
	protected int getV(int mouseX, int mouseY) {
		return this.v;
	}

    @Override
    protected void drawIcon(DrawContext raw, int mouseX, int mouseY, float delta) {
        EmiDrawContext context = EmiDrawContext.wrap(raw);

        int color = 0xFFFFFFFF;
        if (this.isMouseOver(mouseX, mouseY)) {
            color = 0xFF8099FF;
        }

//        context.enableDepthTest();
        context.drawTexture(texture, this.x, this.y, getU(mouseX, mouseY), getV(mouseX, mouseY), this.width, this.height, color);
        if (this.isMouseOver(mouseX, mouseY) && text != null && this.active) {
            context.push();
//            context.matrices().translate(0, 0, 100);
//            context.disableDepthTest();
            MinecraftClient client = MinecraftClient.getInstance();
            EmiRenderHelper.drawTooltip(client.currentScreen, context, text.get().stream().map(EmiPort::ordered).map(TooltipComponent::of).toList(), mouseX, mouseY);
            context.pop();
        }
//        context.resetColor();
    }
}
