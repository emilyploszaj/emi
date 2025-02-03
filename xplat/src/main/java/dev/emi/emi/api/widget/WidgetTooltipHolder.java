package dev.emi.emi.api.widget;

import java.util.List;
import java.util.function.BiFunction;

import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.text.Text;

public interface WidgetTooltipHolder<T> {
	
	T tooltip(BiFunction<Integer, Integer, List<TooltipComponent>> tooltipSupplier);

	default T tooltip(List<TooltipComponent> tooltip) {
		return tooltip((mx, my) -> tooltip);
	}

	default T tooltipText(BiFunction<Integer, Integer, List<Text>> tooltipSupplier) {
		return tooltip((x, y) -> tooltipSupplier.apply(x, y).stream().map(Text::asOrderedText).map(TooltipComponent::of).toList());
	}

	default T tooltipText(List<Text> tooltip) {
		return tooltip(tooltip.stream().map(Text::asOrderedText).map(TooltipComponent::of).toList());
	}
}
