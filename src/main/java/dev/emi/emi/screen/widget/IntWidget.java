package dev.emi.emi.screen.widget;

import java.util.List;
import java.util.regex.Pattern;

import dev.emi.emi.screen.ConfigScreen.Mutator;
import dev.emi.emi.screen.widget.ListWidget.Entry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class IntWidget extends Entry {
	private static final Pattern NUMBER = Pattern.compile("^-?[0-9]+$");
	private final Text name;
	private final Drawable tooltip;
	private TextFieldWidget text;
	private ButtonWidget up, down;

	public IntWidget(Text name, Drawable tooltip, Mutator<Integer> mutator) {
		this.name = name;
		this.tooltip = tooltip;
		MinecraftClient client = MinecraftClient.getInstance();
		text = new TextFieldWidget(client.textRenderer, 0, 0, 136, 18, new LiteralText(""));
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
	}

	@Override
	public void render(MatrixStack matrices, int index, int y, int x, int width, int height, int mouseX, int mouseY,
			boolean hovered, float delta) {
		DrawableHelper.fill(matrices, x, y, x + width, y + height, 0x66000000);
		parentList.client.textRenderer.drawWithShadow(matrices, this.name, x,
			y + 10 - parentList.client.textRenderer.fontHeight / 2, 0xFFFFFF);
		int right = x + width;
		text.x = right - 149;
		text.y = y + 1;
		up.x = right - 12;
		up.y = y;
		down.x = right - 12;
		down.y = y + 10;
		text.render(matrices, mouseX, mouseY, delta);
		up.render(matrices, mouseX, mouseY, delta);
		down.render(matrices, mouseX, mouseY, delta);
	}

	@Override
	public void renderTooltip(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		tooltip.render(matrices, mouseX, mouseY, delta);
	}

	@Override
	public int getHeight() {
		return 24;
	}
	
	@Override
	public List<? extends Element> children() {
		return List.of(text, up, down);
	}
}
