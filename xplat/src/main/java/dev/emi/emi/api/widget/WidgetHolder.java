package dev.emi.emi.api.widget;

import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.DrawableWidget.DrawableWidgetConsumer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
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

	default TextureWidget addTexture(EmiTexture texture, int x, int y) {
		return addTexture(texture.texture, x, y, texture.width, texture.height, texture.u, texture.v,
			texture.regionWidth, texture.regionHeight, texture.textureWidth, texture.textureHeight);
	}

	default DrawableWidget addDrawable(int x, int y, int width, int height, DrawableWidgetConsumer consumer) {
		return add(new DrawableWidget(x, y, width, height, consumer));
	}

	default TextWidget addText(Text text, int x, int y, int color, boolean shadow) {
		return addText(text.asOrderedText(), x, y, color, shadow);
	}

	default TextWidget addText(OrderedText text, int x, int y, int color, boolean shadow) {
		return add(new TextWidget(text, x, y, color, shadow));
	}

	default ButtonWidget addButton(int x, int y, int width, int height, int u, int v,
			BooleanSupplier isActive, ButtonWidget.ClickAction action) {
		return add(new ButtonWidget(x, y, width, height, u, v, isActive, action));
	}

	default ButtonWidget addButton(int x, int y, int width, int height, int u, int v,
			Identifier texture, BooleanSupplier isActive, ButtonWidget.ClickAction action) {
		return add(new ButtonWidget(x, y, width, height, u, v, texture, isActive, action));
	}

	default TooltipWidget addTooltip(BiFunction<Integer, Integer, List<TooltipComponent>> tooltipSupplier, int x, int y, int width, int height) {
		return new TooltipWidget(tooltipSupplier, x, y, width, height);
	}

	default TooltipWidget addTooltip(List<TooltipComponent> tooltip, int x, int y, int width, int height) {
		return addTooltip((mx, my) -> tooltip, x, y, width, height);
	}

	default TooltipWidget addTooltipText(List<Text> tooltip, int x, int y, int width, int height) {
		return addTooltip(tooltip.stream().map(Text::asOrderedText).map(TooltipComponent::of).toList(), x, y, width, height);
	}

	/**
	 * @param time Filling time, in milliseconds
	 */
	default FillingArrowWidget addFillingArrow(int x, int y, int time) {
		return add(new FillingArrowWidget(x, y, time));
	}

	/**
	 * @param time Animation time, in milliseconds
	 */
	default AnimatedTextureWidget addAnimatedTexture(Identifier texture, int x, int y, int width, int height, int u, int v, int time,
			boolean horizontal, boolean endToStart, boolean fullToEmpty) {
		return add(new AnimatedTextureWidget(texture, x, y, width, height, u, v,
			time, horizontal, endToStart, fullToEmpty));
	}

	/**
	 * @param time Animation time, in milliseconds
	 */
	default AnimatedTextureWidget addAnimatedTexture(Identifier texture, int x, int y, int width, int height, int u, int v,
			int regionWidth, int regionHeight, int textureWidth, int textureHeight, int time,
			boolean horizontal, boolean endToStart, boolean fullToEmpty) {
		return add(new AnimatedTextureWidget(texture, x, y, width, height, u, v,
			regionWidth, regionHeight, textureWidth, textureHeight,
			time, horizontal, endToStart, fullToEmpty));
	}

	/**
	 * @param time Animation time, in milliseconds
	 */
	default AnimatedTextureWidget addAnimatedTexture(EmiTexture texture, int x, int y, int time,
			boolean horizontal, boolean endToStart, boolean fullToEmpty) {
		return addAnimatedTexture(texture.texture, x, y, texture.width, texture.height, texture.u, texture.v,
			texture.regionWidth, texture.regionHeight, texture.textureWidth, texture.textureHeight,
			time, horizontal, endToStart, fullToEmpty);
	}

	default GeneratedSlotWidget addGeneratedSlot(Function<Random, EmiIngredient> stackSupplier, int unique, int x, int y) {
		return add(new GeneratedSlotWidget(stackSupplier, unique, x, y));
	}
}
