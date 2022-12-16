package dev.emi.emi;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.CommandDispatcher;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.block.Block;
import net.minecraft.block.TallFlowerBlock;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget.PressAction;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.registry.Registry;

/**
 * Multiversion quarantine, to avoid excessive git pain
 */
public class EmiPort {
	private static final Random RANDOM = new Random();

	public static MutableText literal(String s) {
		return new LiteralText(s);
	}

	public static MutableText literal(String s, Formatting formatting) {
		return new LiteralText(s).formatted(formatting);
	}

	public static MutableText literal(String s, Formatting... formatting) {
		return new LiteralText(s).formatted(formatting);
	}

	public static MutableText literal(String s, Style style) {
		return new LiteralText(s).setStyle(style);
	}
	
	public static MutableText translatable(String s) {
		return new TranslatableText(s);
	}
	
	public static MutableText translatable(String s, Formatting formatting) {
		return new TranslatableText(s).formatted(formatting);
	}
	
	public static MutableText translatable(String s, Object... objects) {
		return new TranslatableText(s, objects);
	}

	public static MutableText append(MutableText text, Text appended) {
		return text.append(appended);
	}

	public static OrderedText ordered(Text text) {
		return text.asOrderedText();
	}

	public static Text fluidName(FluidVariant fluid) {
		return FluidVariantRendering.getName(fluid);
	}

	public static Collection<Identifier> findResources(ResourceManager manager, String prefix, Predicate<String> pred) {
		return manager.findResources(prefix, pred);
	}

	public static InputStream getInputStream(Resource resource) {
		return resource.getInputStream();
	}

	public static void registerCommand(Consumer<CommandDispatcher<ServerCommandSource>> consumer) {
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> consumer.accept(dispatcher));
	}

	public static BannerPattern.Patterns addRandomBanner(BannerPattern.Patterns patterns, Random random) {
		return patterns.add(BannerPattern.values()[random.nextInt(BannerPattern.values().length)],
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
		bldr.end();
		vb.upload(bldr);
	}

	public static void setShader(VertexBuffer buf, Matrix4f mat) {
		buf.setShader(mat, RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
	}

	public static List<BakedQuad> getQuads(BakedModel model) {
		return model.getQuads(null, null, RANDOM);
	}

	public static void draw(BufferBuilder bufferBuilder) {
		bufferBuilder.end();
		BufferRenderer.draw(bufferBuilder);
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
}
