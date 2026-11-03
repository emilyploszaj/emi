package dev.emi.emi.screen.widget.config;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import com.google.common.collect.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.config.IntGroup;
import dev.emi.emi.screen.ConfigScreen.Mutator;

public class IntGroupWidget extends ConfigEntryWidget {
	public final IntGroup group;
	public final List<IntEdit> edits;

	public IntGroupWidget(Component name, List<ClientTooltipComponent> tooltip, Supplier<String> search, Mutator<IntGroup> mutator) {
		super(name, tooltip, search, 20);
		this.edits = Lists.newArrayList();
		this.group = mutator.get();
		int width = getWidth();
		List<GuiEventListener> children = Lists.newArrayList();
		for (int i = 0; i < group.size; i++) {
			final int f = i;
			IntEdit edit = new IntEdit(width, () -> group.values.getInt(f), v -> {
				group.values.set(f, v);
				mutator.set(group);
			});
			edits.add(edit);
			children.add(edit.text);
			children.add(edit.up);
			children.add(edit.down);
		}
		this.setChildren(children);
	}

	public int getSpacing() {
		if (group.values.size() > 2) {
			return 19;
		} else {
			return 20;
		}
	}

	public int getWidth() {
		if (group.values.size() > 2) {
			return 40;
		} else {
			return 99;
		}
	}

	@Override
	public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
		for (int i = 0; i < edits.size(); i++) {
			IntEdit e = edits.get(i);
			if (e.contains(mouseX, mouseY)) {
				return List.of(ClientTooltipComponent.create(EmiPort.ordered(group.getValueTranslation(i))));
			}
		}
		return super.getTooltip(mouseX, mouseY);
	}

	@Override
	public void update(int y, int x, int width, int height) {
		int spacing = getSpacing();
		int w = getWidth();
		int es = edits.size();
		for (int i = 0; i < es; i++) {
			int fromRight = es - i;
			int extra = 0;
			if (es > 2 && i < 2) {
				extra = -1;
			}
			edits.get(i).setPosition(x + width - w * fromRight - spacing * (fromRight - 1) + extra, y);
		}
	}
}
