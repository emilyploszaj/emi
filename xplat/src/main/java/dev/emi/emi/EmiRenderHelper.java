package dev.emi.emi;

import java.text.DecimalFormat;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector2i;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.mixin.accessor.DrawContextAccessor;
import dev.emi.emi.mixin.accessor.OrderedTextTooltipComponentAccessor;
import dev.emi.emi.registry.EmiRecipeFiller;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.OrderedTextTooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.Fluid;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class EmiRenderHelper {
	public static final DecimalFormat TEXT_FORMAT = new DecimalFormat("0.##");
	public static final Text EMPTY_TEXT = EmiPort.literal("");
	public static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	public static final Identifier WIDGETS = EmiPort.id("emi", "textures/gui/widgets.png");
	public static final Identifier BUTTONS = EmiPort.id("emi", "textures/gui/buttons.png");
	public static final Identifier BACKGROUND = EmiPort.id("emi", "textures/gui/background.png");
	public static final Identifier GRID = EmiPort.id("emi", "textures/gui/grid.png");
	public static final Identifier DASH = EmiPort.id("emi", "textures/gui/dash.png");
	public static final Identifier CONFIG = EmiPort.id("emi", "textures/gui/config.png");
	public static final Identifier PIECES = EmiPort.id("emi", "textures/gui/pieces.png");

	public static void drawNinePatch(EmiDrawContext context, Identifier texture, int x, int y, int w, int h, int u, int v, int cornerLength, int centerLength) {
		int cor = cornerLength;
		int cen = centerLength;
		int corcen = cor + cen;
		int innerWidth = w - cornerLength * 2;
		int innerHeight = h - cornerLength * 2;
		int coriw = cor + innerWidth;
		int corih = cor + innerHeight;
		// TL
		context.drawTexture(texture, x,         y,         cor,        cor,         u,          v,          cor, cor, 256, 256);
		// T
		context.drawTexture(texture, x + cor,   y,         innerWidth, cor,         u + cor,    v,          cen, cor, 256, 256);
		// TR
		context.drawTexture(texture, x + coriw, y,         cor,        cor,         u + corcen, v,          cor, cor, 256, 256);
		// L
		context.drawTexture(texture, x,         y + cor,   cor,        innerHeight, u,          v + cor,    cor, cen, 256, 256);
		// C
		context.drawTexture(texture, x + cor,   y + cor,   innerWidth, innerHeight, u + cor,    v + cor,    cen, cen, 256, 256);
		// R
		context.drawTexture(texture, x + coriw, y + cor,   cor,        innerHeight, u + corcen, v + cor,    cor, cen, 256, 256);
		// BL
		context.drawTexture(texture, x,         y + corih, cor,        cor,         u,          v + corcen, cor, cor, 256, 256);
		// B
		context.drawTexture(texture, x + cor,   y + corih, innerWidth, cor,         u + cor,    v + corcen, cen, cor, 256, 256);
		// BR
		context.drawTexture(texture, x + coriw, y + corih, cor,        cor,         u + corcen, v + corcen, cor, cor, 256, 256);
	}

	public static void drawTintedSprite(MatrixStack matrices, Sprite sprite, int color, int x, int y, int xOff, int yOff, int width, int height) {
		EmiPort.setPositionColorTexShader();
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		RenderSystem.setShaderTexture(0, sprite.getAtlasId());
		RenderSystem.enableBlend();
		
		float r = ((color >> 16) & 255) / 256f;
		float g = ((color >> 8) & 255) / 256f;
		float b = (color & 255) / 256f;
		
		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		bufferBuilder.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
		float xMin = (float) x;
		float yMin = (float) y;
		float xMax = xMin + width;
		float yMax = yMin + height;
		float uSpan = sprite.getMaxU() - sprite.getMinU();
		float vSpan = sprite.getMaxV() - sprite.getMinV();
		float uMin = sprite.getMinU() + uSpan / 16 * xOff;
		float vMin = sprite.getMinV() + vSpan / 16 * yOff;
		float uMax = sprite.getMaxU() - uSpan / 16 * (16 - (width + xOff));
		float vMax = sprite.getMaxV() - vSpan / 16 * (16 - (height + yOff));
		Matrix4f model = matrices.peek().getPositionMatrix();
		bufferBuilder.vertex(model, xMin, yMax, 1).color(r, g, b, 1).texture(uMin, vMax).next();
		bufferBuilder.vertex(model, xMax, yMax, 1).color(r, g, b, 1).texture(uMax, vMax).next();
		bufferBuilder.vertex(model, xMax, yMin, 1).color(r, g, b, 1).texture(uMax, vMin).next();
		bufferBuilder.vertex(model, xMin, yMin, 1).color(r, g, b, 1).texture(uMin, vMin).next();
		EmiPort.draw(bufferBuilder);
	}

	public static void drawScroll(EmiDrawContext context, int x, int y, int width, int height, int progress, int total, int color) {
		if (total <= 1) {
			return;
		}
		int start = x + width * progress / total;
		int end = start + Math.max(width / total, 1);
		if (progress == total - 1) {
			end = x + width;
			start = end - Math.max(width / total, 1);
		}
		context.fill(start, y, end - start, height, color);
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

	public static void drawLeftTooltip(Screen screen, EmiDrawContext context, List<TooltipComponent> components, int x, int y) {
		drawTooltip(screen, context, components, x, y, screen.width / 2 - 16,
			(screenWidth, screenHeight, mouseX, mouseY, tooltipWidth, tooltipHeight) -> {
				Vector2i pos = new Vector2i(mouseX, mouseY).add(12, -12);
				pos.x = Math.max(pos.x - 24 - tooltipWidth, 4);
				if (pos.y + tooltipHeight + 3 > screenHeight) {
					pos.y = screenHeight - tooltipHeight - 3;
				}
				return pos;
		});
	}

	public static void drawTooltip(Screen screen, EmiDrawContext context, List<TooltipComponent> components, int x, int y) {
		drawTooltip(screen, context, components, x, y, screen.width / 2 - 16);
	}

	public static void drawTooltip(Screen screen, EmiDrawContext context, List<TooltipComponent> components, int x, int y, int maxWidth) {
		drawTooltip(screen, context, components, x, y, maxWidth, HoveredTooltipPositioner.INSTANCE);
	}

	public static void drawTooltip(Screen screen, EmiDrawContext context, List<TooltipComponent> components, int x, int y, int maxWidth, TooltipPositioner positioner) {
		y = Math.max(16, y);
		// Some mods assume this list will be mutable, oblige them
		List<TooltipComponent> mutable = Lists.newArrayList();
		int wrapWidth = Math.max(components.stream()
			.map(c -> c instanceof OrderedTextTooltipComponent ? 0 : c.getWidth(CLIENT.textRenderer))
			.max(Integer::compare).orElse(0), maxWidth);
		for (TooltipComponent comp : components) {
			if (comp instanceof OrderedTextTooltipComponent ottc && ottc.getWidth(CLIENT.textRenderer) > wrapWidth) {
				try {
					OrderedText ordered = ((OrderedTextTooltipComponentAccessor) ottc).getText();
					MutableText text = Text.empty();
					// Mojang, what is this??? Please give me some other way to wrap
					ordered.accept(((var1, style, codepoint) -> {
						text.append(EmiPort.literal(String.valueOf(Character.toChars(codepoint)), style));
						return true;
					}));
					for (OrderedText o : CLIENT.textRenderer.wrapLines(text, wrapWidth)) {
						mutable.add(TooltipComponent.of(o));
					}
				} catch (Exception e) {
					e.printStackTrace();
					mutable.add(comp);
				}
			} else {
				mutable.add(comp);
			}
		}
		RenderSystem.enableDepthTest();
		EmiPort.setPositionTexShader();
		context.resetColor();
		((DrawContextAccessor) context.raw()).invokeDrawTooltip(CLIENT.textRenderer, mutable, x, y, positioner);
	}

	public static void drawSlotHightlight(EmiDrawContext context, int x, int y, int w, int h, int z) {
		context.push();
		context.matrices().translate(0, 0, z);
		RenderSystem.colorMask(true, true, true, false);
		context.fill(x, y, w, h, -2130706433);
		RenderSystem.colorMask(true, true, true, true);
		context.pop();
	}

	public static Text getAmountText(EmiIngredient stack) {
		return getAmountText(stack, stack.getAmount());
	}

	public static Text getAmountText(EmiIngredient stack, long amount) {
		if (stack.isEmpty() || amount == 0) {
			return EMPTY_TEXT;
		}
		if (stack.getEmiStacks().get(0).getKey() instanceof Fluid) {
			return getFluidAmount(amount);
		}
		return EmiPort.literal("" + amount);
	}

	public static Text getAmountText(EmiIngredient stack, double amount) {
		if (stack.isEmpty() || amount == 0) {
			return EMPTY_TEXT;
		}
		if (stack.getEmiStacks().get(0).getKey() instanceof Fluid) {
			return EmiConfig.fluidUnit.translate(amount);
		}
		return EmiPort.literal(TEXT_FORMAT.format(amount));
	}

	public static Text getFluidAmount(long amount) {
		return EmiConfig.fluidUnit.translate(amount);
	}

	public static int getAmountOverflow(Text amount) {
		int width = CLIENT.textRenderer.getWidth(amount);
		if (width > 14) {
			return width - 14;
		} else {
			return 0;
		}
	}

	public static void renderAmount(EmiDrawContext context, int x, int y, Text amount) {
		context.push();
		context.matrices().translate(0, 0, 200);
		int tx = x + 17 - Math.min(14, CLIENT.textRenderer.getWidth(amount));
		context.drawTextWithShadow(amount, tx, y + 9, -1);
		context.pop();
	}

	public static void renderIngredient(EmiIngredient ingredient, EmiDrawContext context, int x, int y) {
		RenderSystem.enableDepthTest();
		context.push();
		context.matrices().translate(0, 0, 200);
		RenderSystem.setShaderTexture(0, EmiRenderHelper.WIDGETS);
		context.drawTexture(WIDGETS, x, y, 8, 252, 4, 4);
		context.pop();
	}

	public static void renderTag(EmiIngredient ingredient, EmiDrawContext context, int x, int y) {
		if (ingredient.getEmiStacks().size() > 1) {
			RenderSystem.enableDepthTest();
			context.push();
			context.matrices().translate(0, 0, 200);
			context.drawTexture(WIDGETS, x, y + 12, 0, 252, 4, 4);
			context.pop();
		}
	}

	public static void renderRemainder(EmiIngredient ingredient, EmiDrawContext context, int x, int y) {
		for (EmiStack stack : ingredient.getEmiStacks()) {
			EmiStack remainder = stack.getRemainder();
			if (!remainder.isEmpty()) {
				if (remainder.equals(ingredient)) {
					renderCatalyst(ingredient, context, x, y);
				} else {
					context.push();
					context.matrices().translate(0, 0, 200);
					RenderSystem.enableDepthTest();
					context.drawTexture(WIDGETS, x + 12, y, 4, 252, 4, 4);
					context.pop();
				}
				return;
			}
		}
	}

	public static void renderCatalyst(EmiIngredient ingredient, EmiDrawContext context, int x, int y) {
		RenderSystem.enableDepthTest();
		context.push();
		context.matrices().translate(0, 0, 200);
		context.drawTexture(WIDGETS, x + 12, y, 12, 252, 4, 4);
		context.pop();
		return;
	}

	public static void renderRecipeFavorite(EmiIngredient ingredient, EmiDrawContext context, int x, int y) {
		context.push();
		context.matrices().translate(0, 0, 200);
		RenderSystem.enableDepthTest();
		context.drawTexture(WIDGETS, x + 12, y, 16, 252, 4, 4);
		context.pop();
		return;
	}

	public static void renderRecipeBackground(EmiRecipe recipe, EmiDrawContext context, int x, int y) {
		context.resetColor();
		EmiRenderHelper.drawNinePatch(context, BACKGROUND, x, y, recipe.getDisplayWidth() + 8, recipe.getDisplayHeight() + 8, 27, 0, 4, 1);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static void renderRecipe(EmiRecipe recipe, EmiDrawContext context, int x, int y, boolean showMissing, int overlayColor) {
		try {
			renderRecipeBackground(recipe, context, x, y);

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

			context.push();
			context.matrices().translate(x + 4, y + 4, 0);

			recipe.addWidgets(holder);
			float delta = MinecraftClient.getInstance().getTickDelta();
			for (Widget widget : widgets) {
				widget.render(context.raw(), -1000, -1000, delta);
			}
			if (overlayColor != -1) {
				context.fill(-1, -1, recipe.getDisplayWidth() + 2, recipe.getDisplayHeight() + 2, overlayColor);
			}

			if (showMissing) {
				HandledScreen hs = EmiApi.getHandledScreen();
				EmiRecipeHandler handler = EmiRecipeFiller.getFirstValidHandler(recipe, hs);
				if (handler != null) {
					handler.render(recipe, new EmiCraftContext(hs, handler.getInventory(hs), EmiCraftContext.Type.FILL_BUTTON), widgets, context.raw());
				} else if (EmiScreenManager.lastPlayerInventory != null) {
					StandardRecipeHandler.renderMissing(recipe, EmiScreenManager.lastPlayerInventory, widgets, context.raw());
				}
			}

			context.pop();

			// Force translucency to match that of the recipe background
			RenderSystem.disableBlend();
			RenderSystem.colorMask(false, false, false, true);
			RenderSystem.disableDepthTest();
			renderRecipeBackground(recipe, context, x, y);
			RenderSystem.enableDepthTest();
			RenderSystem.colorMask(true, true, true, true);
			// Blend should be off by default
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
