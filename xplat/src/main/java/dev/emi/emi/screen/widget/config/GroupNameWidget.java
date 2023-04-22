package dev.emi.emi.screen.widget.config;

import java.util.List;

import org.apache.commons.compress.utils.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.screen.widget.config.ListWidget.Entry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

public class GroupNameWidget extends Entry {
	protected static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	public final String id;
	public final Text text;
	public final List<ConfigEntryWidget> children = Lists.newArrayList();
	public boolean collapsed = false;

	public GroupNameWidget(String id, Text text) {
		this.id = id;
		this.text = text;
	}

	@Override
	public void render(MatrixStack matrices, int index, int y, int x, int width, int height, int mouseX, int mouseY,
			boolean hovered, float delta) {
		EmiPort.drawCenteredText(matrices, CLIENT.textRenderer, text, x + width / 2, y + 3, -1, true);
		if (hovered || collapsed) {
			String collapse = "[-]";
			int cx = x + width / 2 - CLIENT.textRenderer.getWidth(text) / 2 - 20;
			if (collapsed) {
				collapse = "[+]";
			}
			DrawableHelper.drawTextWithShadow(matrices, CLIENT.textRenderer, EmiPort.literal(collapse), cx, y + 3, -1);
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
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (isMouseOver(mouseX, mouseY)) {
			collapsed = !collapsed;
			MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
			return true;
		}
		return false;
	}

	@Override
	public List<? extends Element> children() {
		return List.of();
	}
}
