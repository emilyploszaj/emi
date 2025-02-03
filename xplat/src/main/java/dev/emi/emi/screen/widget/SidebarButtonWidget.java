package dev.emi.emi.screen.widget;

import java.util.List;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.input.EmiInput;
import dev.emi.emi.screen.EmiScreenManager.SidebarPanel;

public class SidebarButtonWidget extends SizedButtonWidget {
	private final SidebarPanel panel;

	public SidebarButtonWidget(int x, int y, int width, int height, SidebarPanel panel) {
		super(x, y, width, height, 0, 0, () -> {
			return panel.pages.pages.size() > 0;
		}, null, () -> 0, () -> {
			return List.of(panel.getType().getText());
		});
		this.panel = panel;
		texture = EmiRenderHelper.WIDGETS;
	}

	@Override
	public void onPress() {
		panel.cycleType(EmiInput.isShiftDown() ? -1 : 1);
	}

	@Override
	protected int getU(int mouseX, int mouseY) {
		return panel.getType().u;
	}

	@Override
	protected int getV(int mouseX, int mouseY) {
		this.v = panel.getType().v;
		if (!this.active) {
			return super.getV(mouseX, mouseY) - height * 2;
		}
		return super.getV(mouseX, mouseY);
	}
}
