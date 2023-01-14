package dev.emi.emi.screen.widget;

import java.util.List;
import java.util.function.Supplier;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.EmiHistory;
import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.widget.RecipeDefaultButtonWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;

public class ResolutionButtonWidget extends ButtonWidget {
	public Supplier<Widget> hoveredWidget;
	public EmiIngredient stack;

	public ResolutionButtonWidget(int x, int y, int width, int height, EmiIngredient stack, Supplier<Widget> hoveredWidget) {
		super(x, y, width, height, EmiPort.literal(""), button -> {
			BoM.tree.addResolution(stack, null);
			EmiHistory.pop();
		});
		this.stack = stack;
		this.hoveredWidget = hoveredWidget;
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);
		if (this.isHovered()) {
			MinecraftClient client = MinecraftClient.getInstance();
			client.currentScreen.renderTooltip(matrices, List.of(
				EmiPort.translatable("tooltip.emi.resolution"),
				EmiPort.translatable("tooltip.emi.select_resolution"),
				EmiPort.translatable("tooltip.emi.default_resolution"),
				EmiPort.translatable("tooltip.emi.clear_resolution")
			), mouseX, mouseY);
		}
		stack.render(matrices, x + 1, y + 1, delta);
	}

	@Override
	public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		RenderSystem.setShaderTexture(0, EmiRenderHelper.WIDGETS);
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
		DrawableHelper.drawTexture(matrices, x, y, u, 128, width, height, 256, 256);
	}
}
