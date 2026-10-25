package dev.emi.emi.screen;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.widget.config.EmiNameWidget;
import dev.emi.emi.screen.widget.config.ListWidget;

public class ConfigEnumScreen<T> extends Screen {
	private final ConfigScreen last;
	private final List<Entry<T>> entries;
	private final Consumer<T> selection;
	private ListWidget list;

	public ConfigEnumScreen(ConfigScreen last, List<Entry<T>> entries, Consumer<T> selection) {
		super(EmiPort.translatable("screen.emi.config"));
		this.last = last;
		this.entries = entries;
		this.selection = selection;
	}

	@Override
	public void init() {
		super.init();
		this.addRenderableOnly(new EmiNameWidget(width / 2, 16));
		int w = 200;
		int x = (width - w) / 2;
		this.addRenderableWidget(EmiPort.newButton(x, height - 30, w, 20, EmiPort.translatable("gui.done"), button -> {
			onClose();
		}));
		list = new ListWidget(minecraft, width, height, 40, height - 40);
		for (Entry<T> e : entries) {
			list.addEntry(new SelectionWidget<T>(this, e));
		}
		this.addWidget(list);
	}

	@Override
	public void render(GuiGraphics raw, int mouseX, int mouseY, float delta) {
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		list.setScrollAmount(list.getScrollAmount());
		super.render(context.raw(), mouseX, mouseY, delta);
		list.render(context.raw(), mouseX, mouseY, delta);
		ListWidget.Entry entry = list.getHoveredEntry();
		if (entry instanceof SelectionWidget<?> widget) {
			if (widget.button.isHovered()) {
				EmiRenderHelper.drawTooltip(this, context, widget.tooltip, mouseX, mouseY);
			}
		}
	}

	@Override
	public void onClose() {
		Minecraft.getInstance().setScreen(last);
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			this.onClose();
			return true;
		} else if (this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
			this.onClose();
			return true;
		} else if (keyCode == GLFW.GLFW_KEY_TAB) {
			return false;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	public static record Entry<T>(T value, Component name, List<ClientTooltipComponent> tooltip) {
	}

	public static class SelectionWidget<T> extends ListWidget.Entry {
		private final Button button;
		private final List<ClientTooltipComponent> tooltip;

		public SelectionWidget(ConfigEnumScreen<T> screen, Entry<T> e) {
			button = EmiPort.newButton(0, 0, 200, 20, e.name(), t -> {
				screen.selection.accept(e.value());
				screen.onClose();
			});
			tooltip = e.tooltip();
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return List.of(button);
		}

		@Override
		public void render(GuiGraphics raw, int index, int y, int x, int width, int height, int mouseX, int mouseY,
				boolean hovered, float delta) {
			button.y = y;
			button.x = x + width / 2 - button.getWidth() / 2;
			button.render(raw, mouseX, mouseY, delta);
		}

		@Override
		public int getHeight() {
			return 20;
		}
	}
}
