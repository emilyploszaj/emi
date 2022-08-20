package dev.emi.emi.screen.widget;

import java.util.List;

import dev.emi.emi.EmiConfig;
import dev.emi.emi.screen.ConfigScreen.Mutator;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class EnumWidget extends ConfigEntryWidget {
	private final Mutator<EmiConfig.ConfigEnum> mutator;
	private ButtonWidget button;

	public EnumWidget(Text name, Drawable tooltip, Mutator<EmiConfig.ConfigEnum> mutator) {
		super(name, tooltip, 24);
		this.mutator = mutator;

		button = new ButtonWidget(0, 0, 150, 20, getText(), button -> {
			mutator.set(mutator.get().next());
			button.setMessage(getText());
		});
		this.setChildren(List.of(button));
	}

	public Text getText() {
		return mutator.get().getText();
	}

	@Override
	public void update(int y, int x, int width, int height) {
		button.x = x + width - button.getWidth();
		button.y = y;
	}
}
