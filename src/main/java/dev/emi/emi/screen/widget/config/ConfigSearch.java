package dev.emi.emi.screen.widget.config;

import dev.emi.emi.EmiPort;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;

public class ConfigSearch {
	public final ConfigSearchWidgetField field;

	public ConfigSearch(int x, int y, int width, int height) {
		MinecraftClient client = MinecraftClient.getInstance();

		field = new ConfigSearchWidgetField(client.textRenderer, x, y, width, height, EmiPort.literal(""));
		field.setChangedListener(s -> {
			if (s.length() > 0) {
				field.setSuggestion("");
			} else {
				field.setSuggestion(I18n.translate("emi.search_config"));
			}
		});
		field.setSuggestion(I18n.translate("emi.search_config"));
	}

	public void setText(String query) {
		field.setText(query);
	}

	public String getSearch() {
		return field.getText();
	}
	
	private class ConfigSearchWidgetField extends TextFieldWidget {

		public ConfigSearchWidgetField(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
			super(textRenderer, x, y, width, height, text);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (button == 1 && isMouseOver(mouseX, mouseY)) {
				this.setText("");
				this.setTextFieldFocused(true);
				return true;
			}
			return super.mouseClicked(mouseX, mouseY, button);
		}
	}
}
