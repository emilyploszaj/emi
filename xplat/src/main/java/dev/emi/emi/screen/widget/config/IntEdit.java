package dev.emi.emi.screen.widget.config;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import dev.emi.emi.EmiPort;
import dev.emi.emi.input.EmiInput;
import dev.emi.emi.screen.widget.SizedButtonWidget;

public class IntEdit {
	private static final Pattern NUMBER = Pattern.compile("^-?[0-9]*$");
	public final EditBox text;
	public final Button up, down;
	
	public IntEdit(int width, IntSupplier getter, IntConsumer setter) {
		Minecraft client = Minecraft.getInstance();
		text = new EditBox(client.font, 0, 0, width - 14, 18, EmiPort.literal(""));
		text.setValue("" + getter.getAsInt());
		text.setResponder(string -> {
			try {
				if (string.isBlank()) {
					setter.accept(0);
				} else {
					setter.accept(Integer.parseInt(string));
				}
			} catch (Exception e) {
			}
		});
		text.setFilter(s -> {
			return NUMBER.matcher(s).matches();
		});

		up = new SizedButtonWidget(150, 0, 12, 10, 232, 48, () -> true, button -> {
			setter.accept(getter.getAsInt() + getInc());
			text.setValue("" + getter.getAsInt());
		});
		down = new SizedButtonWidget(150, 10, 12, 10, 244, 48, () -> true, button -> {
			setter.accept(getter.getAsInt() - getInc());
			text.setValue("" + getter.getAsInt());
		});
	}

	public boolean contains(int x, int y) {
		return x > text.x && x < up.x + up.getWidth() && y > text.y && y < text.y + text.getHeight();
	}

	public int getInc() {
		if (EmiInput.isShiftDown()) {
			return 10;
		} else if (EmiInput.isControlDown()) {
			return 5;
		}
		return 1;
	}

	public void setPosition(int x, int y) {
		text.x = x + 1;
		text.y = y + 1;
		up.x = x + text.getWidth() + 2;
		up.y = y;
		down.x = up.x;
		down.y = y + 10;
	}
}
