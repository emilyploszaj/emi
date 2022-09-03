package dev.emi.emi.screen.widget;

import java.util.List;

import dev.emi.emi.EmiConfig.ConfigGroup;
import dev.emi.emi.screen.widget.ListWidget.Entry;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public abstract class ConfigEntryWidget extends Entry {
	private final Text name;
	private final Drawable tooltip;
	private final int height;
	public ConfigGroup group;
	private List<? extends Element> children = List.of();
	
	public ConfigEntryWidget(Text name, Drawable tooltip, int height) {
		this.name = name;
		this.tooltip = tooltip;
		this.height = height;
	}

	public void setChildren(List<? extends Element> children) {
		this.children = children;
	}

	public void update(int y, int x, int width, int height) {
	}

	@Override
	public void render(MatrixStack matrices, int index, int y, int x, int width, int height, int mouseX, int mouseY,
			boolean hovered, float delta) {
		if (group != null) {
			x += 10;
			width -= 10;
		}
		update(y, x, width, height);
		DrawableHelper.fill(matrices, x, y, x + width, y + height, 0x66000000);
		parentList.client.textRenderer.drawWithShadow(matrices, this.name, x + 6,
			y + 10 - parentList.client.textRenderer.fontHeight / 2, 0xFFFFFF);
		for (Element element : children()) {
			if (element instanceof Drawable drawable) {
				drawable.render(matrices, mouseX, mouseY, delta);
			}
		}
	}

	@Override
	public void renderTooltip(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		tooltip.render(matrices, mouseX, mouseY, delta);
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public List<? extends Element> children() {
		return children;
	}
}
