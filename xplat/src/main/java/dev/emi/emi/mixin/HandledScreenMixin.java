package dev.emi.emi.mixin;

import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.screen.EmiScreen;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.search.EmiSearch;
import dev.emi.emi.search.EmiSearch.CompiledQuery;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.slot.Slot;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin extends Screen implements EmiScreen {
	@Shadow
	protected int backgroundWidth, backgroundHeight, x, y;

	private HandledScreenMixin() { super(null); }

	@Inject(at = @At(value = "TAIL"), method = "init")
	private void init(CallbackInfo info) {
		EmiScreenManager.addWidgets(this);
	}

	@Intrinsic @Override
	public void renderBackground(MatrixStack matrices) {
		super.renderBackground(matrices);
	}

	@Dynamic
	@Inject(at = @At("RETURN"), method = "renderBackground(Lnet/minecraft/client/util/math/MatrixStack;)V")
	private void renderBackground(MatrixStack matrices, CallbackInfo info) {
		Window window = client.getWindow();
		int mouseX = (int) (client.mouse.getX() * window.getScaledWidth() / window.getWidth());
		int mouseY = (int) (client.mouse.getY() * window.getScaledHeight() / window.getHeight());
		EmiScreenManager.drawBackground(matrices, mouseX, mouseY, client.getTickDelta());
	}

	@Inject(at = @At(value = "INVOKE",
			target = "net/minecraft/client/gui/screen/ingame/HandledScreen.drawForeground(Lnet/minecraft/client/util/math/MatrixStack;II)V",
			shift = Shift.BEFORE),
		method = "render")
	private void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo info) {
		MatrixStack viewStack = RenderSystem.getModelViewStack();
		viewStack.push();
		viewStack.translate(-x, -y, 0.0);
		RenderSystem.applyModelViewMatrix();
		EmiPort.setPositionTexShader();
		EmiScreenManager.render(matrices, mouseX, mouseY, delta);
		viewStack.pop();
		RenderSystem.applyModelViewMatrix();
	}

	@Inject(at = @At(value = "INVOKE",
			target = "net/minecraft/client/gui/screen/ingame/HandledScreen.drawForeground(Lnet/minecraft/client/util/math/MatrixStack;II)V",
			shift = Shift.AFTER),
		method = "render")
	private void renderForeground(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo info) {
		MatrixStack viewStack = RenderSystem.getModelViewStack();
		viewStack.push();
		viewStack.translate(-x, -y, 0.0);
		RenderSystem.applyModelViewMatrix();
		EmiPort.setPositionTexShader();
		EmiScreenManager.drawForeground(matrices, mouseX, mouseY, delta);
		viewStack.pop();
		RenderSystem.applyModelViewMatrix();
	}

	@Inject(at = @At("TAIL"), method = "drawSlot")
	private void drawSlot(MatrixStack matrices, Slot slot, CallbackInfo info) {
		if (EmiScreenManager.search.highlight) {
			CompiledQuery query = EmiSearch.compiledQuery;
			if (query != null && !query.test(EmiStack.of(slot.getStack()))) {
				matrices.push();
				matrices.translate(0, 0, 300);
				DrawableHelper.fill(matrices, slot.x - 1, slot.y - 1, slot.x + 17, slot.y + 17, 0x77000000);
				matrices.pop();
			}
		}
	}

	@Override
	public int emi$getLeft() {
		if (this instanceof RecipeBookProvider provider) {
			if (provider.getRecipeBookWidget().isOpen()) {
				return x - 177;
			}
		}
		return x;
	}

	@Override
	public int emi$getRight() {
		return x + backgroundWidth;
	}

	@Override
	public int emi$getTop() {
		return y;
	}

	@Override
	public int emi$getBottom() {
		return y + backgroundHeight;
	}
}