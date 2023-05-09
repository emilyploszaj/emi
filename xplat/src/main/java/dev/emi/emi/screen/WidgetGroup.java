package dev.emi.emi.screen;

import java.util.List;

import com.google.common.collect.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.widget.DrawableWidget;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.TextWidget.Alignment;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.widget.RecipeBackground;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.util.Formatting;

public class WidgetGroup implements WidgetHolder {
	public final EmiRecipe recipe;
	public final int x, y, width, height;
	public final List<Widget> widgets = Lists.newArrayList();

	public WidgetGroup(EmiRecipe recipe, int x, int y, int width, int height) {
		this.recipe = recipe;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		if (recipe != null) {
			widgets.add(new RecipeBackground(-4, -4, width + 8, height + 8));
		}
	}

	public void error(Throwable e) {
		widgets.clear();
		widgets.add(new RecipeBackground(-4, -4, width + 8, height + 8));
		widgets.add(new TextWidget(EmiPort.ordered(EmiPort.translatable("emi.error.recipe.render")),
			width / 2, height / 2 - 5, Formatting.RED.getColorValue(), true).horizontalAlign(Alignment.CENTER));
		widgets.add(new DrawableWidget(0, 0, width, height, (matrices, mouseX, mouseY, delta) -> {})
			.tooltip((i, j) -> EmiUtil.getStackTrace(e).stream()
				.map(EmiPort::literal).map(EmiPort::ordered).map(TooltipComponent::of).toList()));
	}

	public int x() {
		return x;
	}

	public int y() {
		return y;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public <T extends Widget> T add(T widget) {
		widgets.add(widget);
		return widget;
	}
}
