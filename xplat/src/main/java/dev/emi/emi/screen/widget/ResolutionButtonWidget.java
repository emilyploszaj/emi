package dev.emi.emi.screen.widget;

import java.util.List;
import java.util.function.Supplier;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.runtime.EmiHistory;
import dev.emi.emi.widget.RecipeDefaultButtonWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;

public class ResolutionButtonWidget extends ButtonWidget {
	public Supplier<Widget> hoveredWidget;
	public EmiIngredient stack;

	public ResolutionButtonWidget(int x, int y, int width, int height, EmiIngredient stack, Supplier<Widget> hoveredWidget) {
		super(x, y, width, height, EmiPort.literal(""), button -> {
			BoM.tree.addResolution(stack, null);
			EmiHistory.pop();
		}, s -> s.get());
		this.stack = stack;
		this.hoveredWidget = hoveredWidget;
	}

	@Override
	public void renderWidget(DrawContext raw, int mouseX, int mouseY, float delta) {
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		int u = 0;
		if (this.isHovered()) {
			u = 18;
		} else {
			Widget widget = hoveredWidget.get();
			if ((widget instanceof SlotWidget slot && slot.getRecipe() != null)
					|| widget instanceof RecipeDefaultButtonWidget) {
				u = 36;
			}
		}
		EmiTexture.SLOT.render(context.raw(), x, y, delta);
		context.drawTexture(EmiRenderHelper.WIDGETS, x, y, u, 128, width, height);
		if (this.isHovered()) {
			MinecraftClient client = MinecraftClient.getInstance();
			raw.drawTooltip(client.textRenderer, List.of(
				EmiPort.translatable("tooltip.emi.resolution"),
				EmiPort.translatable("tooltip.emi.select_resolution"),
				EmiPort.translatable("tooltip.emi.default_resolution"),
				EmiPort.translatable("tooltip.emi.clear_resolution")
			), mouseX, mouseY);
		}
		stack.render(raw, x + 1, y + 1, delta);
	}
}
