package dev.emi.emi.runtime;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.stack.EmiIngredient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class EmiDrawContext {
	private final MinecraftClient client = MinecraftClient.getInstance();
	private final MatrixStack matrices;
	
	private EmiDrawContext(MatrixStack matrices) {
		this.matrices = matrices;
	}

	public static EmiDrawContext wrap(MatrixStack matrices) {
		return new EmiDrawContext(matrices);
	}

	public MatrixStack raw() {
		return matrices;
	}

	public MatrixStack matrices() {
		return matrices;
	}

	public void push() {
		matrices.push();
	}

	public void pop() {
		matrices.pop();
	}

	public void drawTexture(Identifier texture, int x, int y, int u, int v, int width, int height) {
		drawTexture(texture, x, y, width, height, u, v, width, height, 256, 256);
	}

	public void drawTexture(Identifier texture, int x, int y, int z, float u, float v, int width, int height) {
		drawTexture(texture, x, y, z, u, v, width, height, 256, 256);
	}

	public void drawTexture(Identifier texture, int x, int y, int z, float u, float v, int width, int height, int textureWidth, int textureHeight) {
		EmiPort.setPositionTexShader();
		RenderSystem.setShaderTexture(0, texture);
		DrawableHelper.drawTexture(matrices, x, y, z, u, v, width, height, textureWidth, textureHeight);
	}

	public void drawTexture(Identifier texture, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
		EmiPort.setPositionTexShader();
		RenderSystem.setShaderTexture(0, texture);
		DrawableHelper.drawTexture(matrices, x, y, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
	}

	public void fill(int x, int y, int width, int height, int color) {
		DrawableHelper.fill(matrices, x, y, x + width, y + height, color);
	}

	public void drawText(Text text, int x, int y) {
		drawText(text, x, y, -1);
	}

	public void drawText(Text text, int x, int y, int color) {
		client.textRenderer.draw(matrices, text, x, y, color);
	}

	public void drawText(OrderedText text, int x, int y, int color) {
		client.textRenderer.draw(matrices, text, x, y, color);
	}

	public void drawTextWithShadow(Text text, int x, int y) {
		drawTextWithShadow(text, x, y, -1);
	}

	public void drawTextWithShadow(Text text, int x, int y, int color) {
		client.textRenderer.drawWithShadow(matrices, text, x, y, color);
	}

	public void drawTextWithShadow(OrderedText text, int x, int y, int color) {
		client.textRenderer.drawWithShadow(matrices, text, x, y, color);
	}

	public void drawCenteredText(Text text, int x, int y) {
		drawCenteredText(text, x, y, -1);
	}

	public void drawCenteredText(Text text, int x, int y, int color) {
		client.textRenderer.draw(matrices, text, x - client.textRenderer.getWidth(text) / 2, y, color);
	}

	public void drawCenteredTextWithShadow(Text text, int x, int y) {
		drawCenteredTextWithShadow(text, x, y, -1);
	}

	public void drawCenteredTextWithShadow(Text text, int x, int y, int color) {
		DrawableHelper.drawCenteredTextWithShadow(matrices, client.textRenderer, text.asOrderedText(), x, y, color);
	}

	public void resetColor() {
		setColor(1f, 1f, 1f, 1f);
	}

	public void setColor(float r, float g, float b) {
		setColor(r, g, b, 1f);
	}

	public void setColor(float r, float g, float b, float a) {
		RenderSystem.setShaderColor(r, g, b, a);
	}

	public void drawStack(EmiIngredient stack, int x, int y) {
		stack.render(raw(), x, y, client.getTickDelta());
	}

	public void drawStack(EmiIngredient stack, int x, int y, int flags) {
		drawStack(stack, x, y, client.getTickDelta(), flags);
	}

	public void drawStack(EmiIngredient stack, int x, int y, float delta, int flags) {
		stack.render(raw(), x, y, delta, flags);
	}
}
