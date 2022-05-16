package dev.emi.emi.screen.widget;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.EmiRenderHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class SizedButtonWidget extends ButtonWidget {
	private final int u, v;
	private final BooleanSupplier isActive;
	private final IntSupplier vOffset;
	private final Supplier<List<Text>> text;

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
		super(x, y, width, height, LiteralText.EMPTY, action);
		this.u = u;
		this.v = v;
		this.isActive = isActive;
		this.vOffset = vOffset;
		this.text = text;
	}
	
	@Override
	public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, EmiRenderHelper.WIDGETS);
		int v = this.v + vOffset.getAsInt();
		this.active = this.isActive.getAsBoolean();
		if (!this.active) {
			v += this.height * 2;
		} else if (this.isHovered()) {
			v += this.height;
		}
		RenderSystem.enableDepthTest();
		drawTexture(matrices, this.x, this.y, this.u, v, this.width, this.height, 256, 256);
		if (this.isHovered() && text != null && this.active) {
			MinecraftClient client = MinecraftClient.getInstance();
			client.currentScreen.renderTooltip(matrices, text.get(), mouseX, mouseY);
		}
	}
}
