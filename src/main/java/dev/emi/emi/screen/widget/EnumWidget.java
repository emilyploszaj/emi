package dev.emi.emi.screen.widget;

import java.util.List;

import dev.emi.emi.EmiConfig;
import dev.emi.emi.screen.ConfigScreen.Mutator;
import dev.emi.emi.screen.widget.ListWidget.Entry;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class EnumWidget extends Entry {
	private final Text name;
	private final Mutator<EmiConfig.ConfigEnum> mutator;
	private ButtonWidget button;

	public EnumWidget(Text name, Mutator<EmiConfig.ConfigEnum> mutator) {
		this.name = name;
		this.mutator = mutator;

		button = new ButtonWidget(0, 0, 150, 20, getText(), button -> {
			mutator.set(mutator.get().next());
			button.setMessage(getText());
		});
	}

	public Text getText() {
		return mutator.get().getText();
	}

	@Override
	public void render(MatrixStack matrices, int index, int y, int x, int width, int height, int mouseX, int mouseY,
			boolean hovered, float delta) {
		DrawableHelper.fill(matrices, x, y, x + width, y + height, 0x66000000);
		parentList.client.textRenderer.drawWithShadow(matrices, this.name, x,
			y + 10 - parentList.client.textRenderer.fontHeight / 2, 0xFFFFFF);
		button.x = x + width - button.getWidth();
		button.y = y;
		button.render(matrices, mouseX, mouseY, delta);
	}

	@Override
	public int getHeight() {
		return 24;
	}
	
	@Override
	public List<? extends Element> children() {
		return List.of(button);
	}
}
