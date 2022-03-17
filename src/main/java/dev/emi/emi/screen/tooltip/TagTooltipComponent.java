package dev.emi.emi.screen.tooltip;

import java.util.List;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.Tag;

public class TagTooltipComponent implements TooltipComponent {
	private final List<Item> items;

	public TagTooltipComponent(Tag<Item> tag) {
		items = tag.values();
	}

	@Override
	public int getHeight() {
		return ((items.size() - 1) / 4 + 1) * 18;
	}

	@Override
	public int getWidth(TextRenderer textRenderer) {
		return 18 * 4;
	}
	
	@Override
	public void drawItems(TextRenderer textRenderer, int x, int y, MatrixStack matrices, ItemRenderer itemRenderer, int z) {
		for (int i = 0; i < items.size(); i++) {
			itemRenderer.renderGuiItemIcon(new ItemStack(items.get(i)), x + i % 4 * 18, y + i / 4 * 18);
		}
	}
}
