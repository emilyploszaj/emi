package dev.emi.emi.screen;

import java.util.List;
import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.widget.config.EmiNameWidget;
import dev.emi.emi.screen.widget.config.ListWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

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
		this.addDrawable(new EmiNameWidget(width / 2, 16));
		int w = 200;
		int x = (width - w) / 2;
		this.addDrawableChild(EmiPort.newButton(x, height - 30, w, 20, EmiPort.translatable("gui.done"), button -> {
			close();
		}));
		list = new ListWidget(client, width, height, 40, height - 40);
		for (Entry<T> e : entries) {
			list.addEntry(new SelectionWidget<T>(this, e));
		}
		this.addSelectableChild(list);
	}

	@Override
	public void render(DrawContext raw, int mouseX, int mouseY, float delta) {
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		list.setScrollAmount(list.getScrollAmount());
		this.renderBackgroundTexture(context.raw());
		list.render(context.raw(), mouseX, mouseY, delta);
		super.render(context.raw(), mouseX, mouseY, delta);
		ListWidget.Entry entry = list.getHoveredEntry();
		if (entry instanceof SelectionWidget<?> widget) {
			if (widget.button.isHovered()) {
				EmiRenderHelper.drawTooltip(this, context, widget.tooltip, mouseX, mouseY);
			}
		}
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		// Prevent double background draw
	}

	@Override
	public void close() {
		MinecraftClient.getInstance().setScreen(last);
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			this.close();
			return true;
		} else if (this.client.options.inventoryKey.matchesKey(keyCode, scanCode)) {
			this.close();
			return true;
		} else if (keyCode == GLFW.GLFW_KEY_TAB) {
			return false;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	public static record Entry<T>(T value, Text name, List<TooltipComponent> tooltip) {
	}

	public static class SelectionWidget<T> extends ListWidget.Entry {
		private final ButtonWidget button;
		private final List<TooltipComponent> tooltip;

		public SelectionWidget(ConfigEnumScreen<T> screen, Entry<T> e) {
			button = EmiPort.newButton(0, 0, 200, 20, e.name(), t -> {
				screen.selection.accept(e.value());
				screen.close();
			});
			tooltip = e.tooltip();
		}

		@Override
		public List<? extends Element> children() {
			return List.of(button);
		}

		@Override
		public void render(DrawContext raw, int index, int y, int x, int width, int height, int mouseX, int mouseY,
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
