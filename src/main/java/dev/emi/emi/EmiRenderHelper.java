package dev.emi.emi;

import java.util.List;

import org.apache.commons.compress.utils.Lists;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.mixin.accessor.ScreenAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class EmiRenderHelper {
	public static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	public static final Identifier WIDGETS = new Identifier("emi", "textures/gui/widgets.png");
	public static final Identifier BACKGROUND = new Identifier("emi", "textures/gui/background.png");

	public static void drawNinePatch(MatrixStack matrices, int x, int y, int w, int h, int u, int v, int cornerLength, int centerLength) {
		int cor = cornerLength;
		int cen = centerLength;
		int corcen = cor + cen;
		int innerWidth = w - cornerLength * 2;
		int innerHeight = h - cornerLength * 2;
		int coriw = cor + innerWidth;
		int corih = cor + innerHeight;
		// TL
		DrawableHelper.drawTexture(matrices, x,         y,         cor,        cor,         u,          v,          cor, cor, 256, 256);
		// T
		DrawableHelper.drawTexture(matrices, x + cor,   y,         innerWidth, cor,         u + cor,    v,          cen, cor, 256, 256);
		// TR
		DrawableHelper.drawTexture(matrices, x + coriw, y,         cor,        cor,         u + corcen, v,          cor, cor, 256, 256);
		// L
		DrawableHelper.drawTexture(matrices, x,         y + cor,   cor,        innerHeight, u,          v + cor,    cor, cen, 256, 256);
		// C
		DrawableHelper.drawTexture(matrices, x + cor,   y + cor,   innerWidth, innerHeight, u + cor,    v + cor,    cen, cen, 256, 256);
		// R
		DrawableHelper.drawTexture(matrices, x + coriw, y + cor,   cor,        innerHeight, u + corcen, v + cor,    cor, cen, 256, 256);
		// BL
		DrawableHelper.drawTexture(matrices, x,         y + corih, cor,        cor,         u,          v + corcen, cor, cor, 256, 256);
		// B
		DrawableHelper.drawTexture(matrices, x + cor,   y + corih, innerWidth, cor,         u + cor,    v + corcen, cen, cor, 256, 256);
		// BR
		DrawableHelper.drawTexture(matrices, x + coriw, y + corih, cor,        cor,         u + corcen, v + corcen, cor, cor, 256, 256);
	}

	public static Text getEmiText() {
		return
			EmiPort.append(
				EmiPort.append(
					EmiPort.literal("E", Style.EMPTY.withColor(0xeb7bfc)),
					EmiPort.literal("M", Style.EMPTY.withColor(0x7bfca2))),
				EmiPort.literal("I", Style.EMPTY.withColor(0x7bebfc)));
	}

	public static Text getPageText(int page, int total, int maxWidth) {
		Text text = EmiPort.translatable("emi.page", page, total);
		if (CLIENT.textRenderer.getWidth(text) > maxWidth) {
			text = EmiPort.translatable("emi.page.short", page, total);
			if (CLIENT.textRenderer.getWidth(text) > maxWidth) {
				text = EmiPort.literal("" + page);
				if (CLIENT.textRenderer.getWidth(text) > maxWidth) {
					text = EmiPort.literal("");
				}
			}
		}
		return text;
	}

	public static void drawTooltip(Screen screen, MatrixStack matrices, List<TooltipComponent> components, int x, int y) {
		y = Math.max(16, y);
		// Some mods assume this list will be mutable, oblige them
		List<TooltipComponent> mutable = Lists.newArrayList();
		mutable.addAll(components);
		((ScreenAccessor) screen).invokeRenderTooltipFromComponents(matrices, mutable, x, y);
	}

	public static void drawSlotHightlight(MatrixStack matrices, int x, int y, int w, int h) {
		matrices.push();
		matrices.translate(0, 0, 100);
        RenderSystem.disableDepthTest();
        RenderSystem.colorMask(true, true, true, false);
		DrawableHelper.fill(matrices, x, y, x + w, y + h, -2130706433);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.enableDepthTest();
		matrices.pop();
	}

	public static int getAmountOverflow(Text amount) {
		int width = CLIENT.textRenderer.getWidth(amount);
		if (width > 14) {
			return width - 14;
		} else {
			return 0;
		}
	}

	public static void renderAmount(MatrixStack matrices, int x, int y, Text amount) {
		matrices.push();
		matrices.translate(0, 0, 200);
		int tx = x + 17 - Math.min(14, CLIENT.textRenderer.getWidth(amount));
		CLIENT.textRenderer.drawWithShadow(matrices, amount, tx, y + 9, -1);
		matrices.pop();
	}

	public static void renderIngredient(EmiIngredient ingredient, MatrixStack matrices, int x, int y) {
		RenderSystem.disableDepthTest();
		RenderSystem.setShaderTexture(0, EmiRenderHelper.WIDGETS);
		DrawableHelper.drawTexture(matrices, x, y, 8, 252, 4, 4, 256, 256);
	}

	public static void renderTag(EmiIngredient ingredient, MatrixStack matrices, int x, int y) {
		RenderSystem.disableDepthTest();
		if (ingredient.getEmiStacks().size() > 1) {
			RenderSystem.setShaderTexture(0, EmiRenderHelper.WIDGETS);
			DrawableHelper.drawTexture(matrices, x, y + 12, 0, 252, 4, 4, 256, 256);
		}
	}

	public static void renderRemainder(EmiIngredient ingredient, MatrixStack matrices, int x, int y) {
		for (EmiStack stack : ingredient.getEmiStacks()) {
			EmiStack remainder = stack.getRemainder();
			if (!remainder.isEmpty()) {
				if (remainder.equals(ingredient)) {
					renderCatalyst(ingredient, matrices, x, y);
				} else {
					RenderSystem.disableDepthTest();
					RenderSystem.setShaderTexture(0, WIDGETS);
					DrawableHelper.drawTexture(matrices, x + 12, y, 4, 252, 4, 4, 256, 256);
				}
				return;
			}
		}
	}

	public static void renderCatalyst(EmiIngredient ingredient, MatrixStack matrices, int x, int y) {
		RenderSystem.disableDepthTest();
		RenderSystem.setShaderTexture(0, WIDGETS);
		DrawableHelper.drawTexture(matrices, x + 12, y, 12, 252, 4, 4, 256, 256);
		return;
	}

	public static void renderRecipeFavorite(EmiIngredient ingredient, MatrixStack matrices, int x, int y) {
		matrices.push();
		matrices.translate(0, 0, 200);
		RenderSystem.disableDepthTest();
		RenderSystem.setShaderTexture(0, WIDGETS);
		DrawableHelper.drawTexture(matrices, x + 12, y, 16, 252, 4, 4, 256, 256);
		matrices.pop();
		return;
	}

	public static void renderRecipe(EmiRecipe recipe, MatrixStack matrices, int x, int y, boolean showMissing, int overlayColor) {
		RenderSystem.applyModelViewMatrix();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		RenderSystem.setShaderTexture(0, BACKGROUND);
		EmiRenderHelper.drawNinePatch(matrices, x, y, recipe.getDisplayWidth() + 8, recipe.getDisplayHeight() + 8, 0, 0, 4, 1);
		List<Widget> widgets = Lists.newArrayList();
		WidgetHolder holder = new WidgetHolder() {

			public int getWidth() {
				return recipe.getDisplayWidth();
			}

			public int getHeight() {
				return recipe.getDisplayHeight();
			}

			public <T extends Widget> T add(T widget) {
				widgets.add(widget);
				return widget;
			}
		};
		MatrixStack view = RenderSystem.getModelViewStack();
		view.push();
		view.translate(x + 4, y + 4, 0);
		RenderSystem.applyModelViewMatrix();
		recipe.addWidgets(holder);
		if (showMissing) {
			EmiClient.getAvailable(recipe);
		}
		float delta = MinecraftClient.getInstance().getTickDelta();
		for (Widget widget : widgets) {
			widget.render(matrices, -1000, -1000, delta);
		}
		if (overlayColor != -1) {
			DrawableHelper.fill(matrices, -1, -1, recipe.getDisplayWidth() + 1, recipe.getDisplayHeight() + 1, overlayColor);
		}
		EmiClient.availableForCrafting.clear();
		view.pop();
		RenderSystem.applyModelViewMatrix();
	}
}
