package dev.emi.emi.screen.widget.config;

import net.minecraft.client.input.MouseButtonEvent;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import com.google.common.collect.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.widget.config.ListWidget.Entry;

public class GroupNameWidget extends Entry {
	protected static final Minecraft CLIENT = Minecraft.getInstance();
	public final String id;
	public final Component text;
	public final List<ConfigEntryWidget> children = Lists.newArrayList();
	public boolean collapsed = false;

	public GroupNameWidget(String id, Component text) {
		this.id = id;
		this.text = text;
	}

	@Override
	public void render(GuiGraphicsExtractor raw, int index, int y, int x, int width, int height, int mouseX, int mouseY,
			boolean hovered, float delta) {
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		context.drawCenteredTextWithShadow(text, x + width / 2, y + 3, -1);
		if (hovered || collapsed) {
			String collapse = "[-]";
			int cx = x + width / 2 - CLIENT.font.width(text) / 2 - 20;
			if (collapsed) {
				collapse = "[+]";
			}
			context.drawTextWithShadow(EmiPort.literal(collapse), cx, y + 3, -1);
		}
	}

	@Override
	public int getHeight() {
		for (ConfigEntryWidget w : children) {
			if (w.isVisible()) {
				return 20;
			}
		}
		return 0;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		double mouseX = event.x();
		double mouseY = event.y();
		int button = event.button();
		if (isMouseOver(mouseX, mouseY)) {
			collapsed = !collapsed;
			EmiPort.playClickSound();
			return true;
		}
		return false;
	}

	@Override
	public List<? extends GuiEventListener> children() {
		return List.of();
	}
}
