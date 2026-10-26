package dev.emi.emi.screen.widget.config;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import dev.emi.emi.EmiPort;
import dev.emi.emi.config.ConfigEnum;
import dev.emi.emi.screen.ConfigEnumScreen;
import dev.emi.emi.screen.ConfigScreen;
import dev.emi.emi.screen.ConfigScreen.Mutator;

public class EnumWidget extends ConfigEntryWidget {
	private final Mutator<ConfigEnum> mutator;
	private Button button;

	public EnumWidget(Component name, List<ClientTooltipComponent> tooltip, Supplier<String> search, Mutator<ConfigEnum> mutator, Predicate<ConfigEnum> filter) {
		super(name, tooltip, search, 20);
		this.mutator = mutator;

		button = EmiPort.newButton(0, 0, 150, 20, getText(), button -> {
			page(mutator.get(), filter, mutator::set);
		});
		this.setChildren(List.of(button));
	}

	public static void page(ConfigEnum original, Predicate<ConfigEnum> filter, Consumer<ConfigEnum> consumer) {
		Enum<?> e = (Enum<?>) original;
		Enum<?>[] values =  e.getClass().getEnumConstants();
		Minecraft client = Minecraft.getInstance();
		if (client.screen instanceof ConfigScreen cs) {
			client.setScreen(new ConfigEnumScreen<ConfigEnum>(cs, Stream.of(values).filter(f -> filter.test((ConfigEnum) f)).map(v -> {
				ConfigEnum en = (ConfigEnum) v;
				return new ConfigEnumScreen.Entry<ConfigEnum>(en, en.getText(), List.of());
			}).toList(), consumer));
		}
	}

	public Component getText() {
		return mutator.get().getText();
	}

	@Override
	public void update(int y, int x, int width, int height) {
		button.x = x + width - button.getWidth();
		button.y = y;
	}
}
