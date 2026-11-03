package dev.emi.emi.screen.widget.config;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import com.google.common.collect.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.config.SidebarSubpanels;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.screen.ConfigScreen.Mutator;

public class SidebarSubpanelsWidget extends ConfigEntryWidget {
	private List<Button> buttons = Lists.newArrayList();
	private List<AbstractWidget> raws = Lists.newArrayList();
	private List<IntEdit> rows = Lists.newArrayList();
	private Mutator<SidebarSubpanels> mutator;

	public SidebarSubpanelsWidget(Component name, List<ClientTooltipComponent> tooltip, Supplier<String> search, Mutator<SidebarSubpanels> mutator) {
		super(name, tooltip, search, 0);
		this.mutator = mutator;
		setChildren(raws);
		updateButtons();
	}

	public void updateButtons() {
		raws.clear();
		buttons.clear();
		SidebarSubpanels pages = mutator.get();
		for (int i = 0; i < pages.subpanels.size(); i++) {
			final int j = i;
			SidebarSubpanels.Subpanel page = pages.subpanels.get(i);
			buttons.add(EmiPort.newButton(0, 0, 150, 20, page.type.getText(), b -> {
				EnumWidget.page(page.type, t -> t != SidebarType.CHESS, t -> {
					pages.subpanels.get(j).type = (SidebarType) t;
					pages.unique();
				});
			}));
			IntEdit edit = new IntEdit(40, () -> page.rows(), (ni) -> page.rows = Math.max(1, ni));
			rows.add(edit);
			raws.add(edit.up);
			raws.add(edit.down);
			raws.add(edit.text);
		}
		buttons.add(EmiPort.newButton(0, 0, 20, 20, EmiPort.literal("+"), b -> {
			EnumWidget.page(SidebarType.INDEX, t -> t != SidebarType.CHESS, t -> {
				pages.subpanels.add(new SidebarSubpanels.Subpanel((SidebarType) t, 1));
				pages.unique();
			});
		}));
		raws.addAll(buttons);
	}

	@Override
	public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
		for (IntEdit edit : rows) {
			if (edit.contains(mouseX, mouseY)) {
				return List.of(ClientTooltipComponent.create(EmiPort.ordered(
					EmiPort.translatable("emi.sidebar.size.rows"))));
			}
		}
		return super.getTooltip(mouseX, mouseY);
	}

	@Override
	public void update(int y, int x, int width, int height) {
		int h = 0;
		for (int i = 0; i < buttons.size() - 1; i++) {
			Button button = buttons.get(i);
			button.x = x + width - 174;
			button.y = y + h;
			h += 24;
		}
		h = 0;
		for (int i = 0; i < rows.size(); i++) {
			IntEdit edit = rows.get(i);
			edit.setPosition(x + width - 218, y + h);
			h += 24;
		}
		Button button = buttons.get(buttons.size() - 1);
		button.x = x + width - 20;
		button.y = y;
	}

	@Override
	public int getHeight() {
		if (!isVisible() || !isParentVisible()) {
			return 0;
		}
		if (buttons.size() == 1) {
			return 20;
		}
		return buttons.size() * 24 - 28;
	}
}
