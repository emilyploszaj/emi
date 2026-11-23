package dev.emi.emi.screen.widget.config;

import dev.emi.emi.EmiPort;
import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class SubGroupNameWidget extends GroupNameWidget {
	public GroupNameWidget parent;

	public SubGroupNameWidget(String id, Component text) {
		super(id, text);
	}

	@Override
	public void render(GuiGraphicsExtractor raw, int index, int y, int x, int width, int height, int mouseX, int mouseY,
			boolean hovered, float delta) {
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		context.drawTextWithShadow(text, x + 20, y + 3);
		if (hovered || collapsed) {
			String collapse = "[-]";
			int cx = x;
			if (collapsed) {
				collapse = "[+]";
			}
			context.drawTextWithShadow(EmiPort.literal(collapse), cx, y + 3);
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
