package dev.emi.emi.api.widget;

import java.util.function.BooleanSupplier;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

public class ButtonWidget extends Widget {
	protected final int x, y, width, height, u, v;
	protected final BooleanSupplier isActive;
	protected final ClickAction action;
	protected final Identifier texture;

	public ButtonWidget(int x, int y, int width, int height, int u, int v, BooleanSupplier isActive, ClickAction action) {
		this(x, y, width, height, u, v, EmiRenderHelper.WIDGETS, isActive, action);
	}

	public ButtonWidget(int x, int y, int width, int height, int u, int v, Identifier texture, BooleanSupplier isActive, ClickAction action) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.u = u;
		this.v = v;
		this.texture = texture;
		this.isActive = isActive;
		this.action = action;
	}

	@Override
	public Bounds getBounds() {
		return new Bounds(x, y, width, height);
	}
	
	@Override
	public void render(DrawContext draw, int mouseX, int mouseY, float delta) {
		EmiDrawContext context = EmiDrawContext.wrap(draw);
		int v = this.v;
		boolean active = this.isActive.getAsBoolean();
		if (!active) {
			v += height * 2;
		} else if (getBounds().contains(mouseX, mouseY)) {
			v += this.height;
		}
		RenderSystem.enableDepthTest();
		context.drawTexture(texture, this.x, this.y, this.u, v, this.width, this.height);
	}

	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int button) {
		action.click(mouseX, mouseY, button);
		MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
		return true;
	}

	public static interface ClickAction {

		void click(double mouseX, double mouseY, int button);
	}
}
