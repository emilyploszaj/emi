package dev.emi.emi.screen.widget.config;

import dev.emi.emi.EmiPort;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class SubGroupNameWidget extends GroupNameWidget {
	public GroupNameWidget parent;

	public SubGroupNameWidget(String id, Text text) {
		super(id, text);
	}

	@Override
	public void render(MatrixStack matrices, int index, int y, int x, int width, int height, int mouseX, int mouseY,
			boolean hovered, float delta) {
		DrawableHelper.drawTextWithShadow(matrices, CLIENT.textRenderer, text, x + 20, y + 3, -1);
		if (hovered || collapsed) {
			String collapse = "[-]";
			int cx = x;
			if (collapsed) {
				collapse = "[+]";
			}
			DrawableHelper.drawTextWithShadow(matrices, CLIENT.textRenderer, EmiPort.literal(collapse), cx, y + 3, -1);
		}
	}

	@Override
	public int getHeight() {
		if (parent.collapsed) {
			return 0;
		}
		return super.getHeight();
	}
}
