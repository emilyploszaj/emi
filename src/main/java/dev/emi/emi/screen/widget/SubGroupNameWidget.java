package dev.emi.emi.screen.widget;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class SubGroupNameWidget extends GroupNameWidget {

	public SubGroupNameWidget(Text text) {
		super(text);
	}

	@Override
	public void render(MatrixStack matrices, int index, int y, int x, int width, int height, int mouseX, int mouseY,
			boolean hovered, float delta) {
		DrawableHelper.drawTextWithShadow(matrices, CLIENT.textRenderer, text, x + 10, y + 3, -1);
	}
}
