package dev.emi.emi.jemi.impl.extras;

import dev.emi.emi.api.widget.WidgetHolder;

public class JemiWidgetBuilder extends JemiPlaceable<JemiWidgetBuilder> {
	public WidgetConstructor constructor;

	public JemiWidgetBuilder(int width, int height, WidgetConstructor constructor) {
		super(width, height);
		this.constructor = constructor;
	}

	public void addWidgets(WidgetHolder holder) {
		constructor.accept(this, holder);
	}
	
	public static interface WidgetConstructor {

		void accept(JemiWidgetBuilder builder, WidgetHolder holder);
	}
}
