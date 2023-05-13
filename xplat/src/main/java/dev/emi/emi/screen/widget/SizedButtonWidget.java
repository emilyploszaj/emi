package dev.emi.emi.screen.widget;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SizedButtonWidget extends ButtonWidget {
	private final BooleanSupplier isActive;
	private final IntSupplier vOffset;
	protected Identifier texture = EmiRenderHelper.BUTTONS;
	protected Supplier<List<Text>> text;
	protected int u, v;

	public SizedButtonWidget(int x, int y, int width, int height, int u, int v, BooleanSupplier isActive, PressAction action) {
		this(x, y, width, height, u, v, isActive, action, () -> 0);
	}

	public SizedButtonWidget(int x, int y, int width, int height, int u, int v, BooleanSupplier isActive, PressAction action,
			List<Text> text) {
		this(x, y, width, height, u, v, isActive, action, () -> 0, () -> text);
	}

	public SizedButtonWidget(int x, int y, int width, int height, int u, int v, BooleanSupplier isActive, PressAction action,
			IntSupplier vOffset) {
		this(x, y, width, height, u, v, isActive, action, vOffset, null);
	}

	public SizedButtonWidget(int x, int y, int width, int height, int u, int v, BooleanSupplier isActive, PressAction action,
			IntSupplier vOffset, Supplier<List<Text>> text) {
		super(x, y, width, height, EmiPort.literal(""), action);
		this.u = u;
		this.v = v;
		this.isActive = isActive;
		this.vOffset = vOffset;
		this.text = text;
	}

	protected int getU(int mouseX, int mouseY) {
		return this.u;
	}

	protected int getV(int mouseX, int mouseY) {
		int v = this.v + vOffset.getAsInt();
		this.active = this.isActive.getAsBoolean();
		if (!this.active) {
			v += this.height * 2;
		} else if (this.isMouseOver(mouseX, mouseY)) {
			v += this.height;
		}
		return v;
	}
	
	@Override
	public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		EmiPort.setPositionTexShader();
		RenderSystem.setShaderTexture(0, texture);
		RenderSystem.enableDepthTest();
		drawTexture(matrices, this.x, this.y, getU(mouseX, mouseY), getV(mouseX, mouseY), this.width, this.height, 256, 256);
		if (this.isMouseOver(mouseX, mouseY) && text != null && this.active) {
			matrices.push();
			RenderSystem.disableDepthTest();
			MinecraftClient client = MinecraftClient.getInstance();
			EmiRenderHelper.drawTooltip(client.currentScreen, matrices, text.get().stream().map(EmiPort::ordered).map(TooltipComponent::of).toList(), mouseX, mouseY);
			matrices.pop();
		}
	}
}
