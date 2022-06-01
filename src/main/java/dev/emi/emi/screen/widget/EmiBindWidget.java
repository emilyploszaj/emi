package dev.emi.emi.screen.widget;

import java.util.List;

import org.apache.commons.compress.utils.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.bind.EmiBind;
import dev.emi.emi.bind.EmiBind.ModifiedKey;
import dev.emi.emi.screen.ConfigScreen;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class EmiBindWidget extends ListWidget.Entry {
	private final ConfigScreen screen;
	private final Drawable tooltip;
	private final Text bindName;
	private EmiBind bind;
	private List<ButtonWidget> buttons = Lists.newArrayList();

	public EmiBindWidget(ConfigScreen screen, Drawable tooltip, EmiBind bind) {
		this.tooltip = tooltip;
		this.screen = screen;
		this.bindName = EmiPort.translatable(bind.translationKey);
		this.bind = bind;
		updateButtons();
	}

	private void updateButtons() {
		buttons.clear();
		for (int i = 0; i < bind.boundKeys.size(); i++) {
			final int j = i;
			ButtonWidget widget = new ButtonWidget(0, 0, 200, 20, getKeyText(bind.boundKeys.get(i)), button -> {
				screen.setActiveBind(bind, j);
			}) {

				@Override
				protected MutableText getNarrationMessage() {
					if (j < bind.boundKeys.size() && bind.boundKeys.get(j).isUnbound()) {
						return EmiPort.translatable("narrator.controls.unbound", bindName);
					}
					return EmiPort.translatable("narrator.controls.bound", bindName, super.getNarrationMessage());
				}
			};
			buttons.add(widget);
		}
	}

	@Override
	public void render(MatrixStack matrices, int index, int y, int x, int width, int height, int mouseX, int mouseY,
			boolean hovered, float delta) {
		if (buttons.size() != bind.boundKeys.size()) {
			updateButtons();
		}
		DrawableHelper.fill(matrices, x, y, x + width, y + height, 0x66000000);
		parentList.client.textRenderer.drawWithShadow(matrices, this.bindName, x,
			y + 10 - parentList.client.textRenderer.fontHeight / 2, 0xFFFFFF);
		int h = 0;
		for (int i = 0; i < buttons.size(); i++) {
			ButtonWidget button = buttons.get(i);
			button.x = x + width - 224;
			button.y = y + h;
			if (screen.activeBind == bind && screen.activeBindOffset == i) {
				button.setWidth(200);
				button.x = x + width - 224;
				if (screen.lastModifier == 0) {
					button.setMessage(EmiPort.literal("...").formatted(Formatting.YELLOW));
				} else {
					button.setMessage(getKeyText(new ModifiedKey(InputUtil.Type.KEYSYM
						.createFromCode(screen.lastModifier), screen.activeModifiers)).formatted(Formatting.YELLOW));
				}
			} else if (i < bind.boundKeys.size()) {
				if (bind.boundKeys.get(i).isUnbound() && i > 0) {
					button.setWidth(20);
					button.x = x + width - 20;
					button.y = y;
					button.setMessage(EmiPort.literal("+").formatted(Formatting.AQUA));
				} else {
					button.setMessage(getKeyText(bind.boundKeys.get(i)));
				}
			}
			button.render(matrices, mouseX, mouseY, delta);
			h += 24;
		}
	}

	@Override
	public void renderTooltip(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		tooltip.render(matrices, mouseX, mouseY, delta);
	}

	@Override
	public int getHeight() {
		int size = buttons.size() * 24;
		if (buttons.size() > 1 && buttons.size() <= bind.boundKeys.size() && bind.boundKeys.get(buttons.size() - 1).isUnbound()
				&& (screen.activeBind != bind || screen.activeBindOffset != buttons.size() - 1)) {
			size -= 24;
		}
		return size;
	}

	private MutableText getKeyText(ModifiedKey key) {
		MutableText text = EmiPort.literal("");
		appendModifiers(text, key.modifiers());
		text.append(key.key().getLocalizedText());
		return text;
	}

	private void appendModifiers(MutableText text, int modifiers) {
		if ((modifiers & EmiUtil.CONTROL_MASK) > 0) {
			text.append(EmiPort.translatable("key.keyboard.control"));
			text.append(EmiPort.literal(" + "));
		}
		if ((modifiers & EmiUtil.ALT_MASK) > 0) {
			text.append(EmiPort.translatable("key.keyboard.alt"));
			text.append(EmiPort.literal(" + "));
		}
		if ((modifiers & EmiUtil.SHIFT_MASK) > 0) {
			text.append(EmiPort.translatable("key.keyboard.shift"));
			text.append(EmiPort.literal(" + "));
		}
	}

	@Override
	public List<? extends Element> children() {
		return buttons;
	}
}
