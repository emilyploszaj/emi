package dev.emi.emi.screen.widget.config;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.regex.Pattern;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.screen.widget.SizedButtonWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;

public class IntEdit {
	private static final Pattern NUMBER = Pattern.compile("^-?[0-9]*$");
	public final TextFieldWidget text;
	public final ButtonWidget up, down;
	
	public IntEdit(int width, IntSupplier getter, IntConsumer setter) {
		MinecraftClient client = MinecraftClient.getInstance();
		text = new TextFieldWidget(client.textRenderer, 0, 0, width - 14, 18, EmiPort.literal(""));
		text.setText("" + getter.getAsInt());
		text.setChangedListener(string -> {
			try {
				if (string.isBlank()) {
					setter.accept(0);
				} else {
					setter.accept(Integer.parseInt(string));
				}
			} catch (Exception e) {
			}
		});
		text.setTextPredicate(s -> {
			return NUMBER.matcher(s).matches();
		});

		up = new SizedButtonWidget(150, 0, 12, 10, 232, 112, () -> true, button -> {
			setter.accept(getter.getAsInt() + getInc());
			text.setText("" + getter.getAsInt());
		});
		down = new SizedButtonWidget(150, 10, 12, 10, 244, 112, () -> true, button -> {
			setter.accept(getter.getAsInt() - getInc());
			text.setText("" + getter.getAsInt());
		});
	}

	public boolean contains(int x, int y) {
		return x > text.x && x < up.x + up.getWidth() && y > text.y && y < text.y + text.getHeight();
	}

	public int getInc() {
		if (EmiUtil.isShiftDown()) {
			return 10;
		} else if (EmiUtil.isControlDown()) {
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
