package dev.emi.emi.api.widget;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.screen.RecipeScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Rect2i;

public class RecipeFillButtonWidget extends Widget {
	private final int x, y;
	private final EmiRecipe recipe;
	private final boolean canFill;

	public RecipeFillButtonWidget(int x, int y, EmiRecipe recipe) {
		this.x = x;
		this.y = y;
		this.recipe = recipe;
		MinecraftClient client = MinecraftClient.getInstance();
		HandledScreen<?> hs = null;
		if (client.currentScreen instanceof RecipeScreen rs) {
			hs = rs.old;
		} else if (client.currentScreen instanceof HandledScreen<?> s) {
			hs = s;
		}
		canFill = hs != null && recipe.canFill(hs);
	}

	@Override
	public Rect2i getBounds() {
		return new Rect2i(x, y, 11, 11);
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		int v = 64;
		if (!canFill) {
			v = 88;
		} else if (getBounds().contains(mouseX, mouseY)) {
			v = 76;
		}
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		RenderSystem.setShaderTexture(0, EmiRenderHelper.WIDGETS);
		DrawableHelper.drawTexture(matrices, x, y, 12, 12, 24, v, 12, 12, 256, 256);
	}

	@Override
	public List<TooltipComponent> getTooltip() {
		// TODO Auto-generated method stub
		return super.getTooltip();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (canFill) {
			EmiApi.performFill(recipe, EmiUtil.isShiftDown());
			return true;
		}
		return false;
	}
}
