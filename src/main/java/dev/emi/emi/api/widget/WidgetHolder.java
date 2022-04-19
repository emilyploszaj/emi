package dev.emi.emi.api.widget;

import java.util.Random;
import java.util.function.Function;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.text.OrderedText;
import net.minecraft.util.Identifier;

public interface WidgetHolder {
	
	int getX();

	int getY();

	int getWidth();

	int getHeight();

	<T extends Widget> T add(T widget);

	default SlotWidget addSlot(EmiIngredient ingredient, int x, int y) {
		return add(new SlotWidget(ingredient, x + getX(), y + getY()));
	}

	default SlotWidget addSlot(int x, int y) {
		return addSlot(EmiStack.EMPTY, x, y);
	}

	default TextureWidget addTexture(Identifier texture, int x, int y, int width, int height, int u, int v) {
		return add(new TextureWidget(texture, x + getX(), y + getY(), width, height, u, v));
	}

	default TextureWidget addTexture(Identifier texture, int x, int y, int width, int height, int u, int v,
			int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
		return add(new TextureWidget(texture, x + getX(), y + getY(), width, height, u, v,
			regionWidth, regionHeight, textureWidth, textureHeight));
	}

	default TextWidget addText(OrderedText text, int x, int y, int color, boolean shadow) {
		return add(new TextWidget(text, x + getX(), y + getY(), color, shadow));
	}

	default FillingArrowWidget addFillingArrow(int x, int y, int time) {
		return add(new FillingArrowWidget(x + getX(), y + getY(), time));
	}

	default AnimatedTextureWidget addAnimatedTexture(Identifier texture, int x, int y, int width, int height, int u, int v, int time,
			boolean horizontal, boolean endToStart, boolean fullToEmpty) {
		return add(new AnimatedTextureWidget(texture, x + getX(), y + getY(), width, height, u, v,
			time, horizontal, endToStart, fullToEmpty));
	}

	default AnimatedTextureWidget addAnimatedTexture(Identifier texture, int x, int y, int width, int height, int u, int v,
			int regionWidth, int regionHeight, int textureWidth, int textureHeight, int time,
			boolean horizontal, boolean endToStart, boolean fullToEmpty) {
		return add(new AnimatedTextureWidget(texture, x + getX(), y + getY(), width, height, u, v,
			regionWidth, regionHeight, textureWidth, textureHeight,
			time, horizontal, endToStart, fullToEmpty));
	}

	default GeneratedSlotWidget addGeneratedSlot(Function<Random, EmiIngredient> stackSupplier, int unique, int x, int y) {
		return add(new GeneratedSlotWidget(stackSupplier, unique, x + getX(), y + getY()));
	}
}
