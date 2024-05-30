package dev.emi.emi.runtime;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.stack.EmiIngredient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class EmiDrawContext {
	private final MinecraftClient client = MinecraftClient.getInstance();
	private final DrawContext context;
	
	private EmiDrawContext(DrawContext context) {
		this.context = context;
	}

	public static EmiDrawContext wrap(DrawContext context) {
		return new EmiDrawContext(context);
	}

	public DrawContext raw() {
		return context;
	}

	public MatrixStack matrices() {
		return context.getMatrices();
	}

	public void push() {
		matrices().push();
	}

	public void pop() {
		matrices().pop();
	}

	public void drawTexture(Identifier texture, int x, int y, int u, int v, int width, int height) {
		drawTexture(texture, x, y, width, height, u, v, width, height, 256, 256);
	}

	public void drawTexture(Identifier texture, int x, int y, int z, float u, float v, int width, int height) {
		drawTexture(texture, x, y, z, u, v, width, height, 256, 256);
	}

	public void drawTexture(Identifier texture, int x, int y, int z, float u, float v, int width, int height, int textureWidth, int textureHeight) {
		EmiPort.setPositionTexShader();
		context.drawTexture(texture, x, y, z, u, v, width, height, textureWidth, textureHeight);
	}

	public void drawTexture(Identifier texture, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
		EmiPort.setPositionTexShader();
		context.drawTexture(texture, x, y, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
	}

	public void fill(int x, int y, int width, int height, int color) {
		context.fill(x, y, x + width, y + height, color);
	}

	public void drawText(Text text, int x, int y) {
		drawText(text, x, y, -1);
	}

	public void drawText(Text text, int x, int y, int color) {
		context.drawText(client.textRenderer, text, x, y, color, false);
	}

	public void drawText(OrderedText text, int x, int y, int color) {
		context.drawText(client.textRenderer, text, x, y, color, false);
	}

	public void drawTextWithShadow(Text text, int x, int y) {
		drawTextWithShadow(text, x, y, -1);
	}

	public void drawTextWithShadow(Text text, int x, int y, int color) {
		context.drawText(client.textRenderer, text, x, y, color, true);
	}

	public void drawTextWithShadow(OrderedText text, int x, int y, int color) {
		context.drawText(client.textRenderer, text, x, y, color, true);
	}

	public void drawCenteredText(Text text, int x, int y) {
		drawCenteredText(text, x, y, -1);
	}

	public void drawCenteredText(Text text, int x, int y, int color) {
		context.drawText(client.textRenderer, text, x - client.textRenderer.getWidth(text) / 2, y, color, false);
	}

	public void drawCenteredTextWithShadow(Text text, int x, int y) {
		drawCenteredTextWithShadow(text, x, y, -1);
	}

	public void drawCenteredTextWithShadow(Text text, int x, int y, int color) {
		context.drawCenteredTextWithShadow(client.textRenderer, text.asOrderedText(), x, y, color);
	}

	public void resetColor() {
		setColor(1f, 1f, 1f, 1f);
	}

	public void setColor(float r, float g, float b) {
		setColor(r, g, b, 1f);
	}

	public void setColor(float r, float g, float b, float a) {
		raw().setShaderColor(r, g, b, a);
	}

	public void drawStack(EmiIngredient stack, int x, int y) {
		stack.render(raw(), x, y, client.getRenderTickCounter().getTickDelta(false));
	}

	public void drawStack(EmiIngredient stack, int x, int y, int flags) {
		drawStack(stack, x, y, client.getRenderTickCounter().getTickDelta(false), flags);
	}

	public void drawStack(EmiIngredient stack, int x, int y, float delta, int flags) {
		stack.render(raw(), x, y, delta, flags);
	}
}
