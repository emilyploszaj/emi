package dev.emi.emi.screen.widget.config;

import net.minecraft.client.input.MouseButtonInfo;

import net.minecraft.client.input.MouseButtonEvent;

import dev.emi.emi.EmiPort;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public class ConfigSearch {
	public final ConfigSearchWidgetField field;

	public ConfigSearch(int x, int y, int width, int height) {
		Minecraft client = Minecraft.getInstance();

		field = new ConfigSearchWidgetField(client.font, x, y, width, height, EmiPort.literal(""));
		field.setResponder(s -> {
			if (s.length() > 0) {
				field.setSuggestion("");
			} else {
				field.setSuggestion(I18n.get("emi.search_config"));
			}
		});
		field.setSuggestion(I18n.get("emi.search_config"));
	}

	public void setText(String query) {
		field.setValue(query);
	}

	public String getSearch() {
		return field.getValue();
	}
	
	private class ConfigSearchWidgetField extends EditBox {

		public ConfigSearchWidgetField(Font textRenderer, int x, int y, int width, int height, Component text) {
			super(textRenderer, x, y, width, height, text);
		}

		@Override
		public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		double mouseX = event.x();
		double mouseY = event.y();
		int button = event.button();
			if (button == 1 && isMouseOver(mouseX, mouseY)) {
				this.setValue("");
				EmiPort.focus(this, true);
				return true;
			}
			return super.mouseClicked(new MouseButtonEvent(mouseX, mouseY, new MouseButtonInfo(button, 0)), false);
		}
	}
}
