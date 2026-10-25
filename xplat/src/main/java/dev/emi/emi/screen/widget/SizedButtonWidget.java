package dev.emi.emi.screen.widget;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.runtime.EmiDrawContext;

public class SizedButtonWidget extends Button {
	private final BooleanSupplier isActive;
	private final IntSupplier vOffset;
	protected ResourceLocation texture = EmiRenderHelper.BUTTONS;
	protected Supplier<List<Component>> text;
	protected int u, v;

	public SizedButtonWidget(int x, int y, int width, int height, int u, int v, BooleanSupplier isActive, OnPress action) {
		this(x, y, width, height, u, v, isActive, action, () -> 0);
	}

	public SizedButtonWidget(int x, int y, int width, int height, int u, int v, BooleanSupplier isActive, OnPress action,
			List<Component> text) {
		this(x, y, width, height, u, v, isActive, action, () -> 0, () -> text);
	}

	public SizedButtonWidget(int x, int y, int width, int height, int u, int v, BooleanSupplier isActive, OnPress action,
			IntSupplier vOffset) {
		this(x, y, width, height, u, v, isActive, action, vOffset, null);
	}

	public SizedButtonWidget(int x, int y, int width, int height, int u, int v, BooleanSupplier isActive, OnPress action,
			IntSupplier vOffset, Supplier<List<Component>> text) {
		super(x, y, width, height, EmiPort.literal(""), action, s -> s.get());
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
	public void renderWidget(GuiGraphics raw, int mouseX, int mouseY, float delta) {
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		context.enableDepthTest();
		context.drawTexture(texture, this.x, this.y, getU(mouseX, mouseY), getV(mouseX, mouseY), this.width, this.height);
		if (this.isMouseOver(mouseX, mouseY) && text != null && this.active) {
			context.push();
			context.disableDepthTest();
			Minecraft client = Minecraft.getInstance();
			EmiRenderHelper.drawTooltip(client.screen, context, text.get().stream().map(EmiPort::ordered).map(ClientTooltipComponent::create).toList(), mouseX, mouseY);
			context.pop();
		}
	}
}
