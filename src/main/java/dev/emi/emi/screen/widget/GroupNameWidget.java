package dev.emi.emi.screen.widget;

import java.util.List;

import dev.emi.emi.EmiPort;
import dev.emi.emi.screen.widget.ListWidget.Entry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class GroupNameWidget extends Entry {
	protected static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	protected final Text text;

	public GroupNameWidget(Text text) {
		this.text = text;
	}

	@Override
	public void render(MatrixStack matrices, int index, int y, int x, int width, int height, int mouseX, int mouseY,
			boolean hovered, float delta) {
		DrawableHelper.drawCenteredTextWithShadow(matrices, CLIENT.textRenderer, EmiPort.ordered(text), x + width / 2, y + 3, -1);
	}

	@Override
	public int getHeight() {
		return 20;
	}

	@Override
	public List<? extends Element> children() {
		return List.of();
	}
}
