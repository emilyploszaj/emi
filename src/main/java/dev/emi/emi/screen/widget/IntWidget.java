package dev.emi.emi.screen.widget;

import java.util.List;
import java.util.regex.Pattern;

import dev.emi.emi.EmiPort;
import dev.emi.emi.screen.ConfigScreen.Mutator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class IntWidget extends ConfigEntryWidget {
	private static final Pattern NUMBER = Pattern.compile("^-?[0-9]*$");
	private TextFieldWidget text;
	private ButtonWidget up, down;

	public IntWidget(Text name, Drawable tooltip, Mutator<Integer> mutator) {
		super(name, tooltip, 24);
		MinecraftClient client = MinecraftClient.getInstance();
		text = new TextFieldWidget(client.textRenderer, 0, 0, 136, 18, EmiPort.literal(""));
		text.setText("" + mutator.get());
		text.setChangedListener(string -> {
			try {
				mutator.set(Integer.parseInt(string));
			} catch (Exception e) {
			}
		});
		text.setTextPredicate(s -> {
			return NUMBER.matcher(s).matches();
		});

		up = new SizedButtonWidget(150, 0, 12, 10, 232, 112, () -> true, button -> {
			mutator.set(mutator.get() + 1);
			text.setText("" + mutator.get());
		});
		down = new SizedButtonWidget(150, 10, 12, 10, 244, 112, () -> true, button -> {
			mutator.set(mutator.get() - 1);
			text.setText("" + mutator.get());
		});
		this.setChildren(List.of(text, up, down));
	}

	@Override
	public void update(int y, int x, int width, int height) {
		int right = x + width;
		text.x = right - 149;
		text.y = y + 1;
		up.x = right - 12;
		up.y = y;
		down.x = right - 12;
		down.y = y + 10;
	}
}
