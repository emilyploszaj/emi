package dev.emi.emi.screen;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.EmiPort;
import dev.emi.emi.config.EmiConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;

public class DisabledToast implements Toast {

	@Override
	public Visibility draw(MatrixStack matrices, ToastManager manager, long time) {
		EmiPort.setPositionTexShader();
		RenderSystem.setShaderTexture(0, TEXTURE);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		manager.drawTexture(matrices, 0, 0, 0, 0, this.getWidth(), this.getHeight());
		MinecraftClient client = manager.getClient();
		DrawableHelper.drawCenteredText(matrices, client.textRenderer, EmiPort.translatable("emi.disabled"), getWidth() / 2, 7, -1);
		DrawableHelper.drawCenteredText(matrices, client.textRenderer, EmiConfig.toggleVisibility.getBindText(), getWidth() / 2, 18, -1);
		if (time > 8_000 || EmiConfig.enabled) {
			return Visibility.HIDE;
		}
		return Visibility.SHOW;
	}
}
