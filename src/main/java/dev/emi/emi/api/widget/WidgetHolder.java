package dev.emi.emi.api.widget;

import java.util.Random;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.DrawableWidget.DrawableWidgetConsumer;
import net.minecraft.text.OrderedText;
import net.minecraft.util.Identifier;

public interface WidgetHolder {

	int getWidth();

	int getHeight();

	<T extends Widget> T add(T widget);

	default SlotWidget addSlot(EmiIngredient ingredient, int x, int y) {
		return add(new SlotWidget(ingredient, x, y));
	}

	default SlotWidget addSlot(int x, int y) {
		return addSlot(EmiStack.EMPTY, x, y);
	}

	default TextureWidget addTexture(Identifier texture, int x, int y, int width, int height, int u, int v) {
		return add(new TextureWidget(texture, x, y, width, height, u, v));
	}

	default TextureWidget addTexture(Identifier texture, int x, int y, int width, int height, int u, int v,
			int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
		return add(new TextureWidget(texture, x, y, width, height, u, v,
			regionWidth, regionHeight, textureWidth, textureHeight));
	}

	default DrawableWidget addDrawable(int x, int y, int width, int height, DrawableWidgetConsumer consumer) {
		return add(new DrawableWidget(x, y, width, height, consumer));
	}

	default TextWidget addText(OrderedText text, int x, int y, int color, boolean shadow) {
		return add(new TextWidget(text, x, y, color, shadow));
	}

	default ButtonWidget addButton(int x, int y, int width, int height, int u, int v,
			BooleanSupplier isActive, ButtonWidget.ClickAction action) {
		return add(new ButtonWidget(x, y, width, height, u, v, isActive, action));
	}

	default FillingArrowWidget addFillingArrow(int x, int y, int time) {
		return add(new FillingArrowWidget(x, y, time));
	}

	default AnimatedTextureWidget addAnimatedTexture(Identifier texture, int x, int y, int width, int height, int u, int v, int time,
			boolean horizontal, boolean endToStart, boolean fullToEmpty) {
		return add(new AnimatedTextureWidget(texture, x, y, width, height, u, v,
			time, horizontal, endToStart, fullToEmpty));
	}

	default AnimatedTextureWidget addAnimatedTexture(Identifier texture, int x, int y, int width, int height, int u, int v,
			int regionWidth, int regionHeight, int textureWidth, int textureHeight, int time,
			boolean horizontal, boolean endToStart, boolean fullToEmpty) {
		return add(new AnimatedTextureWidget(texture, x, y, width, height, u, v,
			regionWidth, regionHeight, textureWidth, textureHeight,
			time, horizontal, endToStart, fullToEmpty));
	}

	default GeneratedSlotWidget addGeneratedSlot(Function<Random, EmiIngredient> stackSupplier, int unique, int x, int y) {
		return add(new GeneratedSlotWidget(stackSupplier, unique, x, y));
	}
}
