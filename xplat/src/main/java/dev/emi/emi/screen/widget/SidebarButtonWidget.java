package dev.emi.emi.screen.widget;

import java.util.List;

import com.google.common.collect.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.input.EmiBind;
import dev.emi.emi.input.EmiInput;
import dev.emi.emi.screen.EmiScreenManager.SidebarPanel;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class SidebarButtonWidget extends SizedButtonWidget {
	private final SidebarPanel panel;

	public SidebarButtonWidget(int x, int y, int width, int height, SidebarPanel panel) {
		super(x, y, width, height, 0, 0, () -> {
			return panel.pages.pages.size() > 0;
		}, null, () -> 0, () -> {
			List<Text> list = Lists.newArrayList();
			list.add(panel.getType().getText());
			list.add(panel.getType().getDescription());
			if (panel.getType() == SidebarType.FAVORITES && EmiConfig.favorite.isBound()) {
				list.add(EmiPort.translatable("emi.sidebar.favorite_stack", EmiConfig.favorite.getBindText()).formatted(Formatting.GRAY));
			}
			if (panel.pages.pages.size() > 1) {
				list.add(EmiPort.translatable("emi.sidebar.cycle", EmiBind.LEFT_CLICK.getBindText()).formatted(Formatting.GRAY));
			}
			return list;
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
