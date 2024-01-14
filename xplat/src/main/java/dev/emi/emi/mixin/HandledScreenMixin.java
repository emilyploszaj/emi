package dev.emi.emi.mixin;

import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.emi.emi.EmiPort;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin extends Screen {
	@Shadow
	protected int backgroundWidth, backgroundHeight, x, y;

	private HandledScreenMixin() { super(null); }

	@Intrinsic @Override
	public void renderBackground(MatrixStack raw) {
		super.renderBackground(raw);
	}

	@Dynamic
	@Inject(at = @At("RETURN"), method = "renderBackground(Lnet/minecraft/client/util/math/MatrixStack;)V")
	private void renderBackground(MatrixStack raw, CallbackInfo info) {
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		Window window = client.getWindow();
		int mouseX = (int) (client.mouse.getX() * window.getScaledWidth() / window.getWidth());
		int mouseY = (int) (client.mouse.getY() * window.getScaledHeight() / window.getHeight());
		EmiScreenManager.drawBackground(context, mouseX, mouseY, client.getTickDelta());
	}

	@Inject(at = @At(value = "INVOKE",
			target = "net/minecraft/client/gui/screen/ingame/HandledScreen.drawForeground(Lnet/minecraft/client/util/math/MatrixStack;II)V",
			shift = Shift.AFTER),
		method = "render")
	private void render(MatrixStack raw, int mouseX, int mouseY, float delta, CallbackInfo info) {
		if (EmiAgnos.isForge()) {
			return;
		}
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		context.push();
		context.matrices().translate(-x, -y, 0.0);
		EmiPort.setPositionTexShader();
		EmiScreenManager.render(context, mouseX, mouseY, delta);
		EmiScreenManager.drawForeground(context, mouseX, mouseY, delta);
		context.pop();
	}
}