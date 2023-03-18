package dev.emi.emi.screen.tooltip;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.stack.EmiIngredient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class IngredientTooltipComponent implements EmiTooltipComponent {
	private static final Identifier TEXTURE = new Identifier("emi", "textures/gui/widgets.png");
	private static final int MAX_DISPLAYED = 63;
	private final List<? extends EmiIngredient> ingredients;
	
	public IngredientTooltipComponent(List<? extends EmiIngredient> ingredients) {
		this.ingredients = ingredients;
	}

	public int getStackWidth() {
		if (ingredients.size() < 4) {
			return ingredients.size();
		} else if (ingredients.size() > 16) {
			return 8;
		} else {
			return 4;
		}
	}

	@Override
	public int getHeight() {
		int s = ingredients.size();
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
	public void drawTooltip(MatrixStack matrices, TooltipRenderData render) {
		int sw = getStackWidth();
		for (int i = 0; i < ingredients.size() && i < MAX_DISPLAYED; i++) {
			ingredients.get(i).render(matrices, i % sw * 18, i / sw * 18, MinecraftClient.getInstance().getTickDelta());
		}
		if (ingredients.size() > MAX_DISPLAYED) {
			EmiPort.setPositionColorTexShader();
			RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
			RenderSystem.setShaderTexture(0, TEXTURE);
			DrawableHelper.drawTexture(matrices, getWidth(render.text) - 14, getHeight() - 8, 0, 192, 9, 3, 256, 256);
		}
	}
}
