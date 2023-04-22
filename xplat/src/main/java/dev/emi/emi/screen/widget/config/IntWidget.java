package dev.emi.emi.screen.widget.config;

import java.util.List;
import java.util.function.Supplier;

import dev.emi.emi.screen.ConfigScreen.Mutator;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.text.Text;

public class IntWidget extends ConfigEntryWidget {
	public final IntEdit edit;

	public IntWidget(Text name, List<TooltipComponent> tooltip, Supplier<String> search, Mutator<Integer> mutator) {
		super(name, tooltip, search, 20);
		this.edit = new IntEdit(150, () -> mutator.get(), i -> mutator.set(i));
		this.setChildren(List.of(edit.text, edit.up, edit.down));
	}

	@Override
	public void update(int y, int x, int width, int height) {
		int right = x + width;
		edit.setPosition(right - 150, y);
	}
}
