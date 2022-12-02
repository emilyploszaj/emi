package dev.emi.emi.screen.widget.config;

import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.config.SidebarPages;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.screen.ConfigScreen.Mutator;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class SidebarPagesWidget extends ConfigEntryWidget {
	private List<ButtonWidget> buttons = Lists.newArrayList();
	private Mutator<SidebarPages> mutator;

	public SidebarPagesWidget(Text name, List<TooltipComponent> tooltip, Supplier<String> search, Mutator<SidebarPages> mutator) {
		super(name, tooltip, search, 0);
		this.mutator = mutator;
		setChildren(buttons);
		updateButtons();
	}

	public void updateButtons() {
		buttons.clear();
		SidebarPages pages = mutator.get();
		boolean canChess = pages.showChess.getAsBoolean();
		for (int i = 0; i < pages.pages.size(); i++) {
			final int j = i;
			SidebarPages.SidebarPage page = pages.pages.get(i);
			buttons.add(EmiPort.newButton(0, 0, 150, 20, page.type.getText(), b -> {
				EnumWidget.page(page.type, t -> canChess || t != SidebarType.CHESS, t -> {
					pages.pages.get(j).type = (SidebarType) t;
					pages.unique();
				});
			}));
		}
		buttons.add(EmiPort.newButton(0, 0, 20, 20, EmiPort.literal("+"), b -> {
			EnumWidget.page(SidebarType.INDEX, t -> canChess || t != SidebarType.CHESS, t -> {
				pages.pages.add(new SidebarPages.SidebarPage((SidebarType) t));
				pages.unique();
			});
		}));
	}

	@Override
	public void update(int y, int x, int width, int height) {
		int h = 0;
		for (int i = 0; i < buttons.size() - 1; i++) {
			ButtonWidget button = buttons.get(i);
			button.x = x + width - 174;
			button.y = y + h;
			h += 24;
		}
		ButtonWidget button = buttons.get(buttons.size() - 1);
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
