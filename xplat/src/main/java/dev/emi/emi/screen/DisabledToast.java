package dev.emi.emi.screen;

import dev.emi.emi.EmiPort;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.resources.ResourceLocation;

public class DisabledToast implements Toast {
	private static final ResourceLocation TEXTURE = EmiPort.id("toast/advancement");

	@Override
	public Visibility render(GuiGraphics raw, ToastComponent manager, long time) {
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		context.resetColor();
		raw.blitSprite(TEXTURE, 0, 0, this.width(), this.height());
		context.drawCenteredText(EmiPort.translatable("emi.disabled"), width() / 2, 7);
		context.drawCenteredText(EmiConfig.toggleVisibility.getBindText(), width() / 2, 18);
		if (time > 8_000 || EmiConfig.enabled) {
			return Visibility.HIDE;
		}
		return Visibility.SHOW;
	}
}
