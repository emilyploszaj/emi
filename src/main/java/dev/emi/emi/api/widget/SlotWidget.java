package dev.emi.emi.api.widget;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import org.apache.commons.compress.utils.Lists;

import dev.emi.emi.EmiConfig;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.text.LiteralText;

public class SlotWidget extends Widget {
	private final EmiIngredient stack;
	private final int x, y;
	private boolean drawBack = true, output = false;
	private EmiRecipe recipe;

	public SlotWidget(EmiIngredient stack, int x, int y) {
		this.stack = stack;
		this.x = x;
		this.y = y;
	}

	public SlotWidget drawBack(boolean drawBack) {
		this.drawBack = drawBack;
		return this;
	}

	public SlotWidget output(boolean output) {
		this.output = output;
		return this;
	}

	public SlotWidget recipeContext(EmiRecipe recipe) {
		this.recipe = recipe;
		return this;
	}

	@Override
	public Rect2i getBounds() {
		if (drawBack && output) {
			return new Rect2i(x, y, 25, 25);
		} else {
			return new Rect2i(x, y, 17, 17);
		}
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		Rect2i bounds = getBounds();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		RenderSystem.setShaderTexture(0, EmiRenderHelper.WIDGETS);
		int off = 1;
		if (drawBack) {
			if (output) {
				off = 5;
				DrawableHelper.drawTexture(matrices, bounds.getX(), bounds.getY(), 26, 26, 18, 0, 26, 26, 256, 256);
			} else {
				DrawableHelper.drawTexture(matrices, bounds.getX(), bounds.getY(), 18, 18, 0, 0, 18, 18, 256, 256);
			}
		}
		stack.render(matrices, bounds.getX() + off, bounds.getY() + off, delta);
	}
	
	@Override
	public List<TooltipComponent> getTooltip() {
		List<TooltipComponent> list = Lists.newArrayList();
		list.addAll(stack.getTooltip());
		if (recipe != null && EmiConfig.devMode) {
			list.add(TooltipComponent.of(new LiteralText(recipe.getId().toString()).asOrderedText()));
		}
		return list;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (EmiScreenManager.stackInteraction(stack, recipe, bind -> bind.matchesMouse(button))) {
			return true;
		} else if (button == 2 && EmiConfig.devMode && recipe != null) {
			MinecraftClient.getInstance().keyboard.setClipboard("\n    \"" + recipe.getId().toString() + "\",");
		} 
		return false;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return EmiScreenManager.stackInteraction(stack, recipe, bind -> bind.matchesKey(keyCode, scanCode));
	}
}
