package dev.emi.emi.runtime;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.emi.emi.EmiPort;
import dev.emi.emi.api.stack.EmiIngredient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

public class EmiDrawContext {
	private final Minecraft client = Minecraft.getInstance();
	private final GuiGraphics context;
	
	private EmiDrawContext(GuiGraphics context) {
		this.context = context;
	}

	public static EmiDrawContext wrap(GuiGraphics context) {
		return new EmiDrawContext(context);
	}

	public GuiGraphics raw() {
		return context;
	}

	public PoseStack matrices() {
		return context.pose();
	}

	public void push() {
		matrices().pushPose();
	}

	public void pop() {
		matrices().popPose();
	}

	public void translate(float x, float y) {
		matrices().translate(x, y, 0);
	}

	public void drawTexture(ResourceLocation texture, int x, int y, int u, int v, int width, int height) {
		drawTexture(texture, x, y, width, height, u, v, width, height, 256, 256);
	}

	public void drawTexture(ResourceLocation texture, int x, int y, int z, float u, float v, int width, int height) {
		drawTexture(texture, x, y, z, u, v, width, height, 256, 256);
	}

	public void drawTexture(ResourceLocation texture, int x, int y, int z, float u, float v, int width, int height, int textureWidth, int textureHeight) {
		EmiPort.setPositionTexShader();
		context.blit(texture, x, y, z, u, v, width, height, textureWidth, textureHeight);
	}

	public void drawTexture(ResourceLocation texture, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
		EmiPort.setPositionTexShader();
		context.blit(texture, x, y, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
	}

	public void fill(int x, int y, int width, int height, int color) {
		context.fill(x, y, x + width, y + height, color);
	}

	public void drawText(Component text, int x, int y) {
		drawText(text, x, y, -1);
	}

	public void drawText(Component text, int x, int y, int color) {
		context.drawString(client.font, text, x, y, color, false);
	}

	public void drawText(FormattedCharSequence text, int x, int y, int color) {
		context.drawString(client.font, text, x, y, color, false);
	}

	public void drawTextWithShadow(Component text, int x, int y) {
		drawTextWithShadow(text, x, y, -1);
	}

	public void drawTextWithShadow(Component text, int x, int y, int color) {
		context.drawString(client.font, text, x, y, color, true);
	}

	public void drawTextWithShadow(FormattedCharSequence text, int x, int y, int color) {
		context.drawString(client.font, text, x, y, color, true);
	}

	public void drawCenteredText(Component text, int x, int y) {
		drawCenteredText(text, x, y, -1);
	}

	public void drawCenteredText(Component text, int x, int y, int color) {
		context.drawString(client.font, text, x - client.font.width(text) / 2, y, color, false);
	}

	public void drawCenteredTextWithShadow(Component text, int x, int y) {
		drawCenteredTextWithShadow(text, x, y, -1);
	}

	public void drawCenteredTextWithShadow(Component text, int x, int y, int color) {
		context.drawCenteredString(client.font, text.getVisualOrderText(), x, y, color);
	}

	public void enableDepthTest() {
		RenderSystem.enableDepthTest();
	}

	public void disableDepthTest() {
		RenderSystem.disableDepthTest();
	}

	public void enableBlend() {
		RenderSystem.enableBlend();
	}

	public void disableBlend() {
		RenderSystem.disableBlend();
	}

	public void resetColor() {
		setColor(1f, 1f, 1f, 1f);
	}

	public void setColor(float r, float g, float b) {
		setColor(r, g, b, 1f);
	}

	public void setColor(float r, float g, float b, float a) {
		raw().setColor(r, g, b, a);
	}

	public void drawStack(EmiIngredient stack, int x, int y) {
		stack.render(raw(), x, y, client.getTimer().getGameTimeDeltaPartialTick(false));
	}

	public void drawStack(EmiIngredient stack, int x, int y, int flags) {
		drawStack(stack, x, y, client.getTimer().getGameTimeDeltaPartialTick(false), flags);
	}

	public void drawStack(EmiIngredient stack, int x, int y, float delta, int flags) {
		stack.render(raw(), x, y, delta, flags);
	}
}
