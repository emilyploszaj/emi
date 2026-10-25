package dev.emi.emi.screen.widget.config;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import dev.emi.emi.EmiPort;
import dev.emi.emi.screen.ConfigScreen.Mutator;

public class BooleanWidget extends ConfigEntryWidget {
	private final Mutator<Boolean> mutator;
	private Button button;

	public BooleanWidget(Component name, List<ClientTooltipComponent> tooltip, Supplier<String> search, Mutator<Boolean> mutator) {
		super(name, tooltip, search, 20);
		this.mutator = mutator;

		button = EmiPort.newButton(0, 0, 150, 20, getText(), button -> {
			mutator.set(!mutator.get());
			button.setMessage(getText());
		});
		this.setChildren(List.of(button));
	}

	public Component getText() {
		if (mutator.get()) {
			return EmiPort.literal("true", ChatFormatting.GREEN);
		} else {
			return EmiPort.literal("false", ChatFormatting.RED);
		}
	}

	@Override
	public void update(int y, int x, int width, int height) {
		button.x = x + width - button.getWidth();
		button.y = y;
	}
}
