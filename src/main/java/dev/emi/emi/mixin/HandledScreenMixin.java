package dev.emi.emi.mixin;

import com.mojang.blaze3d.systems.RenderSystem;

import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.screen.EmiScreen;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.search.EmiSearch.CompiledQuery;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.slot.Slot;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin extends Screen implements EmiScreen {
	@Shadow
	protected int backgroundWidth, backgroundHeight, x, y;

	private HandledScreenMixin() { super(null); }

	@Inject(at = @At(value = "TAIL"), method = "init")
	private void init(CallbackInfo info) {
		this.client.keyboard.setRepeatEvents(true);
		EmiScreenManager.addWidgets(this);
	}

	@Inject(at = @At(value = "INVOKE",
			target = "net/minecraft/client/gui/screen/ingame/HandledScreen.drawForeground(Lnet/minecraft/client/util/math/MatrixStack;II)V",
			shift = Shift.AFTER),
		method = "render")
	private void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo info) {
		MatrixStack viewStack = RenderSystem.getModelViewStack();
		viewStack.push();
		viewStack.translate(-x, -y, 0.0);
		RenderSystem.applyModelViewMatrix();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		EmiScreenManager.render(matrices, mouseX, mouseY, delta);
		viewStack.pop();
		RenderSystem.applyModelViewMatrix();
	}

	@Inject(at = @At("TAIL"), method = "drawSlot")
	private void drawSlot(MatrixStack matrices, Slot slot, CallbackInfo info) {
		if (EmiScreenManager.search.highlight) {
			CompiledQuery query = new CompiledQuery(EmiScreenManager.search.getText());
			if (!query.test(EmiStack.of(slot.getStack()))) {
				matrices.push();
				matrices.translate(0, 0, 300);
				DrawableHelper.fill(matrices, slot.x - 1, slot.y - 1, slot.x + 17, slot.y + 17, 0x77000000);
				matrices.pop();
			}
		}
	}

	@Intrinsic @Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		return super.mouseScrolled(mouseX, mouseY, amount);
	}

	@Inject(at = @At("HEAD"), method = "mouseScrolled(DDD)Z", cancellable = true)
	private void mouseScrolled(double mouseX, double mouseY, double amount, CallbackInfoReturnable<Boolean> info) {
		if (EmiScreenManager.mouseScrolled(mouseX, mouseY, amount)) {
			info.setReturnValue(true);
		}
	}

	@Inject(at = @At("HEAD"), method = "mouseClicked(DDI)Z", cancellable = true)
	public void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> info) {
		if (EmiScreenManager.mouseClicked(mouseX, mouseY, button)) {
			info.setReturnValue(true);
		}
	}
	
	@Inject(at = @At("HEAD"), method = "mouseReleased(DDI)Z", cancellable = true)
	public void mouseReleased(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> info) {
		if (EmiScreenManager.mouseReleased(mouseX, mouseY, button)) {
			info.setReturnValue(true);
		}
	}

	@Inject(at = @At("HEAD"), method = "mouseDragged(DDIDD)Z", cancellable = true)
    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY, CallbackInfoReturnable<Boolean> info) {
		if (EmiScreenManager.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
			info.setReturnValue(true);
		}
	}

	@Inject(at = @At("HEAD"), method = "keyPressed(III)Z", cancellable = true)
	//@Inject(at = @At(value = "INVOKE", target = "net/minecraft/client/option/KeyBinding.matchesKey(II)Z", ordinal = 0),
	//	method = "keyPressed(III)Z", cancellable = true)
	public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> info) {
		if (EmiScreenManager.keyPressed(keyCode, scanCode, modifiers)) {
			info.setReturnValue(true);
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