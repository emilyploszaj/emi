package dev.emi.emi.screen.widget.config;

import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.Lists;

import dev.emi.emi.config.EmiConfig.ConfigGroup;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.widget.config.ListWidget.Entry;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.text.Text;

public abstract class ConfigEntryWidget extends Entry {
	private final Text name;
	private final List<TooltipComponent> tooltip;
	protected final Supplier<String> search;
	private final int height;
	public ConfigGroup group;
	public boolean endGroup = false;
	private List<? extends Element> children = List.of();
	public List<GroupNameWidget> parentGroups = Lists.newArrayList();
	
	public ConfigEntryWidget(Text name, List<TooltipComponent> tooltip, Supplier<String> search, int height) {
		this.name = name;
		this.tooltip = tooltip;
		this.search = search;
		this.height = height;
	}

	public void setChildren(List<? extends Element> children) {
		this.children = children;
	}

	public void update(int y, int x, int width, int height) {
	}

	@Override
	public void render(DrawContext raw, int index, int y, int x, int width, int height, int mouseX, int mouseY,
			boolean hovered, float delta) {
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		if (group != null) {
			context.fill(x + 4, y + height / 2 - 1, 6, 2, 0xffffffff);
			if (endGroup) {
				context.fill(x + 2, y - 4, 2, height / 2 + 5, 0xffffffff);
			} else {
				context.fill(x + 2, y - 4, 2, height + 4, 0xffffffff);
			}
			x += 10;
			width -= 10;
		}
		update(y, x, width, height);
		context.fill(x, y, width, height, 0x66000000);
		context.drawTextWithShadow(this.name, x + 6, y + 10 - parentList.client.textRenderer.fontHeight / 2, 0xFFFFFF);
		for (Element element : children()) {
			if (element instanceof Drawable drawable) {
				drawable.render(context.raw(), mouseX, mouseY, delta);
			}
		}
	}

	@Override
	public List<TooltipComponent> getTooltip(int mouseX, int mouseY) {
		return tooltip;
	}

	public String getSearchableText() {
		return name.getString();
	}

	public boolean isParentVisible() {
		for (GroupNameWidget g : parentGroups) {
			if (g.collapsed) {
				return false;
			}
		}
		return true;
	}

	public boolean isVisible() {
		String s = search.get().toLowerCase();
		if (getSearchableText().toLowerCase().contains(s)) {
			return true;
		}
		for (GroupNameWidget g : parentGroups) {
			if (g.text.getString().toLowerCase().contains(s)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int getHeight() {
		if (isParentVisible() && isVisible()) {
			return height;
		}
		return 0;
	}

	@Override
	public List<? extends Element> children() {
		return children;
	}
}
