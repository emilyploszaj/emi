package dev.emi.emi.jemi.impl.extras;

import java.util.Collection;
import java.util.List;

import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.jemi.impl.JemiTooltipBuilder;
import mezz.jei.api.gui.widgets.IDrawableWidget;
import mezz.jei.api.gui.widgets.IRecipeWidgetTooltipCallback;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class JemiWidgetBuilder extends JemiPlaceable<IDrawableWidget> implements IDrawableWidget {
	public WidgetConstructor constructor;
	public IRecipeWidgetTooltipCallback tooltip;

	public JemiWidgetBuilder(int width, int height, WidgetConstructor constructor) {
		super(width, height);
		this.constructor = constructor;
	}

	public void addWidgets(WidgetHolder holder) {
		constructor.accept(this, holder);
		if (tooltip != null) {
			IRecipeWidgetTooltipCallback callback = tooltip;
			holder.addTooltip((mx, my) -> {
				JemiTooltipBuilder builder = new JemiTooltipBuilder();
				callback.onTooltip(builder);
				return builder.getTooltip();
			}, x, y, width, height);
		}
	}

	@Override
	public IDrawableWidget setTooltip(FormattedText tooltip) {
		return setTooltip(List.of(tooltip));
	}

	@Override
	public IDrawableWidget setTooltip(Collection<? extends FormattedText> tooltip) {
		return setTooltip(builder -> builder.addAll(tooltip));
	}

	@Override
	public IDrawableWidget setTooltip(TooltipComponent tooltip) {
		return setTooltip(builder -> builder.add(tooltip));
	}

	@Override
	public IDrawableWidget setTooltip(IRecipeWidgetTooltipCallback tooltipCallback) {
		this.tooltip = tooltipCallback;
		return this;
	}

	public static interface WidgetConstructor {

		void accept(JemiWidgetBuilder builder, WidgetHolder holder);
	}
}
