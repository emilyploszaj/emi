package dev.emi.emi.api.widget;

import java.util.function.BooleanSupplier;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.EmiRenderHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;

public class ButtonWidget extends Widget {
	private final int x, y, width, height, u, v;
	private final BooleanSupplier isActive;
	private final ClickAction action;

	public ButtonWidget(int x, int y, int width, int height, int u, int v, BooleanSupplier isActive, ClickAction action) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.u = u;
		this.v = v;
		this.isActive = isActive;
		this.action = action;
	}

	@Override
	public Bounds getBounds() {
		return new Bounds(x, y, width, height);
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, EmiRenderHelper.WIDGETS);
		int v = this.v;
		boolean active = this.isActive.getAsBoolean();
		if (!active) {
			v += height * 2;
		} else if (getBounds().contains(mouseX, mouseY)) {
			v += this.height;
		}
		RenderSystem.enableDepthTest();
		DrawableHelper.drawTexture(matrices, this.x, this.y, this.u, v, this.width, this.height, 256, 256);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		action.click(mouseX, mouseY, button);
		MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
		return true;
	}

	public static interface ClickAction {

		void click(double mouseX, double mouseY, int button);
	}
}
