package dev.emi.emi.screen;

import dev.emi.emi.EmiPort;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.util.Identifier;

public class DisabledToast implements Toast {
	private static final Identifier TEXTURE = Identifier.of("toast/advancement");

	@Override
	public Visibility draw(DrawContext raw, ToastManager manager, long time) {
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		context.resetColor();
		raw.drawGuiTexture(TEXTURE, 0, 0, this.getWidth(), this.getHeight());
		context.drawCenteredText(EmiPort.translatable("emi.disabled"), getWidth() / 2, 7);
		context.drawCenteredText(EmiConfig.toggleVisibility.getBindText(), getWidth() / 2, 18);
		if (time > 8_000 || EmiConfig.enabled) {
			return Visibility.HIDE;
		}
		return Visibility.SHOW;
	}
}
