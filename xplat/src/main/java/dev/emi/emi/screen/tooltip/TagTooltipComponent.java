package dev.emi.emi.screen.tooltip;

import java.util.List;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.util.Identifier;

public class TagTooltipComponent implements EmiTooltipComponent {
	private static final Identifier TEXTURE = EmiPort.id("emi", "textures/gui/widgets.png");
	private static final int MAX_DISPLAYED = 63;
	private final List<EmiStack> stacks;

	public TagTooltipComponent(List<EmiStack> stacks) {
		this.stacks = stacks;
	}

	public int getStackWidth() {
		if (stacks.size() < 4) {
			return stacks.size();
		} else if (stacks.size() > 16) {
			return 8;
		} else {
			return 4;
		}
	}

	@Override
	public int getHeight() {
		int s = stacks.size();
		if (s > MAX_DISPLAYED) {
			s = MAX_DISPLAYED;
		}
		return ((s - 1) / getStackWidth() + 1) * 18;
	}

	@Override
	public int getWidth(TextRenderer textRenderer) {
		return 18 * getStackWidth();
	}

	@Override
	public void drawTooltip(EmiDrawContext context, TooltipRenderData render) {
		int sw = getStackWidth();
		for (int i = 0; i < stacks.size() && i < MAX_DISPLAYED; i++) {
			context.drawStack(stacks.get(i), i % sw * 18, i / sw * 18, EmiIngredient.RENDER_ICON);
		}
		if (stacks.size() > MAX_DISPLAYED) {
			context.resetColor();
			context.drawTexture(TEXTURE, getWidth(render.text) - 14, getHeight() - 8, 0, 192, 9, 3);
		}
	}
}
