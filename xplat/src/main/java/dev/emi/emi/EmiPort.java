package dev.emi.emi;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.block.Block;
import net.minecraft.block.TallFlowerBlock;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget.PressAction;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.recipe.Recipe;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.registry.Registry;

/**
 * Multiversion quarantine, to avoid excessive git pain
 */
public final class EmiPort {
	private static final net.minecraft.util.math.random.Random RANDOM = net.minecraft.util.math.random.Random.create();

	public static MutableText literal(String s) {
		return Text.literal(s);
	}

	public static MutableText literal(String s, Formatting formatting) {
		return Text.literal(s).formatted(formatting);
	}

	public static MutableText literal(String s, Formatting... formatting) {
		return Text.literal(s).formatted(formatting);
	}

	public static MutableText literal(String s, Style style) {
		return Text.literal(s).setStyle(style);
	}
	
	public static MutableText translatable(String s) {
		return Text.translatable(s);
	}
	
	public static MutableText translatable(String s, Formatting formatting) {
		return Text.translatable(s).formatted(formatting);
	}
	
	public static MutableText translatable(String s, Object... objects) {
		return Text.translatable(s, objects);
	}

	public static MutableText append(MutableText text, Text appended) {
		return text.append(appended);
	}

	public static OrderedText ordered(Text text) {
		return text.asOrderedText();
	}

	public static Collection<Identifier> findResources(ResourceManager manager, String prefix, Predicate<String> pred) {
		return manager.findResources(prefix, i -> pred.test(i.toString())).keySet();
	}

	public static InputStream getInputStream(Resource resource) {
		try {
			return resource.getInputStream();
		} catch (Exception e) {
			return null;
		}
	}

	public static BannerPattern.Patterns addRandomBanner(BannerPattern.Patterns patterns, Random random) {
		return patterns.add(Registry.BANNER_PATTERN.getEntry(random.nextInt(Registry.BANNER_PATTERN.size())).get(),
			DyeColor.values()[random.nextInt(DyeColor.values().length)]);
	}

	public static boolean canTallFlowerDuplicate(TallFlowerBlock tallFlowerBlock) {
		try {
			return tallFlowerBlock.isFertilizable(null, null, null, true) && tallFlowerBlock.canGrow(null, null, null, null);
		} catch(Exception e) {
			return false;
		}
	}

	public static void upload(VertexBuffer vb, BufferBuilder bldr) {
		vb.bind();
		vb.upload(bldr.end());
	}

	public static void setShader(VertexBuffer buf, Matrix4f mat) {
		buf.bind();
		buf.draw(mat, RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
	}

	public static List<BakedQuad> getQuads(BakedModel model) {
		return model.getQuads(null, null, RANDOM);
	}

	public static void draw(BufferBuilder bufferBuilder) {
		BufferRenderer.drawWithShader(bufferBuilder.end());
	}

	public static int getGuiScale(MinecraftClient client) {
		return (int) client.getWindow().getScaleFactor();
	}

	public static void setPositionTexShader() {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
	}

	public static void setPositionColorTexShader() {
		RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
	}

	public static Registry<Item> getItemRegistry() {
		return Registry.ITEM;
	}

	public static Registry<Block> getBlockRegistry() {
		return Registry.BLOCK;
	}

	public static Registry<Fluid> getFluidRegistry() {
		return Registry.FLUID;
	}

	public static Registry<Potion> getPotionRegistry() {
		return Registry.POTION;
	}

	public static Registry<Enchantment> getEnchantmentRegistry() {
		return Registry.ENCHANTMENT;
	}

	public static ButtonWidget newButton(int x, int y, int w, int h, Text name, PressAction action) {
		return new ButtonWidget(x, y, w, h, name, action);
	}

	public static ItemStack getOutput(Recipe<?> recipe) {
		return recipe.getOutput();
	}

	public static void focus(TextFieldWidget widget, boolean focused) {
		widget.setTextFieldFocused(focused);
	}

	public static void drawCenteredText(MatrixStack matrices, TextRenderer renderer, Text text, int x, int y, int color) {
		drawCenteredText(matrices, renderer, text, x, y, color, false);
	}

	public static void drawCenteredText(MatrixStack matrices, TextRenderer renderer, Text text, int x, int y, int color, boolean shadow) {
		if (shadow) {
			DrawableHelper.drawCenteredTextWithShadow(matrices, renderer, text.asOrderedText(), x, y, color);
		} else {
			DrawableHelper.drawCenteredText(matrices, renderer, text, x, y, color);
		}
	}
}
