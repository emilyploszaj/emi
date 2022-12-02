package dev.emi.emi.screen.widget.config;

import java.util.List;
import java.util.function.Supplier;

import dev.emi.emi.EmiPort;
import dev.emi.emi.screen.ConfigScreen.Mutator;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class BooleanWidget extends ConfigEntryWidget {
	private final Mutator<Boolean> mutator;
	private ButtonWidget button;

	public BooleanWidget(Text name, List<TooltipComponent> tooltip, Supplier<String> search, Mutator<Boolean> mutator) {
		super(name, tooltip, search, 20);
		this.mutator = mutator;

		button = EmiPort.newButton(0, 0, 150, 20, getText(), button -> {
			mutator.set(!mutator.get());
			button.setMessage(getText());
		});
		this.setChildren(List.of(button));
	}

	public Text getText() {
		if (mutator.get()) {
			return EmiPort.literal("true", Formatting.GREEN);
		} else {
			return EmiPort.literal("false", Formatting.RED);
		}
	}

	@Override
	public void update(int y, int x, int width, int height) {
		button.x = x + width - button.getWidth();
		button.y = y;
	}
}
